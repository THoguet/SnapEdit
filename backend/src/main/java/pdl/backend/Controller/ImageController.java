package pdl.backend.Controller;

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
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import pdl.backend.Algorithm.Algorithm;
import pdl.backend.Algorithm.Parameters.Parameter;
import pdl.backend.Image.Image;
import pdl.backend.Image.ImageDao;
import pdl.backend.Image.ImageProcessing;

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

	/**
	 * Supprime une image
	 *
	 * @param id le matricule de l'image à supprimer
	 * @return Un code ResponseEntity
	 */
	@DeleteMapping(value = "/images/{id}")
	public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
		var img = imageDao.retrieve(id);
		if (img.isEmpty())
			return JSONError("Image n°" + id + "doesn't exist", HttpStatus.NOT_FOUND);
		imageDao.delete(img.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Poste une image
	 *
	 * @param file               l'image à ajouter
	 * @param redirectAttributes
	 * @return Une ResponseEntity
	 */
	@PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		String contentType = file.getContentType();
		if (contentType == null
				|| (!contentType.equals(MediaType.IMAGE_JPEG_VALUE) && !contentType.equals(MediaType.IMAGE_PNG_VALUE)))
			return JSONError("Content type " + contentType + "is not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
		try {
			Image newImg = new Image(file.getOriginalFilename(), file.getBytes(), null);
			imageDao.create(newImg);
			URI newURI = ServletUriComponentsBuilder.fromCurrentRequest().path("/" + newImg.getId()).build().toUri();
			return ResponseEntity.created(newURI).build();
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
		}
	}

	/**
	 * Supprime toutes les images postées
	 *
	 * @return Une ResponseEntity
	 */
	@DeleteMapping(value = "/images")
	public ResponseEntity<?> deleteImagesList() {
		return JSONError("Method DELETE not allowed for /images", HttpStatus.METHOD_NOT_ALLOWED);
	}

	/**
	 * Récupère toutes les images
	 *
	 * @return un ArrayNode contenant toutes les images à récupérer
	 */
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
			objectNode.put("filtered", image.getParentId());
			nodes.add(objectNode);
		}
		return nodes;
	}

	public BorderType getBorderType(String borderType) {
		switch (borderType) {
			case "EXTENDED":
				return BorderType.EXTENDED;
			case "REFLECT":
				return BorderType.REFLECT;
			case "REFLECT_101":
				return BorderType.NORMALIZED;
			case "WRAP":
				return BorderType.WRAP;
			case "ZERO":
				return BorderType.ZERO;
			default:
				return null;
		}
	}

	/**
	 * récupère une image et la modifie si un algorithme est renseigné
	 *
	 * @param id         le matricule de l'image à récupérer
	 * @param parameters une String qui contient le reste des paramètres :
	 *                   l'algorithme à utiliser ainsi que ses paramètres
	 * @return Une ResponseEntity
	 */
	@GetMapping(value = "/images/{id}", produces = { "application/json" })
	public ResponseEntity<?> getImage(@PathVariable("id") long id,
			@RequestParam(required = false) Map<String, String> parameters) {

		// Récupère l'image correspondante à l'ID fourni
		Optional<Image> optImage = imageDao.retrieve(id);
		if (!optImage.isPresent())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		Image imageToFilter = optImage.get();
		// Si aucun paramètre n'est fourni, renvoie l'image brute
		if (parameters.isEmpty()) {
			InputStream inputStream = new ByteArrayInputStream(imageToFilter.getData());
			return ResponseEntity.ok().contentType(imageToFilter.getMediaType())
					.body(new InputStreamResource(inputStream));
		}
		final List<Algorithm> algos = AlgorithmController.ALGORITHMS;
		// Vérifie la présence du paramètre "algorithm"
		if (!parameters.containsKey("algorithm")) {
			return JSONError("First parameter should be 'algorithm'", HttpStatus.BAD_REQUEST);
		}

		String algo = parameters.get("algorithm");
		BufferedImage bufImg;
		try {
			bufImg = ImageIO.read(new ByteArrayInputStream(imageToFilter.getData()));
		} catch (IOException e) {
			return JSONError("There was an internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		Planar<GrayU8> input = ConvertBufferedImage.convertFromPlanar(bufImg, null, true, GrayU8.class);

		for (Algorithm a : algos) {
			if (a.getPath().equals(algo)) {
				for (Parameter p : a.getParameters()) {
					if (!parameters.containsKey(p.getPath())) {
						return JSONError("Parameter '" + p.getPath() + "' is missing", HttpStatus.BAD_REQUEST);
					}
					try {
						p.setValue(parameters.get(p.getPath()));
					} catch (Exception e) {
						return JSONError("Parameter '" + p.getPath() + "' is invalid", HttpStatus.BAD_REQUEST);
					}
				}
				try {
					a.apply(input);
				} catch (Exception e) {
					return JSONError("There was an internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
				}
				break;
			}
		}

		// transforme l'image en quelque chose que le navigateur puisse lire
		bufImg = ConvertBufferedImage.convertTo_U8(input, null, true);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufImg, imageToFilter.getMediaType().getSubtype(), baos);
		} catch (IOException e) {
			return JSONError("There was an internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		byte[] imageData = baos.toByteArray();
		Image newImg;
		try {
			if (imageToFilter.isFiltered()) {
				imageDao.delete(imageToFilter);
				newImg = new Image(imageToFilter.getRealName(), imageData, imageToFilter.getParent());
			} else {
				final Image child = imageToFilter.getChild();
				if (child != null) {
					imageDao.delete(child);
				}
				newImg = new Image(imageToFilter.getRealName(), imageData, imageToFilter);
			}
		} catch (IOException e) {
			return JSONError("There was an internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (imageToFilter.isFiltered()) {
			imageDao.delete(imageToFilter);
		}
		newImg.setFiltered(true);
		imageDao.create(newImg);
		return ResponseEntity.ok().body(newImg.getId());
	}

	@GetMapping(value = "/images/progress", produces = { "application/json" })
	public ResponseEntity<?> getProgress() {
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("progress", ImageProcessing.getProgress());
		return ResponseEntity.ok().body(objectNode);
	}
}
