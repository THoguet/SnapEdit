package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IntegerParameter extends Parameter {
	private final int min;
	private final int max;
	private final int step;

	public IntegerParameter(String name, String path, int min, int max, int step) {
		super(name, path, "range");
		this.max = max;
		this.min = min;
		this.step = step;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	public int getStep() {
		return this.step;
	}

	@Override
	public void setValue(String value) {
		super.setValueObject(Integer.parseInt(value));
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = super.getNode(mapper);
		node.put("min", this.min);
		node.put("max", this.max);
		node.put("step", this.step);
		return node;
	}
}
