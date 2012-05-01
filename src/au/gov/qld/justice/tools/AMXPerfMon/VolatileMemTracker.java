package au.gov.qld.justice.tools.AMXPerfMon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.LiveGraph.dataFile.write.DataStreamWriter;

public class VolatileMemTracker extends DataTracker {

	private static final Pattern regex = Pattern
			.compile("NonVolatile Free\\s*:\\s*(\\d+)/(\\d+)");

	VolatileMemTracker(DataStreamWriter logger) {
		super(logger, "NonVolatile Memory");
	}

	@Override
	String getQuery() {
		// This is already being sent out by the volatile mem tracker
		return null;
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
