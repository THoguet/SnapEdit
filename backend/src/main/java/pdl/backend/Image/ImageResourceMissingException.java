package pdl.backend.Image;

import java.io.FileNotFoundException;

public class ImageResourceMissingException extends FileNotFoundException {
	public ImageResourceMissingException(String message) {
		super(message);
	}
}
