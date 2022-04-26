package reso.utilities;

public class MonitorTimeMetric<M> extends Monitor {
	
	private class MonitorEventTimeMetric
		implements Monitor.MonitorEvent {
		final double time;
		final M      metric;
		public MonitorEventTimeMetric(double time, M metric) {
			this.time= time;
			this.metric= metric;
		}
		public String toString() {
			return time + "\t" + metric;
		}
	}
	
	public MonitorTimeMetric(String name, String source, boolean consoleEnabled) {
		super(name, source, consoleEnabled);
	}
	
	public void record(double time, M metric) {
		record(new MonitorEventTimeMetric(time, metric));
	}

}
