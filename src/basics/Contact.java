package basics;

/**
 * Class for represents a contact of two entities.
 */
public class Contact implements java.io.Serializable{

	/**
	 * Creates a <code>Contact</code> of two entities.
	 * 
	 * @param e1 the first entity
	 * @param e2 the second entity
	 * @return
	 */
	public static Contact of(Entity e1, Entity e2) {
		return new Contact(e1, e2);
	}

	/** The two entities being in contact */
	private final Entity e1, e2;

	/**
	 * Private constructor
	 * 
	 * @param e1 the first entity
	 * @param e2 the second entity
	 */
	public Contact(Entity e1, Entity e2) {
		super();
		if (e1.getId() < e2.getId()) {
			this.e1 = e1;
			this.e2 = e2;
		} else {
			this.e2 = e1;
			this.e1 = e2;
		}
	}

	/**
	 * Gets the pair of entities.
	 * 
	 * @return an array of two entities
	 */
	public Entity[] getPair() {
		return new Entity[] { e1, e2 };
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e1 == null) ? 0 : e1.hashCode());
		result = prime * result + ((e2 == null) ? 0 : e2.hashCode());
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
		Contact other = (Contact) obj;
		return this.e1.equals(other.e1) && this.e2.equals(other.e2)
				|| this.e1.equals(other.e2) && this.e2.equals(other.e1);
	}

	@Override
	public String toString() {
		return String.format("%s<->%s: %6.2f", e1, e2, e1.distance(e2));
	}

}
