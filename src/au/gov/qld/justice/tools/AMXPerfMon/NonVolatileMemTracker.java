package au.gov.qld.justice.tools.AMXPerfMon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.LiveGraph.dataFile.write.DataStreamWriter;

public class NonVolatileMemTracker extends DataTracker {

	private static final Pattern regex = Pattern
			.compile("Volatile Free\\s*:\\s*(\\d+)/(\\d+)");

	NonVolatileMemTracker(DataStreamWriter logger) {
		super(logger, "Volatile Memory");
	}

	@Override
	String getQuery() {
		return "show mem";
	}

	@Override
	void update(CharSequence buf) {
		Matcher m = regex.matcher(buf);
		if (m.find()) {
			long free = Long.parseLong(m.group(1));
			long max = Long.parseLong(m.group(2));
			setDataValue(((float)(max - free) / max) * 100);
		}
	}

}
