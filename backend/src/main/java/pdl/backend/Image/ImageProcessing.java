package pdl.backend.Image;

import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

import boofcv.concurrency.BoofConcurrency;

public final class ImageProcessing {

	private ImageProcessing() {
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
		});
	}

	/**
	 * Étale l'histogramme d'une pour une meilleure
	 * répartition des couleurs
	 * 
	 * @param input L'image à étaler.
	 */
	public static void histogram(Planar<GrayU8> input) {
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
		});
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
		});
	}

	/**
	 * 
	 * Calcule et stocke dans output l'image de la norme du gradient
	 * calculé sur input par convolution avec les filtres de Sobel.
	 * 
	 * @param input L'image d'entrée en niveaux de gris.
	 */
	public static void gradientImageSobel(Planar<GrayU8> input) {
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
		});
		for (int i = 0; i < input.width; i++) {
			for (int j = 0; j < input.height; j++) {
				int[] rgb = getRGBValue(output, i, j);
				setRGBValue(input, i, j, rgb);
			}
		}
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
		});
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
	public static void changeColoration(Planar<GrayU8> input, String hexOldColor, int range,
			String newColor, boolean keep) {

		int r = Integer.valueOf(hexOldColor.substring(1, 3), 16);
		int g = Integer.valueOf(hexOldColor.substring(3, 5), 16);
		int b = Integer.valueOf(hexOldColor.substring(5, 7), 16);

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
			case "red":
				deltaColor = 0;
				break;
			case "yellow":
				deltaColor = 60;
				break;
			case "green":
				deltaColor = 120;
				break;
			case "cyan":
				deltaColor = 180;
				break;
			case "blue":
				deltaColor = 240;
				break;
			case "magenta":
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
					if (newColor == "grey") {
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
