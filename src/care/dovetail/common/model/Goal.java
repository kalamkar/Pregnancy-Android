package care.dovetail.common.model;

public class Goal {
	public enum Aggregation {
		SUM,
		AVERAGE,
		MAX,
		MIN;
	}

	public String title;
	public long targetValue;
	public Aggregation aggregation;
	public Measurement value;

	// Either endMillis in measurement for one time goal or interval for repeating goal is needed
	public long intervalMillis;

	public boolean meetsTarget() {
		switch(aggregation) {
		case SUM:
			return targetValue >= value.value;
		case AVERAGE:
			// TODO(abhi): Add space for 5% variation to checking equality of target average.
			return System.currentTimeMillis() > value.endMillis && targetValue == value.value;
		case MAX:
			return System.currentTimeMillis() > value.endMillis && targetValue > value.value;
		case MIN:
			return System.currentTimeMillis() > value.endMillis && targetValue < value.value;
		}
		return false;
	}

	public Goal aggregate(Aggregation agg) {
		this.aggregation = agg;
		return this;
	}

	public Goal target(long targetValue, String unit) {
		this.targetValue = targetValue;
		this.value.unit = unit;
		return this;
	}

	public void updateValue(Measurement newValue) {
		if (value.unit != null && !value.unit.equalsIgnoreCase(newValue.unit)) {
			return;
		}
		switch(aggregation) {
		case SUM:
			value.value += newValue.value;
			break;
		case AVERAGE:
			value.value = (value.value + newValue.value) / 2;
			break;
		case MAX:
			value.value = Math.max(value.value, newValue.value);
			break;
		case MIN:
			value.value = Math.min(value.value, newValue.value);
			break;
		}
	}

	public static Goal repeated(String title, long startMillis, long intervalMillis) {
		Goal goal = new Goal();
		goal.title = title;
		goal.value = new Measurement();
		goal.value.startMillis = startMillis;
		goal.intervalMillis = intervalMillis;
		return goal;
	}

	public static Goal oneTime(String title, long startMillis, long exipiryMillis) {
		Goal goal = new Goal();
		goal.title = title;
		goal.value = new Measurement();
		goal.value.startMillis = startMillis;
		goal.value.endMillis = exipiryMillis;
		return goal;
	}
}