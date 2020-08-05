package basics;

public final class Pos implements java.io.Serializable{
	
	/**
	 * Creates a new <code>Pos</code> object from x- and y-coordinates. 
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @return the new <code>Pos</code> object. 
	 */
	public static Pos of(int x, int y) {
		return new Pos(x, y); 
	}
	
	/** The x-coordinate of this position. */
	public final int x;

	/** The y-coordinate of this position. */
	public final int y;

	/**
	 * Constructor initializing x- and y-coordinates. 
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 */
	private Pos(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pos other = (Pos) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	} 

}
