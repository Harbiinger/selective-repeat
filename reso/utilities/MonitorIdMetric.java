package reso.utilities;

public class MonitorIdMetric<M> extends Monitor {

	private class MonitorEventIdMetric
	implements MonitorEvent {
		public final int id;
		public final M metric;
		public MonitorEventIdMetric(int id, M metric) {
			this.id= id;
			this.metric= metric;
		}
		public String toString() {
			return id + "\t" + metric;
		}
	}
	
	public MonitorIdMetric(String name, String source, boolean consoleEnabled) {
		super(name, source, consoleEnabled);
	}
	
	public void record(int id, M metric) {
		record(new MonitorEventIdMetric(id, metric));
	}
		
}
