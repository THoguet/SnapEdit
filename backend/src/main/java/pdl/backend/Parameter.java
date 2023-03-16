package pdl.backend;

public class Parameter {
	private final String name;
	private final String displayName;
	private final int min;
	private final int max;
	private final boolean mustBeOdd;

	public Parameter(String name, String displayName, int min, int max, boolean mustBeOdd) {
		this.name = name;
		this.displayName = displayName;
		this.max = max;
		this.min = min;
		this.mustBeOdd = mustBeOdd;
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

	public boolean mustBeOdd() {
		return this.mustBeOdd;
	}
}
