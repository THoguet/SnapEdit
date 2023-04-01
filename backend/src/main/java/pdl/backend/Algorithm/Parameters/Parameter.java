package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class Parameter {
	private final String name;
	private final String displayName;
	private final String type;
	private Object value;

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

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("name", this.name);
		node.put("displayName", this.displayName);
		node.put("type", this.type);
		return node;
	}

	public void setValueObject(Object value) {
		this.value = value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}
}
