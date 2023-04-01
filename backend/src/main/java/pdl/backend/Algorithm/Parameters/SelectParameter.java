package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SelectParameter extends Parameter {
	private final String[] options;

	public SelectParameter(String name, String displayName, String[] options) {
		super(name, displayName, "select");
		this.options = options;
	}

	public String[] getOptions() {
		return this.options;
	}

	@Override
	public void setValue(String value) {
		super.setValueObject(value);
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = super.getNode(mapper);
		ArrayNode optionsNode = mapper.createArrayNode();
		for (String option : this.options) {
			optionsNode.add(option);
		}
		node.set("options", optionsNode);
		return node;
	}
}
