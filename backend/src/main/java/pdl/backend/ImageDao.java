package pdl.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao implements Dao<Image> {

    private final Map<Long, Image> images = new HashMap<>();

    public ImageDao() {
        try {
            final File folder = new File("backend/src/main/resources/images");
            getAllImages(folder);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        System.out.println("IMAGEEEEEEEEEEEEEEs");
        for (Image img : retrieveAll()) {
            System.out.println(img.getName());
        }
        System.out.println("ENNNNNNNNNNNNNNd");

    }

    public void getAllImages(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getAllImages(fileEntry);
            } else {
                final ClassPathResource imgFile = new ClassPathResource(fileEntry.getName());
                String extension = "";

                int i = fileEntry.getName().lastIndexOf('.');
                if (i > 0) {
                    extension = fileEntry.getName().substring(i + 1);
                }
                if (!extension.equals("jpg") && !extension.equals("pgn")) {
                    continue;
                }
                byte[] fileContent;
                try {
                    fileContent = Files.readAllBytes(imgFile.getFile().toPath());
                    Image img = new Image(imgFile.getFilename(), fileContent);
                    images.put(img.getId(), img);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Optional<Image> retrieve(final long id) {
        return Optional.ofNullable(images.get(id));
    }

    @Override
    public List<Image> retrieveAll() {
        return new ArrayList<Image>(images.values());
    }

    @Override
    public void create(final Image img) {
        images.put(img.getId(), img);
    }

    @Override
    public void update(final Image img, final String[] params) {
        img.setName(Objects.requireNonNull(params[0], "Name cannot be null"));

        images.put(img.getId(), img);
    }

    @Override
    public void delete(final Image img) {
        images.remove(img.getId());
    }
}
