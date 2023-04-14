package pdl.backend.Image;

import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import pdl.backend.Area;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import boofcv.concurrency.BoofConcurrency;
import java.util.concurrent.atomic.AtomicInteger;

public final class ImageProcessing {

	private static final AtomicInteger counter = new AtomicInteger(-1);
	private static int nbSteps = 0;

	private ImageProcessing() {
	}

	public static int getProgress() {
		if (nbSteps == 0)
			return -1;
		if (counter.get() == -1)
			return -1;
		return Math.round(((float) ImageProcessing.counter.get() / (float) nbSteps) * 100);
	}

	/**
	 * 
	 * Retourne les valeurs r, g et b d'un pixel d'une image placés en paramètres
	 * 
	 * @param input L'image
	 * @param x     La ligne
	 * @param y     La colonne
	 */
	public static int[] getRGBValue(Planar<GrayU8> input, int x, int y) {
		int[] rgb = new int[3];
		for (int i = 0; i < rgb.length; i++) {
			rgb[i] = input.getBand(i).get(x, y);
		}
		return rgb;
	}

	/**
	 * 
	 * Colore le pixel donné de l'image donnée avec les valeurs rgb données
	 * 
	 * @param input L'image
	 * @param x     La ligne
	 * @param y     La colonne
	 * @param rgb   Tableau de taille 3 avec les valeurs de r, g, et b
	 */
	public static void setRGBValue(Planar<GrayU8> input, int x, int y, int[] rgb) {
		for (int i = 0; i < rgb.length; i++) {
			input.getBand(i).set(x, y, rgb[i]);
		}
	}

	/**
	 * 
	 * Convertit une image en couleur en une image en niveaux de gris
	 * 
	 * @param input l'image d'entrée en couleur
	 */
	public static void convertColorToGray(Planar<GrayU8> input) {
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				int[] rgb = getRGBValue(input, x, y);
				int gray = (int) (0.3 * rgb[0] + 0.59 * rgb[1] + 0.11 * rgb[2]);
				setRGBValue(input, x, y, new int[] { gray, gray, gray });
			}
		});
	}

	/**
	 * 
	 * Permet de changer la luminosité d'une image
	 * 
	 * @param input L'image pour laquelle la luminosité doit être
	 *              modifiée.
	 * @param delta La valeur du décalage à appliquer à la luminosité de l'image.
	 */
	public static void changeLuminosity(Planar<GrayU8> input, int delta) {
		nbSteps = input.height;
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				int[] rgb = getRGBValue(input, x, y);
				for (int i = 0; i < rgb.length; i++) {
					if (rgb[i] + delta > 255) {
						rgb[i] = 255;
					} else if (rgb[i] + delta < 0) {
						rgb[i] = 0;
					} else {
						rgb[i] += delta;
					}
				}
				setRGBValue(input, x, y, rgb);
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
	}

	/**
	 * Étale l'histogramme d'une pour une meilleure
	 * répartition des couleurs
	 * 
	 * @param input L'image à étaler.
	 */
	public static void histogram(Planar<GrayU8> input) {
		nbSteps = input.height;
		int h[] = new int[256];
		Planar<GrayU8> grayInput = input.clone();
		convertColorToGray(grayInput);
		// On remplit le tableau avec le nombre de pixels pour chaque niveau de
		// gris
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				int gl = grayInput.getBand(0).get(x, y);
				h[gl]++;
			}
		});

		// On remplit le tableau avec la cumulée de l'histogramme
		int C[] = new int[256];
		C[0] = h[0];
		for (int i = 1; i < 256; i++) {
			C[i] = C[i - 1] + h[i];
		}

		int LUT[] = new int[256];
		// Création de la table de LUT qui stocke la transformation de l'histogramme
		// pour chaque niveau de gris
		for (int i = 0; i < 256; i++) {
			LUT[i] = C[i] * 255 / (input.height * input.width);
		}

		// Transformation de l'image en utilisant la table de LUT
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				for (int j = 0; j < input.getNumBands(); j++) {
					int val = input.getBand(j).get(x, y);
					input.getBand(j).set(x, y, LUT[val]);
				}
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
	}

	/**
	 * 
	 * Passe un pixel de l'espace de couleurs RGB à l'espace de couleurs HSV :
	 * 
	 * @param r   la composante rouge du pixel
	 * @param g   la composante verte du pixel
	 * @param b   la composante bleue du pixel
	 * @param hsv le tableau de sortie contenant les valeurs de h, s et v
	 */
	public static void rgbToHsv(int r, int g, int b, float[] hsv) {
		int max = Math.max(r, Math.max(g, b));
		int min = Math.min(r, Math.min(g, b));

		// Trouver valeur de h
		if (max == min) {
			hsv[0] = 0;
		} else if (max == r) {
			hsv[0] = (60 * (g - b) / (max - min) + 360) % 360;
		} else if (max == g) {
			hsv[0] = 60 * (b - r) / (max - min) + 120;
		} else {
			hsv[0] = 60 * (r - g) / (max - min) + 240;
		}
		// Trouver valeur de s
		if (max == 0) {
			hsv[1] = 0;
		} else {
			hsv[1] = 1 - (min / max);
		}
		// Trouver valeur de v
		hsv[2] = max;
	}

	/**
	 * 
	 * Passe un pixel de l'espace de couleurs HSV à l'espace de couleurs RGB :
	 * 
	 * @param h   la composante hue du pixel
	 * @param s   la composante saturation du pixel
	 * @param v   la composante value du pixel
	 * @param rgb le tableau de sortie contenant les valeurs de r, g et b
	 */
	public static void hsvToRgb(float h, float s, float v, int[] rgb) {
		int t_i = (int) Math.floor(h / 60) % 6;
		float f = h / 60 - t_i;
		float l = v * (1 - s);
		float m = v * (1 - f * s);
		float n = v * (1 - (1 - f) * s);
		switch (t_i) {
			case 0:
				rgb[0] = (int) Math.round(v);
				rgb[1] = (int) Math.round(n);
				rgb[2] = (int) Math.round(l);
				break;
			case 1:
				rgb[0] = (int) Math.round(m);
				rgb[1] = (int) Math.round(v);
				rgb[2] = (int) Math.round(l);
				break;
			case 2:
				rgb[0] = (int) Math.round(l);
				rgb[1] = (int) Math.round(v);
				rgb[2] = (int) Math.round(n);
				break;
			case 3:
				rgb[0] = (int) Math.round(l);
				rgb[1] = (int) Math.round(m);
				rgb[2] = (int) Math.round(v);
				break;
			case 4:
				rgb[0] = (int) Math.round(n);
				rgb[1] = (int) Math.round(l);
				rgb[2] = (int) Math.round(v);
				break;
			case 5:
				rgb[0] = (int) Math.round(v);
				rgb[1] = (int) Math.round(l);
				rgb[2] = (int) Math.round(m);
				break;
		}

	}

	/**
	 * Applique une teinte à une image.
	 *
	 * @param hue   La valeur de la teinte à appliquer
	 * @param input L'image à modifier
	 */
	public static void colorFilter(Planar<GrayU8> input, int hue) {
		if (hue < 0 || hue > 360) {
			throw new IllegalArgumentException("Hue doit être compris entre 0 et 360");
		}
		nbSteps = input.height;
		BoofConcurrency.loopFor(0, input.height, j -> {
			for (int i = 0; i < input.width; i++) {
				float[] hsv = new float[3];
				int[] pixelValue = getRGBValue(input, i, j);
				rgbToHsv(pixelValue[0], pixelValue[1], pixelValue[2], hsv);
				hsv[0] = hue;
				int[] rgb = new int[3];
				hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);
				setRGBValue(input, i, j, rgb);
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
	}

	/**
	 * 
	 * Calcule et stocke dans output l'image de la norme du gradient
	 * calculé sur input par convolution avec les filtres de Sobel.
	 * 
	 * @param input L'image d'entrée en niveaux de gris.
	 */
	public static void gradientImageSobel(Planar<GrayU8> input) {
		nbSteps = input.height;
		Planar<GrayU8> output = input.createSameShape();
		int h1[][] = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
		int h2[][] = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		int half = h1.length / 2;
		Planar<GrayU8> inputGray = input.clone();
		convertColorToGray(inputGray);
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				if (y < half || x < half || y >= input.height - half || x >= input.width - half) {
					int gl = inputGray.getBand(0).get(x, y);
					int[] rgb = { gl, gl, gl };
					setRGBValue(output, x, y, rgb);
					continue;
				}
				int currentJ = 0;
				int currentI = 0;
				int Gx = 0;
				int Gy = 0;
				for (int j = y - half; j <= y + half; j++) {
					for (int i = x - half; i <= x + half; i++) {
						Gx += inputGray.getBand(0).get(i, j) * h1[currentJ][currentI];
						Gy += inputGray.getBand(0).get(i, j) * h2[currentJ][currentI];
						currentI++;
					}
					currentI = 0;
					currentJ++;
				}
				int value = (int) Math.sqrt(Gx * Gx + Gy * Gy);
				if (value < 0)
					value = 0;
				if (value > 255)
					value = 255;
				int[] rgb = { value, value, value };
				setRGBValue(output, x, y, rgb);
			}
			counter.incrementAndGet();
		});
		input.setTo(output);
		counter.set(-1);
	}

	/**
	 * Applique un filtre moyenneur de taille indiquée à l'image
	 * passée en paramètre et stocke le résultat dans l'image de sortie.
	 * 
	 * @param input      image d'entrée
	 * @param size       taille du filtre à appliquer (doit être impaire)
	 * @param borderType type de bord à utiliser pour la convolution
	 */
	public static void meanFilter(Planar<GrayU8> input, int size, BorderType borderType) {
		// Vérification que la taille est impaire
		if (size % 2 == 0 || size < 0) {
			throw new IllegalArgumentException(
					"La taille doit être impaire et positive");
		}
		int[][] kernel = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				kernel[i][j] = 1;
			}
		}
		convolution(input, kernel, borderType);
	}

	/**
	 * 
	 * Applique un filtre gaussien de taille et de valeur données sur une image et
	 * stocke le résultat dans l'image de sortie.
	 * 
	 * @param input      L'image d'entrée
	 * @param size       La taille du filtre à appliquer (doit être impaire)
	 * @param sigma      La valeur du sigma à utiliser pour le filtre gaussien.
	 * @param borderType Le type de bord à utiliser pour la convolution
	 */
	public static void gaussienFilter(Planar<GrayU8> input, int size, double sigma, BorderType borderType) {
		if (size % 2 == 0 || size < 0 || sigma <= 0) {
			throw new IllegalArgumentException(
					"La taille doit être impaire et positive et sigma doit être strictement positif");
		}
		double[][] kernel = new double[size][size];
		int half = size / 2;
		double sum = 0;
		for (int x = -half; x <= half; ++x) {
			for (int y = -half; y <= half; ++y) {
				double gaussianValue = 1 / (2 * Math.PI * sigma * sigma)
						* Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
				kernel[x + half][y + half] = gaussianValue;
				sum += gaussianValue;
			}
		}
		// On récupère le pourcentage de chaque valeur par rapport à la somme
		for (int x = -half; x <= half; ++x) {
			for (int y = -half; y <= half; ++y) {
				kernel[x + half][y + half] = kernel[x + half][y + half] / sum;
			}
		}

		int[][] kernelInt = new int[size][size];
		double factor = 1 / kernel[0][0];
		// Si le facteur est trop grand (la valeur du coin est très très petite par
		// rapport au reste), on le fixe à 1 million
		if (factor > 1000000) {
			factor = 1000000;
		}
		// normalisation
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				kernelInt[x][y] = (int) (kernel[x][y] * factor);
			}
		}
		convolution(input, kernelInt, borderType);
	}

	public static int[][] createGaussienKernel(int size, double sigma) {
		// Vérification que la taille est impaire
		if (size % 2 == 0 || size < 0) {
			throw new IllegalArgumentException("La taille doit être impaire et positive");
		}
		double[][] kernel = new double[size][size];
		int half = size / 2;
		double sum = 0;
		for (int x = -half; x <= half; ++x) {
			for (int y = -half; y <= half; ++y) {
				double gaussianValue = 1 / (2 * Math.PI * sigma * sigma)
						* Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
				kernel[x + half][y + half] = gaussianValue;
				sum += gaussianValue;
			}
		}
		// We get the percentage of each value compared to the sum of all values
		for (int x = -half; x <= half; ++x) {
			for (int y = -half; y <= half; ++y) {
				kernel[x + half][y + half] = kernel[x + half][y + half] / sum;
			}
		}

		int[][] kernelInt = new int[size][size];
		double factor = 1 / kernel[0][0];
		// If the factor is too big (corner value is very very small compared to all
		// other values), we need to reduce it
		if (factor > 1000000) {
			factor = 1000000;
		}
		// normalisation
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				kernelInt[x][y] = (int) (kernel[x][y] * factor);
			}
		}
		return kernelInt;
	}

	/**
	 * 
	 * Effectue une convolution sur une image avec le noyau
	 * donné et stocke le résultat dans l'image de sortie.
	 * 
	 * @param input      L'image d'entrée
	 * @param kernel     Le noyau de convolution à utiliser.
	 * @param borderType Le type de bord à utiliser pour la convolution
	 */
	public static void convolution(Planar<GrayU8> input, int[][] kernel, BorderType borderType) {
		nbSteps = input.height;
		Planar<GrayU8> copy = input.clone();
		int size = kernel.length;
		int half = kernel.length / 2;
		int coefs = 0;
		// Calcul du nombre total de coefficients dans le noyau
		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[i].length; j++) {
				coefs += kernel[i][j];
			}
		}
		final int totalCoefs = coefs;
		// Parcours de l'image d'entrée et application de la convolution
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				// On traite d'abord les bords
				if (y < half || x < half || y >= input.height - half || x >= input.width -
						half) {
					if (borderType == BorderType.SKIP) {
						setRGBValue(input, x, y, getRGBValue(copy, x, y));
					} else if (borderType == BorderType.ZERO) {
						setRGBValue(input, x, y, new int[] { 0, 0, 0 });
					} else {
						int totalValue[] = new int[3];
						int coefsUsed = 0;
						for (int j = y - half; j < y + half + 1; j++) {
							int tmpj = j;
							if (j < 0 || j >= input.height) {
								if (borderType == BorderType.EXTENDED) {
									tmpj = j < 0 ? 0 : input.height - 1;
								} else if (borderType == BorderType.NORMALIZED) {
									continue;
								} else if (borderType == BorderType.REFLECT) {
									tmpj = j < 0 ? -j : (input.height - 1) - (j - (input.height - 1));
								} else if (borderType == BorderType.WRAP) {
									tmpj = j < 0 ? j + (input.height - 1) : (j - (input.height - 1));
								}
							}
							for (int i = x - size / 2; i < x + size / 2 + 1; i++) {
								int tmpi = i;
								if (i < 0 || i >= input.width) {
									if (borderType == BorderType.EXTENDED) {
										tmpi = i < 0 ? 0 : input.width - 1;
									} else if (borderType == BorderType.NORMALIZED) {
										continue;
									} else if (borderType == BorderType.REFLECT) {
										tmpi = i < 0 ? -i : (input.width - 1) - (i - (input.width - 1));
									} else if (borderType == BorderType.WRAP) {
										tmpi = i < 0 ? i + (input.width - 1) : (i - (input.width - 1));
									}
								}
								for (int k = 0; k < totalValue.length; k++) {
									totalValue[k] += copy.getBand(k).get(tmpi, tmpj)
											* kernel[i + (-x + half)][j + (-y + half)];
								}
								coefsUsed += kernel[i + (-x + half)][j + (-y + half)];
							}
						}
						for (int k = 0; k < totalValue.length; k++) {
							totalValue[k] /= coefsUsed;
						}
						setRGBValue(input, x, y, totalValue);
					}
					continue;
				}
				int currentJ = 0;
				int currentI = 0;
				int totalValue[] = new int[3];
				// Parcours du voisinage défini par le noyau
				for (int j = y - half; j <= y + half; j++) {
					for (int i = x - half; i <= x + half; i++) {
						for (int k = 0; k < totalValue.length; k++) {
							totalValue[k] += copy.getBand(k).get(i, j) * kernel[currentJ][currentI];
						}
						currentI++;
					}
					currentI = 0;
					currentJ++;
				}
				for (int k = 0; k < totalValue.length; k++) {
					totalValue[k] /= totalCoefs;
				}
				setRGBValue(input, x, y, totalValue);
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
	}

	/**
	 * Inverse les couleurs de l'image donnée en paramètre.
	 * 
	 * @param input L'image à inverser
	 */
	public static void negativeFilter(Planar<GrayU8> input) {
		nbSteps = input.height;
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				for (int j = 0; j < input.getNumBands(); j++) {
					int gl = input.getBand(j).get(x, y);
					input.getBand(j).set(x, y, 255 - gl);
				}
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
	}

	/**
	 * Effectue un filtre de type sépia sur l'image donnée en paramètre.
	 * 
	 * @param input L'image à modifier
	 */
	public static void sepiaFilter(Planar<GrayU8> input) {
		if (input.getNumBands() == 1) {
			return;
		}
		nbSteps = input.height;
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				int[] rgb = getRGBValue(input, x, y);
				int r = rgb[0];
				int g = rgb[1];
				int b = rgb[2];
				int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
				int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
				int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);
				if (tr > 255) {
					tr = 255;
				}
				if (tg > 255) {
					tg = 255;
				}
				if (tb > 255) {
					tb = 255;
				}
				setRGBValue(input, x, y, new int[] { tr, tg, tb });
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
	}

	public static enum FillingType {
		SKIP, CONVOLUTION, LEFT, RIGHT, TOP, BOTTOM
	}

	/**
	 * Donne une valeur de pixel unique en fonction de sa position dans l'image afin
	 * de créer une map des pixels à traiter
	 * 
	 * @param i coordonnée x du pixel
	 * @param j coordonnée y du pixel
	 * @return valeur unique du pixel
	 */
	public static int mapValue(int i, int j) {
		return j + (i + j) * (i + j + 1) / 2;
	}

	/**
	 * Indique si le pixel doit être pris en compte lors de la convolution
	 * 
	 * @param input image d'entrée
	 * @param x     coordonnée x du pixel
	 * @param y     coordonnée y du pixel
	 * @param xMin  coordonnée x minimale de la zone
	 * @param xMax  coordonnée x maximale de la zone
	 * @param yMin  coordonnée y minimale de la zone
	 * @param yMax  coordonnée y maximale de la zone
	 * @param map   map des pixels à traiter
	 * @return
	 */
	public static boolean isInImage(Planar<GrayU8> input, int x, int y, int xMin, int xMax, int yMin, int yMax,
			Map<Integer, Boolean> map) {
		if (x < 0 || x >= input.width || y < 0 || y >= input.height) {
			// outside the image
			return false;
		}
		if (x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
			// inside the zone
			if (map.get(mapValue(x, y))) {
				return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * Donne la valeur de convolution d'un pixel
	 * 
	 * @param input  l'image d'entrée
	 * @param x      coordonnée x du pixel
	 * @param y      coordonnée y du pixel
	 * @param xMin   coordonnée x minimale de la zone
	 * @param xMax   coordonnée x maximale de la zone
	 * @param yMin   coordonnée y minimale de la zone
	 * @param yMax   coordonnée y maximale de la zone
	 * @param kernel le noyau de convolution
	 * @param map    la map des pixels de la zone
	 * @return la valeur rgb du pixel
	 */
	public static int[] getConvolutionValue(Planar<GrayU8> input, int x, int y, int xMin, int xMax, int yMin, int yMax,
			int[][] kernel, Map<Integer, Boolean> map) {
		int[] rgb = new int[3];
		int half = kernel.length / 2;
		int values[] = new int[3];
		int coefs = 0;
		for (int i = x - half; i <= x + half; i++) {
			for (int j = y - half; j <= y + half; j++) {
				if (isInImage(input, i, j, xMin, xMax, yMin, yMax, map)) {
					rgb = getRGBValue(input, i, j);
					for (int k = 0; k < 3; k++) {
						values[k] += rgb[k] * kernel[i - x + half][j - y + half];
					}
					coefs += kernel[i - x + half][j - y + half];

				}
			}
		}
		for (int k = 0; k < 3; k++) {
			values[k] /= coefs;
		}
		return values;
	}

	/**
	 * 
	 * Retire une zone de l'image en utilisant la méthode donnée en paramètre.
	 * 
	 * @param input   L'image d'entrée
	 * @param output  L'image de sortie
	 * @param area    Les coordonnées de la zone à supprimer
	 * @param filling Le type de remplissage
	 */
	public static void deleteArea(Planar<GrayU8> input, Area area, FillingType filling) {
		if (area.isEmpty()) {
			throw new IllegalArgumentException("La zone est vide");
		}
		int xMin = area.getxMin();
		int xMax = area.getxMax();
		int yMin = area.getyMin();
		int yMax = area.getyMax();
		if (xMin < 0 || xMax > input.width || yMin < 0 || yMax > input.height) {
			throw new IllegalArgumentException("Les coordonnées sont en dehors de l'image");
		}
		if (filling == FillingType.CONVOLUTION) {
			int[][] kernel = createGaussienKernel(5, 1);

			Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
			for (int i = xMin; i <= xMax; i++) {
				for (int j = yMin; j <= yMax; j++) {
					map.put(mapValue(i, j), false);
					setRGBValue(input, i, j, new int[] { 0, 0, 0 });
				}
			}

			int xStart = xMin;
			int yStart = yMin;
			int xEnd = xMax;
			int yEnd = yMax;

			while (xStart <= xEnd && yStart <= yEnd) {
				for (int x = xStart; x <= xEnd; x++) {
					setRGBValue(input, x, yStart,
							getConvolutionValue(input, x, yStart, xMin, xMax, yMin, yMax, kernel, map));
					map.put(mapValue(x, yStart), true);
				}
				for (int y = yStart + 1; y <= yEnd; y++) {
					setRGBValue(input, xEnd, y,
							getConvolutionValue(input, xEnd, y, xMin, xMax, yMin, yMax, kernel, map));
					map.put(mapValue(xEnd, y), true);
				}
				if (yStart < yEnd) {
					for (int x = xEnd - 1; x >= xStart; x--) {
						setRGBValue(input, x, yEnd,
								getConvolutionValue(input, x, yEnd, xMin, xMax, yMin, yMax, kernel, map));
						map.put(mapValue(x, yEnd), true);
					}
				}
				if (xStart < xEnd) {
					for (int y = yEnd - 1; y > yStart; y--) {
						setRGBValue(input, xStart, y,
								getConvolutionValue(input, xStart, y, xMin, xMax, yMin, yMax, kernel, map));
						map.put(mapValue(xStart, y), true);
					}
				}
				xStart++;
				yStart++;
				xEnd--;
				yEnd--;
			}
		}
		for (int i = xMin; i <= xMax; i++) {
			for (int j = yMin; j <= yMax; j++) {
				if (filling == FillingType.SKIP) {
					setRGBValue(input, i, j, new int[] { 0, 0, 0 });
				} else if (filling == FillingType.LEFT) {
					int ind = xMin - (i - xMin);
					if (ind < 0) {
						ind = -ind;
					}
					while (ind > 2 * xMin) {
						ind -= 2 * xMin;
					}
					if (ind > xMin) {
						ind = xMin - (ind - xMin);
					}
					setRGBValue(input, i, j, getRGBValue(input, ind, j));
				} else if (filling == FillingType.RIGHT) {
					int ind = xMax;
					boolean decrease = false;
					for (int k = xMax; k >= i; k--) {
						if (decrease) {
							ind--;
							if (ind == xMax) {
								decrease = false;
								ind++;
							}
						} else {
							ind++;
							if (ind == input.width) {
								decrease = true;
								ind--;
							}
						}
					}
					setRGBValue(input, i, j, getRGBValue(input, ind, j));
				} else if (filling == FillingType.TOP) {
					int ind = yMin - (j - yMin);
					if (ind < 0) {
						ind = -ind;
					}
					while (ind > 2 * yMin) {
						ind -= 2 * yMin;
					}
					if (ind > yMin) {
						ind = yMin - (ind - yMin);
					}
					setRGBValue(input, i, j, getRGBValue(input, i, ind));
				} else if (filling == FillingType.BOTTOM) {
					int ind = yMax;
					boolean decrease = false;
					for (int k = yMax; k >= j; k--) {
						if (decrease) {
							ind--;
							if (ind == yMax) {
								decrease = false;
								ind++;
							}
						} else {
							ind++;
							if (ind == input.height) {
								decrease = true;
								ind--;
							}
						}
					}
					setRGBValue(input, i, j, getRGBValue(input, i, ind));
				} else {
					setRGBValue(input, i, j, getRGBValue(input, i, j));
				}
			}
		}
	}

	/**
	 * Effectue un filtre de type bruit gaussien sur l'image donnée en paramètre.
	 * 
	 * @param input     L'image à modifier
	 * @param intensity L'intensité du bruit
	 */
	public static void gaussianNoiseFilter(Planar<GrayU8> input, int intensity) {
		nbSteps = input.height;
		BoofConcurrency.loopFor(0, input.height, y -> {
			for (int x = 0; x < input.width; x++) {
				int rgb[] = getRGBValue(input, x, y);
				for (int i = 0; i < input.getNumBands(); i++) {
					Random rand = new Random();
					// Genere une valeur de bruit aleatoire a partir d'une distribution gaussienne
					double noise = rand.nextGaussian() * intensity * 10;
					rgb[i] = (int) Math.round(rgb[i] + noise);
					// On vérifie que la valeur est comprise entre 0 et 255
					rgb[i] = Math.max(0, Math.min(255, rgb[i]));
					if (input.getNumBands() == 1) {
						rgb[1] = rgb[0];
						rgb[2] = rgb[0];
						break;
					}
				}
				setRGBValue(input, x, y, rgb);
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
	}

	/**
	 * Rogne l'image en fonction de l'area donnée en paramètre
	 * 
	 * @param input L'image à rogner
	 * @param area  L'area à utiliser pour rogner l'image
	 */
	public static void crop(Planar<GrayU8> input, Area area) {
		if (area.isEmpty())
			throw new IllegalArgumentException("La zone est vide");
		int xMin = area.getxMin();
		int xMax = area.getxMax();
		int yMin = area.getyMin();
		int yMax = area.getyMax();
		if (xMin < 0 || xMax > input.width || yMin < 0 || yMax > input.height) {
			throw new IllegalArgumentException("Les coordonnées sont en dehors de l'image");
		}
		nbSteps = xMax - xMin + 1;
		Planar<GrayU8> output = new Planar<>(GrayU8.class, xMax - xMin + 1, yMax - yMin + 1, input.getNumBands());
		BoofConcurrency.loopFor(xMin, xMax, i -> {
			for (int j = yMin; j <= yMax; j++) {
				setRGBValue(output, i - xMin, j - yMin, getRGBValue(input, i, j));
			}
			counter.incrementAndGet();
		});
		counter.set(-1);
		input.setTo(output);
	}

	/**
	 * 
	 * retorune la valeur seuil d'en dessous (0-60-120-180-240-300)
	 * 
	 * @param h le niveau hue de la couleur du pixel (en HSV)
	 */
	private static int getFloorLevel(float h) {
		int floorLevel = 0;
		while (floorLevel + 60 < h)
			floorLevel += 60;
		return floorLevel;
	}

	/**
	 * 
	 * Applique une nouvelle couleur à la place de la couleur choisie si keep est
	 * faux
	 * ou à la place de toutes les autres couleurs si keep est vrai.
	 * 
	 * @param input    L'image d'entrée
	 * @param oldColor La couleur choisie en hexadecimal
	 * @param range    définit la gamme de couleur choisie, elle peut aller de 0 à
	 *                 179.
	 * @param newColor La couleur donnée qui va être appliquée
	 * @param keep     Permet de choisir entre garder la couleur choisie puis
	 *                 modifie tout le reste ou modifie uniquement la couleur
	 *                 choisie
	 */
	public static void changeColoration(Planar<GrayU8> input, String hexOldColor, int range, String newColor,
			boolean keep) {

		int r = Integer.valueOf(hexOldColor.substring(0, 2), 16);
		int g = Integer.valueOf(hexOldColor.substring(2, 4), 16);
		int b = Integer.valueOf(hexOldColor.substring(4, 6), 16);

		float hsvTMP[] = new float[3];
		rgbToHsv(r, g, b, hsvTMP);
		float hue = hsvTMP[0];

		float tmpMin = ((hue - range) + 360) % 360;
		float tmpMax = (hue + range) % 360;
		System.out.println("aimed hue : " + hue);
		System.out.println("tmpMin = " + tmpMin + "; tmpMax = " + tmpMax + "; keep = " + keep);
		if (tmpMin > tmpMax) {
			float tmp = tmpMin;
			tmpMin = tmpMax;
			tmpMax = tmp;
			keep = !keep;
		}
		System.out.println("tmpMin = " + tmpMin + "; tmpMax = " + tmpMax + "; keep = " + keep);

		int deltaColor = 0;
		switch (newColor) {
			case "RED":
				deltaColor = 0;
				break;
			case "YELLOW":
				deltaColor = 60;
				break;
			case "GREEN":
				deltaColor = 120;
				break;
			case "CYAN":
				deltaColor = 180;
				break;
			case "BLUE":
				deltaColor = 240;
				break;
			case "MAGENTA":
				deltaColor = 300;
				break;
			default:
				break;
		}

		// oldColor is the name of the color we want to modify / keep
		// colorLevel is the floor of a 60-digit-wide range (RED = 0-59, YELLOW =
		// 60-119, etc...) representing with numbers oldColor

		// newColor is the name of the color we want to apply on each concerned pixel
		// newColorDelta is the ammount we will add to obtain the desired shade of the
		// new color

		// example : old color is CYAN, we want to modify it, the new color is MAGENTA :
		// colorLevel = 180, we take a pixel, hue is 214, we get the shade by
		// substracting it the colorLevel :
		// shade = 214 - 180 = 34
		// we now add the delta to the shade and we get the desired hue that we will
		// apply :
		// newHue = shade + newColorDelta = 34 + 300 = 334. We then just modify the hue
		// of the pixel
		// and we finaly finished modifying the color of the pixel while keeping its
		// shade

		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				int rgb[] = new int[3];
				float hsv[] = new float[3];
				for (int band = 0; band < 3; band++)
					rgb[band] = input.getBand(band).get(x, y);

				rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
				float h = hsv[0];
				float s = hsv[1];
				float v = hsv[2];

				boolean test1 = keep && (h >= tmpMin && h < tmpMax);
				boolean test2 = (!keep) && (h < tmpMin || h >= tmpMax);
				if (test1 || test2) {
					continue;
				} else {
					if (newColor == "GREY") {
						int gl = 0;
						double coeff[] = { 0.3, 0.59, 0.11 };

						for (int band = 0; band < 3; band++)
							gl += (coeff[band] * rgb[band]);

						for (int band = 0; band < 3; band++)
							input.getBand(band).set(x, y, gl);
					} else {
						h = (h - getFloorLevel(h)) + deltaColor;
						hsvToRgb(h, s, v, rgb);

						for (int band = 0; band < 3; band++)
							input.getBand(band).set(x, y, rgb[band]);
					}
				}
			}
		}
	}

	/**
	 * 
	 * Applique une nouvelle couleur à la place de la couleur choisie si keep est
	 * faux
	 * ou à la place de toutes les autres couleurs si keep est vrai.
	 * 
	 * @param input     L'image d'entrée
	 * @param intensity L'intensité de la saturation variant de 0 à 1
	 */
	public static void saturation(Planar<GrayU8> input, double intensity) {
		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				int rgb[] = new int[3];
				float hsv[] = new float[3];
				float s;
				for (int band = 0; band < 3; band++) {
					rgb[band] = input.getBand(band).get(x, y);
				}
				rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
				s = (float) (hsv[1] * intensity);
				s = Math.min(Math.max(s, 0.0f), 1.0f);
				hsvToRgb(hsv[0], s, hsv[2], rgb);
				for (int band = 0; band < 3; band++) {
					input.getBand(band).set(x, y, rgb[band]);
				}
			}
		}
	}
}
