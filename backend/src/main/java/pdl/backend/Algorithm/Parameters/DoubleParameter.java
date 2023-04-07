package pdl.backend.Algorithm.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DoubleParameter extends Parameter {
	private final double min;
	private final double max;
	private final double step;

	public DoubleParameter(String name, String path, double min, double max, double step) {
		super(name, path, "range");
		this.max = max;
		this.min = min;
		this.step = step;
	}

	public double getMin() {
		return this.min;
	}

	public double getMax() {
		return this.max;
	}

	public double getStep() {
		return this.step;
	}

	@Override
	public void setValue(String value) {
		super.setValueObject(Double.parseDouble(value));
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = super.getNode(mapper);
		node.put("min", this.min);
		node.put("max", this.max);
		node.put("step", this.step);
		return node;
	}
}
