package pdl.backend;

public class DoubleParameter extends Parameter {
	private final double min;
	private final double max;
	private final double step;

	public DoubleParameter(String name, String displayName, double min, double max, double step) {
		super(name, displayName);
		this.max = max;
		this.min = min;
		this.step = step;
	}

	public double getMin() {
		return this.min;
	}

	public double getMax() {
		return this.max;
	}

	public double getStep() {
		return this.step;
	}

}
