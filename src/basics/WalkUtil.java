package basics;

import java.util.Random;

/**
 * Utility class for randomly change position of entities and delaying
 * execution.
 */
public class WalkUtil {

	private static final Random RAND = new Random();
	private static final int MIN_DELAY = 1000;
	private static final int RAND_DELAY = 1000;
	private static final int DIST = 8;

	/**
	 * Changes the position randomly on the area of given size
	 * {@link Constants.SIZE}.
	 * 
	 * @param entity the entity to change the position
	 * @return the same entity with position changed
	 */
	public static Entity walk(Entity entity) {
		int dx = RAND.nextInt(DIST);
		int dy = RAND.nextInt(DIST);
		int x = entity.getX() + dx < 0 || entity.getX() + dx >= Constants.SIZE ? entity.getX() - dx
				: entity.getX() + dx;
		int y = entity.getY() + dy < 0 || entity.getY() + dy >= Constants.SIZE ? entity.getY() - dy
				: entity.getY() + dy;
		entity.goTo(x, y);
		return entity;
	}

	/**
	 * Passivates the current thread for a randomly generated time between MIN_DELAY
	 * and MIN_DELAY + RAND_DELAY.
	 */
	public static void delay() {
		try {
			Thread.sleep(MIN_DELAY + RAND.nextInt(RAND_DELAY));
		} catch (InterruptedException e) {
			// needed so user thread also gets an interrupted flag(is getting cleared by
			// catching)
			Thread.currentThread().interrupt();
		}
	}

}
