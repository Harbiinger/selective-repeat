package reso.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Monitor {
	
	public static interface MonitorEvent { };
	
	protected ArrayList<MonitorEvent> events= new ArrayList<MonitorEvent>();
	
	private boolean consoleEnabled= false;
	public final String name;
	public final String source;
	
	public Monitor(String name, String source, boolean consoleEnabled) {
		this.consoleEnabled= consoleEnabled;
		this.name= name;
		this.source= source;
	}
	
	public void record(MonitorEvent event) {
		events.add(event);
		if (consoleEnabled)
			System.out.println(source + "\t" + name + "\t" + event);
	}
	
	public void save(String fileName)
		throws IOException {
		FileWriter f= new FileWriter(fileName);
		try {
			for (MonitorEvent s: events)
				f.write(source + "\t" + name + "\t" + s.toString() + "\n");
		} finally {
			f.close();
		}
	}

}
