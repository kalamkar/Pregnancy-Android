package care.dovetail.common.model;

public class Measurement {
	public enum Unit {
		STEPS,
		GRAMS
	}

	public long startMillis;
	public long endMillis;
	public String name;
	public long value;
	public String unit;
	public String extra;
}
