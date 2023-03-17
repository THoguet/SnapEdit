package pdl.backend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.image.BufferedImage;

@RestController
public class ImageController {

	@Autowired
	private ObjectMapper mapper;

	private final ImageDao imageDao;

	public ImageController(ImageDao imageDao) {
		this.imageDao = imageDao;
	}

	/**
	 * Retourne une erreur de type APPLICATION_JSON
	 *
	 * @param message Le message d'erreur à afficher
	 * @param status  La valeur de l'erreur
	 * @return Une ResponseEntity
	 */
	public ResponseEntity<?> JSONError(String message, HttpStatus status) {
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("code", Integer.toString(status.value()));
		objectNode.put("message", message);
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(objectNode, httpHeaders, status);
	}

	@DeleteMapping(value = "/images/{id}")
	public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
		var img = imageDao.retrieve(id);
		if (img.isEmpty())
			return JSONError("Image n°" + id + "doesn't exist", HttpStatus.NOT_FOUND);
		imageDao.delete(img.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		String contentType = file.getContentType();
		if (contentType == null
				|| (!contentType.equals(MediaType.IMAGE_JPEG_VALUE) && !contentType.equals(MediaType.IMAGE_PNG_VALUE)))
			return JSONError("Content type " + contentType + "isn't supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
		try {
			Image newImg = new Image(file.getOriginalFilename(), file.getBytes());
			imageDao.create(newImg);
			URI newURI = ServletUriComponentsBuilder.fromCurrentRequest().path("/" + newImg.getId()).build().toUri();
			return ResponseEntity.created(newURI).build();
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
		}
	}

	@DeleteMapping(value = "/images")
	public ResponseEntity<?> deleteImagesList() {
		return JSONError("Method DELETE not allowed for /images", HttpStatus.METHOD_NOT_ALLOWED);
	}

	@GetMapping(value = "/images", produces = "application/json")
	@ResponseBody
	public ArrayNode getImageList() {
		List<Image> images = imageDao.retrieveAll();
		ArrayNode nodes = mapper.createArrayNode();
		for (Image image : images) {
			ObjectNode objectNode = mapper.createObjectNode();
			objectNode.put("id", image.getId());
			objectNode.put("name", image.getName());
			objectNode.put("size", image.getSize());
			objectNode.put("type", image.getMediaType().toString());
			nodes.add(objectNode);
		}
		return nodes;
	}

	@GetMapping(value = "/images/{id}", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE,
			"application/json" })
	public ResponseEntity<?> getImage(@PathVariable("id") long id,
			@RequestParam(required = false) Map<String, String> parameters) {

		// Récupère l'image correspondante à l'ID fourni
		Optional<Image> optImage = imageDao.retrieve(id);
		if (!optImage.isPresent())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		Image image = optImage.get();
		// Si aucun paramètre n'est fourni, renvoie l'image brute
		if (parameters.isEmpty()) {
			InputStream inputStream = new ByteArrayInputStream(image.getData());
			return ResponseEntity.ok().contentType(image.getMediaType())
					.body(new InputStreamResource(inputStream));
		}
		// Vérifie la présence du paramètre "algorithm"
		if (!parameters.containsKey("algorithm")) {
			return JSONError("First parameter should be 'algorithm'", HttpStatus.BAD_REQUEST);
		}

		String algo = parameters.get("algorithm");
		BufferedImage bufImg;
		try {
			bufImg = ImageIO.read(new ByteArrayInputStream(image.getData()));
		} catch (IOException e) {
			return JSONError("There was an internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		Planar<GrayU8> input = ConvertBufferedImage.convertFromPlanar(bufImg, null, true, GrayU8.class);

		// applique l'algo a l'image en fonction des parametres et verifie la validite
		// des parametres
		switch (algo) {
			case "changeLuminosity":
				if (!parameters.containsKey("delta") || parameters.size() != 2)
					return JSONError("Algorithm 'changeLuminosity' requires one parameter 'delta'(integer)",
							HttpStatus.BAD_REQUEST);
				try {
					int i = Integer.parseInt(parameters.get("delta"));
					ImageProcessing.changeLuminosity(input, i);
				} catch (NumberFormatException e) {
					return JSONError("Parameter 'delta' should be an integer", HttpStatus.BAD_REQUEST);
				}
				break;
			case "histogram":
				if (parameters.size() != 1)
					return JSONError("Algorithm 'histogram' doesn't require any parameters", HttpStatus.BAD_REQUEST);
				ImageProcessing.histogram(input);
				break;
			case "colorFilter":
				if (!parameters.containsKey("hue") || parameters.size() != 2)
					return JSONError("Algorithm 'colorFilter' requires one parameter 'hue'(integer)",
							HttpStatus.BAD_REQUEST);
				try {
					int i = Integer.parseInt(parameters.get("hue"));
					if (i < 0) {
						return JSONError("Parameter 'hue' should be an integer between 0 and 360",
								HttpStatus.BAD_REQUEST);
					}
					ImageProcessing.colorFilter(i, input);
					// hue entre 0 et 360?
				} catch (NumberFormatException e) {
					return JSONError("Parameter 'hue' should be an integer between 0 and 360",
							HttpStatus.BAD_REQUEST);
				}
				break;
			case "meanFilter":
				if (!parameters.containsKey("size") || parameters.size() != 2)
					return JSONError("Algorithm 'meanFilter' requires one parameter 'size'(positive odd integer)",
							HttpStatus.BAD_REQUEST);
				try {
					int i = Integer.parseInt(parameters.get("size"));
					if (i % 2 == 0)
						return JSONError("Parameter 'size' should be a positive odd integer",
								HttpStatus.BAD_REQUEST);
					Planar<GrayU8> clone = input.clone();
					ImageProcessing.meanFilter(clone, input, i);
				} catch (NumberFormatException e) {
					return JSONError("Parameter 'size' should be a positive odd integer",
							HttpStatus.BAD_REQUEST);
				}
				break;
			case "gaussienFilter":
				if (/* !parameters.containsKey("size") || */ parameters.size() != 1)
					return JSONError("Algorithm 'gaussienFilter' requires no parameters", HttpStatus.BAD_REQUEST);
				try {
					// int i = Integer.parseInt(parameters.get("size"));
					// if (i % 2 == 0)
					// return JSONError("Parameter 'size' should be a positive odd integer",
					// HttpStatus.BAD_REQUEST);
					Planar<GrayU8> clone = input.clone();
					ImageProcessing.gaussienFilter(clone, input, 5);
				} catch (NumberFormatException e) {
					return JSONError("Parameter 'size' should be a positive odd integer",
							HttpStatus.BAD_REQUEST);
				}
				break;
			case "contours":
				if (parameters.size() != 1)
					return JSONError("Algorithm 'contours' doesn't require any parameters", HttpStatus.BAD_REQUEST);
				Planar<GrayU8> clone = input.clone();
				ImageProcessing.gradientImageSobel(clone, input);
				break;
			default:
				return JSONError("Requested algorithm doesn't exist", HttpStatus.BAD_REQUEST);

		}

		// transforme l'image en quelque chose que le navigateur puisse lire
		bufImg = ConvertBufferedImage.convertTo_U8(input, null, true);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufImg, image.getMediaType().getSubtype(), baos);
		} catch (IOException e) {
			return JSONError("There was an internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		byte[] imageData = baos.toByteArray();
		InputStream inputStream = new ByteArrayInputStream(
				imageData);
		return ResponseEntity.ok().contentType(image.getMediaType()).body(new InputStreamResource(inputStream));
	}
}