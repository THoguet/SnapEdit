package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class Parameter {
	private final String name;
	private final String path;
	private final String type;
	private Object value;

	public Parameter(String name, String path, String type) {
		this.name = name;
		this.path = path;
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public String getPath() {
		return this.path;
	}

	public String getType() {
		return this.type;
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("name", this.name);
		node.put("path", this.path);
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
