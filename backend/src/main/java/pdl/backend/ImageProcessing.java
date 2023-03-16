package pdl.backend;

import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public class ImageProcessing {

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
	 * @param input  l'image d'entrée en couleur
	 * @param output l'image de sortie en niveaux de gris
	 */
	public static void convertColorToGray(Planar<GrayU8> input, Planar<GrayU8> output) {
		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				int[] rgb = getRGBValue(input, x, y);
				int gray = (int) (0.3 * rgb[0] + 0.59 * rgb[1] + 0.11 * rgb[2]);
				setRGBValue(output, x, y, new int[] { gray, gray, gray });
			}
		}
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
		for (int y = 0; y < input.height; y++) {
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
		}
	}

	/**
	 * Étale l'histogramme d'une pour une meilleure
	 * répartition des couleurs
	 * 
	 * @param input L'image à étaler.
	 */
	public static void histogram(Planar<GrayU8> input) {
		int h[] = new int[256];
		Planar<GrayU8> grayInput = input.createSameShape();
		convertColorToGray(input, grayInput);
		// On remplit le tableau avec le nombre de pixels pour chaque niveau de
		// gris
		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				int gl = grayInput.getBand(0).get(x, y);
				h[gl]++;
			}
		}

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
		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				for (int j = 0; j < input.getNumBands(); j++) {
					int val = input.getBand(j).get(x, y);
					input.getBand(j).set(x, y, LUT[val]);
				}
			}
		}
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
	public static void colorFilter(int hue, Planar<GrayU8> input) {
		if (hue < 0 || hue > 360) {
			throw new IllegalArgumentException("Hue doit être compris entre 0 et 360");
		}
		for (int j = 0; j < input.height; j++) {
			for (int i = 0; i < input.width; i++) {
				float[] hsv = new float[3];
				int[] pixelValue = getRGBValue(input, i, j);
				rgbToHsv(pixelValue[0], pixelValue[1], pixelValue[2], hsv);
				hsv[0] = hue;
				int[] rgb = new int[3];
				hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);
				setRGBValue(input, i, j, rgb);
			}
		}
	}

	/**
	 * 
	 * Calcule et stocke dans output l'image de la norme du gradient
	 * calculé sur input par convolution avec les filtres de Sobel.
	 * 
	 * @param input  L'image d'entrée en niveaux de gris.
	 * @param output L'image de sortie en niveaux de gris.
	 */
	public static void gradientImageSobel(Planar<GrayU8> input, Planar<GrayU8> output) {
		int h1[][] = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
		int h2[][] = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		int half = h1.length / 2;
		Planar<GrayU8> inputGray = input.createSameShape();
		convertColorToGray(input, inputGray);
		for (int y = 0; y < input.height; y++) {
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
		}
	}

	/**
	 * Applique un filtre moyenneur de taille indiquée à l'image
	 * passée en paramètre (le bord de l'image n'est pas modifié)
	 * 
	 * @param input  image d'entrée
	 * @param output image de sortie
	 * @param size   taille du filtre à appliquer (doit être impaire)
	 */
	public static void meanFilter(Planar<GrayU8> input, Planar<GrayU8> output, int size) {
		// Vérification que la taille est impaire
		if (size % 2 == 0) {
			throw new IllegalArgumentException("La taille doit être impaire");
		}
		// Boucle pour parcourir tous les pixels de l'image d'entrée
		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				if (y < size / 2 || x < size / 2 || y >= input.height - size / 2
						|| x >= input.width - size / 2) {
					setRGBValue(output, x, y, getRGBValue(input, x, y));
					continue;
				}
				int totalValue[] = new int[3];
				// Boucle pour parcourir le carré size x size autour du pixel actuel
				for (int j = y - size / 2; j < y + size / 2 + 1; j++) {
					for (int i = x - size / 2; i < x + size / 2 + 1; i++) {
						for (int k = 0; k < totalValue.length; k++) {
							totalValue[k] += input.getBand(k).get(i, j);
						}
					}
				}
				// Calcule la moyenne des pixels du carré et l'applique au pixel actuel dans
				// l'image de
				// sortie
				for (int k = 0; k < totalValue.length; k++) {
					totalValue[k] /= size * size;
				}
				setRGBValue(output, x, y, totalValue);
			}
		}

	}

	/**
	 * TODO
	 * Calcule le noyau gaussien de taille demandée
	 * 
	 * @param size taille du noyau gaussien
	 * @return noyau gaussien
	 */
	public static int[][] createGaussienKernel(int size) {
		// Vérification que la taille est impaire
		if (size % 2 == 0 || size < 0) {
			throw new IllegalArgumentException("La taille doit être impaire et positive");
		}
		double[][] kernel = new double[size][size];
		double sigma = 4 / 3;
		double sum = 0.0; // For accumulating the kernel values
		for (int x = 0; x < size; ++x)
			for (int y = 0; y < size; ++y) {
				kernel[x][y] = 1 / (2 * Math.PI * sigma * sigma) * Math.exp(-(x * x + y * y) / (2 * sigma * sigma));

			}

		// Normalize the kernel
		int[][] kernelk = new int[size][size];
		for (int x = 0; x < size; ++x)
			for (int y = 0; y < size; ++y)
				kernelk[x][y] = (int) (kernel[x][y] / sum);

		return kernelk;
	}

	/**
	 * 
	 * Effectue une convolution sur une image avec le noyau
	 * donné et stocke le résultat dans l'image de sortie.
	 * 
	 * @param input  L'image d'entrée
	 * @param output L'image de sortie
	 * @param kernel Le noyau de convolution à utiliser.
	 */
	public static void gaussienFilter(Planar<GrayU8> input, Planar<GrayU8> output,
			int size) {
		int[][] kernel = { { 1, 2, 3, 2, 1 }, { 2, 6, 8, 6, 2 }, { 3, 8, 10, 8, 3 }, { 2, 6, 8, 6, 2 },
				{ 1, 2, 3, 2, 1 } };
		int half = size / 2;
		int totalCoefs = 0;
		// Calcul du nombre total de coefficients dans le noyau
		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[i].length; j++) {
				totalCoefs += kernel[i][j];
			}
		}
		// Parcours de l'image d'entrée et application de la convolution
		for (int y = 0; y < input.height; y++) {
			for (int x = 0; x < input.width; x++) {
				// Vérification si le carré sera à l'intérieur de l'image
				if (y < half || x < half || y >= input.height - half || x >= input.width -
						half) {
					setRGBValue(output, x, y, getRGBValue(input, x, y));
					continue;
				}
				int currentJ = 0;
				int currentI = 0;
				int totalValue[] = new int[3];
				// Parcours du voisinage défini par le noyau
				for (int j = y - half; j <= y + half; j++) {
					for (int i = x - half; i <= x + half; i++) {
						for (int k = 0; k < totalValue.length; k++) {
							totalValue[k] += input.getBand(k).get(i, j) * kernel[currentJ][currentI];
						}
						currentI++;
					}
					currentI = 0;
					currentJ++;
				}
				for (int k = 0; k < totalValue.length; k++) {
					totalValue[k] /= totalCoefs;
				}
				setRGBValue(output, x, y, totalValue);
			}
		}
	}
}
