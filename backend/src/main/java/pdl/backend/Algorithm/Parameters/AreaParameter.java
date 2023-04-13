package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pdl.backend.Area;

public class AreaParameter extends Parameter {

	private final Area area;

	public AreaParameter(String name, String path) {
		super(name, path, "area");
		area = new Area(0, 0, 0, 0);
	}

	@Override
	public void setValue(String value) {
		this.area.setValues(value);
		super.setValueObject(this.area);
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = super.getNode(mapper);
		return node;
	}
}