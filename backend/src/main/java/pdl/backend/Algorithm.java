package pdl.backend;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Algorithm {
	private final String name;
	private final String pass;
	private final ArrayList<Parameter> parameters;

	public Algorithm(String name, String pass) {
		this.name = name;
		this.pass = pass;
		this.parameters = new ArrayList<>();
		switch (pass) {
			case "changeLuminosity":
				Parameter param1 = new Parameter("delta", "", -255, 255, false);
				this.parameters.add(param1);
				break;
			case "histogram":
				break;
			case "colorFilter":
				Parameter param2 = new Parameter("hue", "Teinte", 0, 360, false);
				this.parameters.add(param2);
				break;
			case "meanFilter":
				Parameter param3 = new Parameter("size", "Taille du filtre", 1, 101, true);
				this.parameters.add(param3);
				break;
			case "gaussienFilter":
				Parameter param4 = new Parameter("size", "Taille du filtre", 1, 101, true);
				this.parameters.add(param4);
				break;
			case "contours":
				break;
			default:
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pass");

		}
	}

	public String getName() {
		return this.name;
	}

	public String getPass() {
		return this.pass;
	}

	public ArrayList<Parameter> getParameters() {
		return this.parameters;
	}
}
