package au.gov.qld.justice.tools.AMXPerfMon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.LiveGraph.dataFile.write.DataStreamWriter;

public class DuetMemTracker extends DataTracker {

	private static final Pattern free = Pattern
			.compile("Duet Memory Free\\s*:\\s*(\\d+)");

	private static final Pattern total = Pattern
			.compile("Duet Memory:\\s+(\\d+)M");

	DuetMemTracker(DataStreamWriter logger) {
		super(logger, "Duet Memory");
	}

	@Override
	String getQuery() {
		return "get duet mem";
	}

	@Override
	void update(CharSequence buf) {
		Matcher m = total.matcher(buf);
		if (m.find()) {
			long max = Integer.parseInt(m.group(1)) * 0x100000;
			m = free.matcher(buf);
			if (m.find()) {
				long free = Long.parseLong(m.group(1));
				setDataValue(((float)(max - free) / max) * 100);
			}
		}
	}

}
