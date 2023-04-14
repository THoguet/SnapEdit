package pdl.backend.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import pdl.backend.Area;
import pdl.backend.Algorithm.Algorithm;
import pdl.backend.Algorithm.Parameters.AreaParameter;
import pdl.backend.Algorithm.Parameters.BooleanParameter;
import pdl.backend.Algorithm.Parameters.ColorParameter;
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

	public final static List<Algorithm> ALGORITHMS = algorithmList();

	private final static List<Algorithm> algorithmList() {
		final String[] borderTypes = { "SKIP", "ZERO", "NORMALIZED", "REFLECT", "EXTENDED", "WRAP" };
		final String[] fillingTypes = { "SKIP", "CONVOLUTION", "LEFT", "RIGHT", "TOP", "BOTTOM" };
		final String[] newColors = { "RED", "GREEN", "BLUE", "YELLOW", "CYAN", "MAGENTA", "GREY" };
		ArrayList<Algorithm> algorithms = new ArrayList<Algorithm>();
		algorithms.add(new Algorithm("Changement de luminosité", "changeLuminosity",
				Arrays.asList(new IntegerParameter("Delta", "delta", -255, 255, 1),
						new AreaParameter("Zone", "area")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.changeLuminosity(input, (int) para.get(0));
				}));

		algorithms.add(new Algorithm("Changement de teinte", "colorFilter",
				Arrays.asList(new IntegerParameter("Teinte", "hue", 0, 359, 1),
						new AreaParameter("Zone", "area")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.colorFilter(input, (int) para.get(0));
				}));

		algorithms.add(new Algorithm("Filtre Moyenneur", "meanFilter", Arrays.asList(
				new IntegerParameter("Taille du filtre", "size", 1, 21, 2),
				new SelectParameter("borderType", "Type de bordure", borderTypes),
				new AreaParameter("Zone", "area")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.meanFilter(input, (int) para.get(0), BorderType.valueOf((String) para.get(1)));
				}));

		algorithms.add(new Algorithm("Filtre Gaussien", "gaussienFilter", Arrays.asList(
				new IntegerParameter("Taille du filtre", "size", 1, 21, 2),
				new DoubleParameter("Sigma", "sigma", 0.1, 2, 0.1),
				new SelectParameter("Type de bordure", "borderType", borderTypes),
				new AreaParameter("Zone", "area")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.gaussienFilter(input, (int) para.get(0), (double) para.get(1),
							BorderType.valueOf((String) para.get(2)));
				}));

		algorithms.add(
				new Algorithm("Détection de contours", "contours", Arrays.asList(new AreaParameter("Zone", "area")),
						(Planar<GrayU8> input, List<Object> para) -> {
							ImageProcessing.gradientImageSobel(input);
						}));

		algorithms.add(new Algorithm("Égalisation d'histogramme", "histogram",
				Arrays.asList(new AreaParameter("Zone", "area")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.histogram(input);
				}));

		algorithms.add(new Algorithm("Filtre négatif", "negative", Arrays.asList(new AreaParameter("Zone", "area")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.negativeFilter(input);
				}));

		algorithms.add(new Algorithm("Filtre sépia", "sepia", Arrays.asList(new AreaParameter("Zone", "area")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.sepiaFilter(input);
				}));

		algorithms.add(new Algorithm("Suppression zone", "deleteArea",
				Arrays.asList(new AreaParameter("Zone", "area", false),
						new SelectParameter("Type de remplissage", "fillingType", fillingTypes)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.deleteArea(input, (Area) para.get(0),
							FillingType.valueOf((String) para.get(1)));
				}));

		algorithms.add(new Algorithm("Filtre de bruit", "noise",
				Arrays.asList(new IntegerParameter("Intensité", "intensity", 1, 100, 1)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.gaussianNoiseFilter(input, (int) para.get(0));
				}));

		algorithms.add(new Algorithm("Rognage", "crop",
				Arrays.asList(new AreaParameter("Zone", "area", false)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.crop(input, (Area) para.get(0));
				}));

		algorithms.add(new Algorithm("Changement de coloration", "changeColoration",
				Arrays.asList(new ColorParameter("Couleur à changer", "colorToChange"),
						new IntegerParameter("Tolerance", "tolerance", 0, 255, 1),
						new SelectParameter("Couleur de remplacement", "colorToReplace", newColors),
						new BooleanParameter("Garder la couleur ?", "keep")),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.changeColoration(input, (String) para.get(0), (int) para.get(1),
							(String) para.get(2), (boolean) para.get(3));
				}));

		algorithms.add(new Algorithm("Saturation", "saturation",
				Arrays.asList(new DoubleParameter("Intensité", "intensity", 0, 1, 0.05)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.saturation(input, (double) para.get(0));
				}));

		algorithms.add(new Algorithm("Vignette", "vignette",
				Arrays.asList(new DoubleParameter("Puissance", "power", 0.05, 4, 0.05)),
				(Planar<GrayU8> input, List<Object> para) -> {
					ImageProcessing.vignette(input, (double) para.get(0));
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
