package pdl.backend.Algorithm;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import pdl.backend.Algorithm.Parameters.Parameter;
import pdl.backend.Algorithm.Parameters.ParameterNullPointerException;

public class Algorithm {
	private final String name;
	private final String path;
	private final List<Parameter> parameters;
	private final AlgorithmFunctionInterface function;

	public Algorithm(String name, String path, List<Parameter> parameters, AlgorithmFunctionInterface function) {
		this.name = name;
		this.path = path;
		this.parameters = parameters;
		this.function = function;
	}

	public void apply(Planar<GrayU8> image) {
		List<Object> para = this.parameters.stream().map(p -> p.getValue()).toList();
		for (Object p : para) {
			if (p == null) {
				throw new ParameterNullPointerException("Parameter not set");
			}
		}
		this.function.apply(image, para);
	}

	public String getName() {
		return this.name;
	}

	public String getPath() {
		return this.path;
	}

	public List<Parameter> getParameters() {
		return this.parameters;
	}

	public ObjectNode getNode(ObjectMapper mapper) {
		ObjectNode node = mapper.createObjectNode();
		node.put("name", this.getName());
		node.put("path", this.getPath());
		ArrayNode parameters = node.putArray("parameters");
		for (Parameter p : this.getParameters()) {
			parameters.add(p.getNode(mapper));
		}
		return node;
	}
}
