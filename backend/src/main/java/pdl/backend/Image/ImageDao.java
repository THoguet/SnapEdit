package pdl.backend.Image;

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

import pdl.backend.Dao;

@Repository
public class ImageDao implements Dao<Image> {

	private final static String[] ACCEPTED_EXTENSION = { "jpg", "png", "jpeg" };

	private final Map<Long, Image> images = new HashMap<>();

	public ImageDao() throws IOException {
		this("/images");
	}

	public ImageDao(final String folderPathStr) throws IOException {
		final var folderPath = new ClassPathResource(folderPathStr);
		if (!folderPath.exists())
			throw new ImageResourceMissingException("folder images doesn't not exist");
		final var folder = folderPath.getFile();
		getAllImages(folder);
	}

	private void getAllImages(final File folder) {
		for (final File file : folder.listFiles()) {
			if (file.isDirectory()) {
				getAllImages(file);
			} else {
				String extension = "";

				int i = file.getName().lastIndexOf('.');
				if (i > 0) {
					extension = file.getName().substring(i + 1);
				}
				boolean accepted = false;
				for (final String ext : ACCEPTED_EXTENSION) {
					if (ext.equals(extension)) {
						accepted = true;
						break;
					}
				}
				if (!accepted) {
					continue;
				}
				byte[] fileContent;
				try {
					fileContent = Files.readAllBytes(file.toPath());
					Image img = new Image(file.getName(), fileContent);
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
