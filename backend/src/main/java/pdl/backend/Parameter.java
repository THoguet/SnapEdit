package pdl.backend;

public class Parameter {
	private final String name;
	private final String displayName;
	private final String type;

	public Parameter(String name, String displayName, String type) {
		this.name = name;
		this.displayName = displayName;
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getType() {
		return this.type;
	}
}
