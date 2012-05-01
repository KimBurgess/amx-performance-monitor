package au.gov.qld.justice.tools.AMXPerfMon;

import org.LiveGraph.dataFile.write.DataStreamWriter;

abstract class DataTracker {

	protected final DataStreamWriter logger;

	protected final String name;

	DataTracker(DataStreamWriter logger, String name) {
		this.logger = logger;
		this.name = name;
		logger.addDataSeries(name);
	}

	protected void setDataValue(Float value) {
		logger.setDataValue(name, value);
	}
	
	protected void setDataValue(Long value) {
		logger.setDataValue(name, value);
	}

	abstract String getQuery();

	abstract void update(CharSequence buf);

}
