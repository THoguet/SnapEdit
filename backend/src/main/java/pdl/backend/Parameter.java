package pdl.backend;

public class Parameter {
	private final String name;
	private final String displayName;

	public Parameter(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public String getName() {
		return this.name;
	}

	public String getDisplayName() {
		return this.displayName;
	}
}
