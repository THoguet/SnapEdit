package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

public class Image {
	private static Long count = Long.valueOf(0);
	private final Long id;
	private String name;
	private final byte[] data;
	private final MediaType mediaType;
	private final int width;
	private final int height;
	private final int nbColors;

	public Image(final String name, final byte[] data) throws IOException {
		id = count++;
		this.name = name;
		this.data = data;
		this.mediaType = MediaType.valueOf(URLConnection.guessContentTypeFromName(name));
		BufferedImage bufImg = ImageIO.read(new ByteArrayInputStream(data));
		this.width = bufImg.getWidth();
		this.height = bufImg.getHeight();
		this.nbColors = bufImg.getColorModel().getNumComponents();

	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public byte[] getData() {
		return data;
	}

	public static Long getCount() {
		return count;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getNbColors() {
		return nbColors;
	}

	public String getSize() {
		return this.getWidth() + "*" + this.getHeight() + "*" + this.getNbColors();
	}
}
