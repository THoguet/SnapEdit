package pdl.backend;

public class Parameter {
	private final String name;
	private final String displayName;
	private final int min;
	private final int max;
	private final int step;

	public Parameter(String name, String displayName, int min, int max, int step) {
		this.name = name;
		this.displayName = displayName;
		this.max = max;
		this.min = min;
		this.step = step;
	}

	public String getName() {
		return this.name;
	}

	public String getDisplayName() {
		return this.displayName;
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
