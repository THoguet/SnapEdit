package pdl.backend;

public class Area {
	private int xMin;
	private int yMin;
	private int xMax;
	private int yMax;

	public Area(int xMin, int yMin, int xMax, int yMax) {
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
	}

	public Area(String str) {
		this.setValues(str);
	}

	public void setValues(String str) {
		String[] values = str.split(";");
		if (values.length != 4) {
			throw new IllegalArgumentException("Area parameter must have 4 values");
		}
		int[] intValues = new int[4];
		for (int i = 0; i < 4; i++) {
			intValues[i] = Integer.parseInt(values[i]);
		}
		this.xMin = intValues[0];
		this.yMin = intValues[1];
		this.xMax = intValues[2];
		this.yMax = intValues[3];
	}

	public int getxMin() {
		return xMin;
	}

	public void setxMin(int xMin) {
		this.xMin = xMin;
	}

	public int getyMin() {
		return yMin;
	}

	public void setyMin(int yMin) {
		this.yMin = yMin;
	}

	public int getxMax() {
		return xMax;
	}

	public void setxMax(int xMax) {
		this.xMax = xMax;
	}

	public int getyMax() {
		return yMax;
	}

	public void setyMax(int yMax) {
		this.yMax = yMax;
	}

	public boolean isEmpty() {
		return xMin == xMax && yMin == yMax;
	}
}
