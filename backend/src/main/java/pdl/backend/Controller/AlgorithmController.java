package pdl.backend.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import pdl.backend.Algorithm.Algorithm;
import pdl.backend.Algorithm.Parameters.AreaParameter;
import pdl.backend.Algorithm.Parameters.DoubleParameter;
import pdl.backend.Algorithm.Parameters.IntegerParameter;
import pdl.backend.Algorithm.Parameters.SelectParameter;
import pdl.backend.Image.ImageProcessing;
import pdl.backend.Image.ImageProcessing.FillingType;
import pdl.backend.Algorithm.Parameters.Parameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlgorithmController {

	@Autowired
	private ObjectMapper mapper;

	public static List<Algorithm> algorithmList() {
		final String[] borderTypes = { "SKIP", "ZERO", "NORMALIZED", "REFLECT", "EXTENDED", "WRAP" };
		final String[] fillingTypes = { "SKIP", "CONVOLUTION", "LEFT", "RIGHT", "TOP", "BOTTOM" };
		ArrayList<Algorithm> algorithms = new ArrayList<Algorithm>();
		algorithms.add(new Algorithm("Changement de luminosité", "changeLuminosity",
				Arrays.asList(new IntegerParameter("delta", "delta", -255, 255, 1)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.changeLuminosity(input, (int) para.get(0));
				}));
		algorithms.add(new Algorithm("Changement de teinte", "colorFilter",
				Arrays.asList(new IntegerParameter("hue", "Teinte", 0, 359, 1)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.colorFilter(input, (int) para.get(0));
				}));

		algorithms.add(new Algorithm("Filtre Moyenneur", "meanFilter", Arrays.asList(
				new IntegerParameter("size", "Taille du filtre", 1, 21, 2),
				new SelectParameter("borderType", "Type de bordure", borderTypes)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.meanFilter(input, (int) para.get(0), BorderType.valueOf((String) para.get(1)));
				}));

		algorithms.add(new Algorithm("Filtre Gaussien", "gaussienFilter", Arrays.asList(
				new IntegerParameter("size", "Taille du filtre", 1, 21, 2),
				new DoubleParameter("sigma", "Sigma", 0.1, 2, 0.1),
				new SelectParameter("borderType", "Type de bordure", borderTypes)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.gaussienFilter(input, (int) para.get(0), (double) para.get(1),
							BorderType.valueOf((String) para.get(2)));
				}));

		algorithms.add(new Algorithm("Détection de contours", "contours", new ArrayList<Parameter>(),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.gradientImageSobel(input);
				}));

		algorithms.add(new Algorithm("Égalisation d'histogramme", "histogram", new ArrayList<Parameter>(),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.histogram(input);
				}));

		algorithms.add(new Algorithm("Filtre négatif", "negative", new ArrayList<Parameter>(),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.oppositeColors(input);
				}));

		algorithms.add(new Algorithm("Filtre sépia", "sepia", new ArrayList<Parameter>(),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.sepiaFilter(input);
				}));
		algorithms.add(new Algorithm("Suppression zone", "deleteArea",
				Arrays.asList(new AreaParameter("area", "Zone"),
						new SelectParameter("fillingType", "Type de remplissage", fillingTypes)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.deleteArea(input, (int[]) para.get(0), FillingType.valueOf((String) para.get(1)));
				}));

		return algorithms;
	}

	@GetMapping(value = "/algorithms", produces = "application/json")
	@ResponseBody
	public ArrayNode getAlgorithmList() {
		List<Algorithm> algorithms = AlgorithmController.algorithmList();
		ArrayNode nodes = mapper.createArrayNode();
		for (Algorithm a : algorithms) {
			nodes.add(a.getNode(mapper));
		}
		return nodes;
	}

}
