package pdl.backend;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlgorithmController {

	@Autowired
	private ObjectMapper mapper;

	@GetMapping(value = "/algorithms", produces = "application/json")
	@ResponseBody
	public ArrayNode getAlgorithmList() {
		ArrayList<Algorithm> algorithms = new ArrayList<Algorithm>();
		Algorithm algo = new Algorithm("Changement de luminosité", "changeLuminosity");
		algorithms.add(algo);
		algo = new Algorithm("Changement de teinte", "colorFilter");
		algorithms.add(algo);
		algo = new Algorithm("Filtre Moyenneur", "meanFilter");
		algorithms.add(algo);
		algo = new Algorithm("Filtre Gaussien", "gaussienFilter");
		algorithms.add(algo);
		algo = new Algorithm("Détection de contours", "contours");
		algorithms.add(algo);
		algo = new Algorithm("Égalisation d'histogramme", "histogram");
		algorithms.add(algo);

		ArrayNode nodes = mapper.createArrayNode();
		for (Algorithm a : algorithms) {
			ObjectNode objectNode = mapper.createObjectNode();
			objectNode.put("name", a.getName());
			objectNode.put("path", a.getPath());
			ArrayNode parameters = objectNode.putArray("parameters");
			for (Parameter p : a.getParameters()) {
				ObjectNode paramNode = mapper.createObjectNode();
				paramNode.put("name", p.getName());
				paramNode.put("displayName", p.getDisplayName());
				if (p instanceof IntegerParameter) {
					IntegerParameter copy = (IntegerParameter) p;
					paramNode.put("min", copy.getMin());
					paramNode.put("max", copy.getMax());
					paramNode.put("step", copy.getStep());
				} else if (p instanceof DoubleParameter) {
					DoubleParameter copy = (DoubleParameter) p;
					paramNode.put("min", copy.getMin());
					paramNode.put("max", copy.getMax());
					paramNode.put("step", copy.getStep());
				}
				parameters.add(paramNode);
			}
			nodes.add(objectNode);
		}
		return nodes;
	}

}
