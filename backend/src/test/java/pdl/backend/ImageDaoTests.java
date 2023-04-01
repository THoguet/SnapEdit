package pdl.backend;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pdl.backend.Image.ImageDao;

public class ImageDaoTests {

	@Test
	@DisplayName("ImageDao should throw an exception when the image folder is missing")
	public void exceptionImageFolderNotFound() throws Exception {
		assertThrows(IOException.class, () -> {
			new ImageDao("testing");
		});
	}

	@Test
	@DisplayName("ImageDao should have all images of the folder / subfolders and should ignore non (JPG/PNG) files")
	public void initImageDao() throws Exception {
		ImageDao imageDao = new ImageDao("imagesTest");
		var listImages = imageDao.retrieveAll();
		assert (listImages.size() == 5);
	}
}