package basics;

import static basics.Constants.CONTACT_DISTANCE;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class representing an spatial area. 
 * Area defines coordinates and the contains entities. 
 * Contains entities that are within the area or within 
 * {@link Constants.DISTANCE} {@link Constants.DISTANCE} distance to the area. 
 */
public class Area {
	
	private final int x, y, w, h; 
	private Set<Entity> entities;  
	private List<Integer> neighbourIndexList;
	private final Lock lock = new ReentrantLock();
	/**
	 * Constructor initializing this area. 
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param w the width
	 * @param h the height
	 */
	public Area(int x, int y, int w, int h) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.entities = new CopyOnWriteArraySet<>(); 
		this.neighbourIndexList = null;
	} 
	
	/**
	 * Adds an entity to this area. 
	 * The entity should be within the area or within {@link Constants.DISTANCE} distance to the area. 
	 * @param ent
	 */
	public void add(Entity ent) {
		entities.add(ent); 
	}
	
	/** 
	 * Gets the set of entities of this area. 
	 * @return the set of entities 
	 */
	public Set<Entity> getEntities() {
		return Collections.unmodifiableSet(entities); 
	}

	/**
	 * Gets the left coordinate of this area. 
	 * @return the left coordinate 
	 */
	public int getLeft() {
		return x;
	}

	/**
	 * Gets the top coordinate of this area. 
	 * @return the top coordinate 
	 */
	public int getTop() {
		return y;
	}

	/**
	 * Gets the right coordinate of this area. 
	 * @return the right coordinate 
	 */
	public int getRight() {
		return x + w; 
	}
	
	/**
	 * Gets the bottom coordinate of this area. 
	 * @return the bottom coordinate 
	 */
	public int getBottom() {
		return y + h; 
	}
	
	/**
	 * Gets the width of this area. 
	 * @return the width 
	 */
	public int getWidth() {
		return w;
	}

	/**
	 * Gets the height of this area. 
	 * @return the height 
	 */
	public int getHeight() {
		return h;
	}
	
	/**
	 * Tests if the entity is within this area. 
	 * @param area the area 
	 * @return true if this entity is within the area
	 */
	public boolean isWithin(Entity e) {
		return e.getX() >= getLeft() && e.getY() >= getTop() &&
				e.getX() < getRight() && e.getY() < getBottom();
	}

	/**
	 * Tests if this entity is within {@link Constants.DISTANCE} distance 
	 * to the right or below the area. 
	 * @param area the area 
	 * @return true if this entity is close to the right or bottom of the area
	 */
	public boolean isClose(Entity e) {
		return ! isWithin(e) && 
				e.getX() >= getRight() && e.getX() < getRight() + CONTACT_DISTANCE &&
						e.getY() >= getBottom() && e.getY() < getBottom() + CONTACT_DISTANCE;
	} 


	
	/**
	 * Splits this area into d * d sub-areas. 
	 * Returns an array of the subareas. 
	 * The entities of this area that are within the sub-areas or 
	 * within {@link Constants.DISTANCE} distance of 
	 * to the sub-areas are added to the sub-areas
	 * @param n the number splits in x- and y-coordinate
	 * @return the array of sub-areas
	 */
	public Area[] split(int n) {
		int n_2 = n * n; 
		int wDivN = getWidth() / n; 
		int hDivN = getHeight() / n; 
		Area[] subAreas = new Area[n_2];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int wSub = i < n - 1 ? wDivN : wDivN + w % n; 
				int hSub = j < n - 1 ? hDivN : hDivN + h % n; 
				subAreas[i*n + j] = 
						new Area(getLeft() + wDivN * i, 
								 getTop() + hDivN * j, 
								 wSub, 
								 hSub); 
			}
			
		}
		for (Entity entity : getEntities()) {
			for (int i = 0; i < n_2; i++) {
				Area subArea = subAreas[i]; 
				if (subArea.isWithin(entity) || subArea.isClose(entity)) {
					subArea.add(entity);
				}
			}
		}
		return subAreas; 
	}

	/**
	 * Returns a string representation of this area. 
	 * @return a string representation 
	 */
	@Override
	public String toString() {
		return "[" + x + "/" + y + " " + getRight() + "/" + getBottom() + "] entities: " + entities.size();
	}

	/**
	 * method removes entity form list of this area
	 * 
	 * @param clientEnt
	 */
	public void delete(Entity clientEnt) {
		entities.remove(clientEnt);
	}

	public void setEntities(Set<Entity> input) {
		this.entities = input;
	}

	/**
	 * Method returns the indices of the neighbours of the given area
	 * only useable if main area gets splitted
	 * @return
	 */
	public List<Integer> getNeighbourList() {
		return neighbourIndexList;
	}
	/**
	 * Method sets the indices of the neighbours of the given area
	 * only useable if main area gets splitted
	 */
	public void setNeighbourList(List<Integer> neighList) {
		this.neighbourIndexList = neighList;
	}
	
	/**
	 * Caller of this method gets lock of the area
	 * @return
	 */
	public Lock getLock() {
		return lock;
	}
	
}




