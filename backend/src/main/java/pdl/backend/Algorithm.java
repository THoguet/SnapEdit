package pdl.backend;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Algorithm {
	private final String name;
	private final String path;
	private final ArrayList<Parameter> parameters;

	public Algorithm(String name, String path) {
		this.name = name;
		this.path = path;
		this.parameters = new ArrayList<>();
		switch (path) {
			case "changeLuminosity":
				Parameter param1 = new Parameter("delta", "delta", -255, 255, 1);
				this.parameters.add(param1);
				break;
			case "histogram":
				break;
			case "colorFilter":
				Parameter param2 = new Parameter("hue", "Teinte", 0, 359, 1);
				this.parameters.add(param2);
				break;
			case "meanFilter":
				Parameter param3 = new Parameter("size", "Taille du filtre", 1, 101, 2);
				this.parameters.add(param3);
				break;
			case "gaussienFilter":
				// TODO
				// Parameter param4 = new Parameter("size", "Taille du filtre", 1, 101, 2);
				// this.parameters.add(param4);
				break;
			case "contours":
				break;
			default:
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid path");

		}
	}

	public String getName() {
		return this.name;
	}

	public String getPath() {
		return this.path;
	}

	public ArrayList<Parameter> getParameters() {
		return this.parameters;
	}
}
