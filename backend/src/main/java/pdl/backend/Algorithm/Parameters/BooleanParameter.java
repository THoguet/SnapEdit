package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BooleanParameter extends Parameter {
	public BooleanParameter(String name, String path) {
		super(name, path, "boolean");
		super.setValueObject(false);
	}

	@Override
	public void setValue(String value) {
		super.setValueObject(Boolean.parseBoolean(value));
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = super.getNode(mapper);
		return node;
	}
}
