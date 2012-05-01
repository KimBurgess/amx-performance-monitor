package au.gov.qld.justice.tools.AMXPerfMon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.LiveGraph.dataFile.write.DataStreamWriter;

public class CPUTracker extends DataTracker {

	private static final Pattern regex = Pattern
			.compile("CPU usage = (\\d{1,2}\\.\\d{1,2})%");

	CPUTracker(DataStreamWriter logger) {
		super(logger, "CPU Usage");
	}

	@Override
	String getQuery() {
		return "cpu usage";
	}

	@Override
	void update(CharSequence buf) {
		Matcher m = regex.matcher(buf);
		if (m.find()) {
			setDataValue(Float.parseFloat(m.group(1)));
		}
	}

}
