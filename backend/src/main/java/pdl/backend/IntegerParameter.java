package pdl.backend;

public class IntegerParameter extends Parameter {
	private final int min;
	private final int max;
	private final int step;

	public IntegerParameter(String name, String displayName, int min, int max, int step) {
		super(name, displayName, "range");
		this.max = max;
		this.min = min;
		this.step = step;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	public int getStep() {
		return this.step;
	}

}
