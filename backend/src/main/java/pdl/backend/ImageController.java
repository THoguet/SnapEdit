package pdl.backend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import java.awt.image.BufferedImage;

@RestController
public class ImageController {

    @Autowired
    private ObjectMapper mapper;

    private final ImageDao imageDao;

    public ImageController(ImageDao imageDao) {
        this.imageDao = imageDao;
    }

    @DeleteMapping(value = "/images/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

        Optional<Image> image = imageDao.retrieve(id);

        if (image.isPresent()) {
            imageDao.delete(image.get());
            return new ResponseEntity<>("Image id=" + id + " deleted.", HttpStatus.OK);
        }
        return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/images")
    public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals(MediaType.IMAGE_JPEG.toString())
                && !contentType.equals(MediaType.IMAGE_PNG.toString())) {
            return new ResponseEntity<>("Only JPEG/PNG file format supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        try {
            imageDao.create(new Image(file.getOriginalFilename(), file.getBytes()));
        } catch (IOException e) {
            return new ResponseEntity<>("Failure to read file", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("Image uploaded", HttpStatus.CREATED);
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

    @GetMapping(value = "/images/{id}", produces = { MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<?> getImage(@PathVariable("id") long id,
            @RequestParam(required = false) Map<String, String> parameters) {

        Optional<Image> image = imageDao.retrieve(id);
        if (image.isPresent()) {
            if (parameters.isEmpty()) {
                InputStream inputStream = new ByteArrayInputStream(image.get().getData());
                return ResponseEntity.ok().contentType(image.get().getMediaType())
                        .body(new InputStreamResource(inputStream));

            }
            if (parameters.containsKey("algorithm")) {
                if (parameters.get("algorithm").equals("changeLuminosity") && parameters.containsKey("delta")) {
                    BufferedImage bufImg;
                    try {
                        bufImg = ImageIO.read(new ByteArrayInputStream(image.get().getData()));
                    } catch (IOException e) {
                        return new ResponseEntity<>("Image not readable",
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    Planar<GrayU8> input = ConvertBufferedImage.convertFromPlanar(bufImg, null,
                            true, GrayU8.class);
                    ImageProcessing.changeLuminosity(input, Integer.parseInt(parameters.get("delta")));
                    bufImg = ConvertBufferedImage.convertTo_U8(input, null, true);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufImg, image.get().getMediaType().getSubtype(), baos);

                    } catch (IOException e) {
                        return new ResponseEntity<>("Image couldn't be converted",
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    byte[] imageData = baos.toByteArray();

                    InputStream inputStream = new ByteArrayInputStream(imageData);
                    return ResponseEntity.ok().contentType(image.get().getMediaType())
                            .body(new InputStreamResource(inputStream));
                }
            }

        }
        return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
    }
}
