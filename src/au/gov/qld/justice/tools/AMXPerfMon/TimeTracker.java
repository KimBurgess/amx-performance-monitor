package au.gov.qld.justice.tools.AMXPerfMon;

import org.LiveGraph.dataFile.write.DataStreamWriter;

public class TimeTracker extends DataTracker {

	private final long startMillis;

	TimeTracker(DataStreamWriter logger) {
		super(logger, "Time");
		startMillis = System.currentTimeMillis();
	}

	@Override
	String getQuery() {
		return null;
	}

	@Override
	void update(CharSequence buf) {
		setDataValue(System.currentTimeMillis() - startMillis);
	}

}
