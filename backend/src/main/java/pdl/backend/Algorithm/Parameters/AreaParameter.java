package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AreaParameter extends Parameter {

	public AreaParameter(String name, String displayName) {
		super(name, displayName, "area");
	}

	@Override
	public void setValue(String value) {
		String[] values = value.split(";");
		if (values.length != 4) {
			throw new IllegalArgumentException("Area parameter must have 4 values");
		}
		int[] intValues = new int[4];
		for (int i = 0; i < 4; i++) {
			intValues[i] = Integer.parseInt(values[i]);
		}
		super.setValueObject(intValues);
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = super.getNode(mapper);
		return node;
	}
}