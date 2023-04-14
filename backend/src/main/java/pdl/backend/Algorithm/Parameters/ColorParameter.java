package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ColorParameter extends Parameter {
	public ColorParameter(String name, String path) {
		super(name, path, "color");
		super.setValue("#000000");
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = super.getNode(mapper);
		return node;
	}
}
