package au.gov.qld.justice.tools.AMXPerfMon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.LiveGraph.LiveGraph;
import org.LiveGraph.dataFile.common.PipeClosedByReaderException;
import org.LiveGraph.dataFile.write.DataStreamWriter;
import org.LiveGraph.settings.GraphSettings;
import org.LiveGraph.settings.GraphSettings.XAxisType;

public class AMXPerfMon {

	private static int DEFAULT_POLLING_INTERVAL = 100; // ms

	private Timer timer;

	private Socket sock;

	private InputStream in;

	private OutputStream out;

	private DataStreamWriter logger;

	private List<DataTracker> dataTrackers;

	public static void main(String[] args) {

		if (args.length < 1) {
			System.err
					.println("Usage: AMXPerfMon <master-ip> [<polling interval (ms)>]");
			System.exit(1);
		}
		String masterAddr = args[0];

		int pollingInterval = DEFAULT_POLLING_INTERVAL;
		if (args.length > 1) {
			pollingInterval = Integer.parseInt(args[1]);
		}

		new AMXPerfMon(masterAddr, pollingInterval);
	}

	public AMXPerfMon(String host, int pollingInterval) {
		try {
			connect(host);

			LiveGraph lg = getLiveGraphInstance();

			lg.getDataFileSettings().setUpdateFrequency(pollingInterval);

			logger = getLogger(lg, host + " performance monitor");

			addTrackers();

			configGraph(lg);
			
			timer = new Timer();
			timer.scheduleAtFixedRate(new Poller(), 0, pollingInterval);
		} catch (UnknownHostException e) {
			System.out.println("Could not find " + host);
		} catch (IOException e) {
			System.out.println("Could not connect to " + host);
		}
	}

	private void connect(String host) throws UnknownHostException, IOException {
		sock = new Socket(host, 23);
		in = sock.getInputStream();
		out = sock.getOutputStream();
	}

	private static LiveGraph getLiveGraphInstance() {
		LiveGraph lg = LiveGraph.application();
		lg.execStandalone();
		lg.guiManager().setDisplayDataFileSettingsWindows(false);
		lg.guiManager().setDisplayGraphSettingsWindows(false);
		return lg;
	}

	private static DataStreamWriter getLogger(LiveGraph lg, String name) {
		DataStreamWriter logger = lg.updateInvoker().startMemoryStreamMode();
		if (null == logger) {
			System.err
					.println("Could not switch LiveGraph into memory stream mode.");
			lg.disposeGUIAndExit();
			System.exit(1);
		}

		logger.setSeparator(";");
		logger.writeFileInfo(name);

		return logger;
	}

	private void addTrackers() {
		dataTrackers = new Vector<DataTracker>();
		dataTrackers.add(new TimeTracker(logger));
		dataTrackers.add(new CPUTracker(logger));
		dataTrackers.add(new VolatileMemTracker(logger));
		dataTrackers.add(new NonVolatileMemTracker(logger));
		dataTrackers.add(new DuetMemTracker(logger));
		dataTrackers.add(new DiskTracker(logger));
	}
	
	private void configGraph(LiveGraph lg) {
		GraphSettings conf = lg.getGraphSettings();
		
		// Set the graph bound for showing percentages
		conf.setMaxY(100);
		conf.setMinX(0);
		
		// Set the X axis to show time nicely
		conf.setXAxisType(XAxisType.XAxis_DataValSecsToSetPower);
		conf.setXAxisParamValue(-3);
		conf.setXAxisSeriesIndex(0);
	}

	private class Poller extends TimerTask {
		private void poll() {
			try {
				Iterator<DataTracker> iter = dataTrackers.iterator();
				while (iter.hasNext()) {
					DataTracker tracker = iter.next();
					if (tracker.getQuery() != null) {
						out.write(tracker.getQuery().getBytes());
						out.write(0x0D);
						out.flush();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void parseBuffer(CharSequence buf) {
			Iterator<DataTracker> iter = dataTrackers.iterator();
			while (iter.hasNext()) {
				DataTracker tracker = iter.next();
				tracker.update(buf);
			}

			logger.writeDataSet();
		}

		@Override
		public void run() {
			byte[] buf = new byte[2056];
			int bytesRead = 0;

			try {
				if ((bytesRead = in.read(buf)) > 0) {
					parseBuffer(new String(buf, 0, bytesRead));

					// If LiveGraph's main window was closed
					if (logger.hadIOException()) {
						if (logger.getIOException() instanceof PipeClosedByReaderException) {
							logger.close();
							timer.cancel();
							sock.close();
							System.exit(1);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			poll();
		}
	}

}
