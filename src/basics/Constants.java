package basics;

import java.util.Random;

/**
 * Class containing constants for doing the experiments.
 */
public class Constants {
	public static final long SERVER_TIMEOUT = 5000;
	/**
	 * Defines the used Port for the server connection
	 */
	public static final int PORT = 7777;
	/**
	 * Seed value for the random number generator. Use seed for deterministic
	 * configuration
	 */
	private static final int SEED = 6;

	/** The random number generator used to generate the entity locations */
	public static final Random RAND = new Random(SEED);

	/** The distance value when entities are getting into contact */
	public static final int CONTACT_DISTANCE = 10;

	/** here we set the nxn field, where n = SPLIT */
	public static final int SPLIT = 64;

	/** Setting the Constants for the RecursiveTask */
	public static final int THRESHHOLD_AREA = 32;
	public static final int THRESHHOLD_ENTITIES = 64;

	// TEST SETTINGS
//	/** The size of the area. An area with SIZE * SIZE is created. */
	public static final int SIZE = 4096;
//	
//	/** The number of entities for the area.  */
//	public static final int N_ENTITIES = 1000;

	// SMALL SETTINGS
//	public static final int SIZE =    524_288;
//	public static final int N_ENTITIES = 100_000;

	// MODERATE SETTINGS
//	public static final int SIZE =      1_048_576 ;
//	public static final int N_ENTITIES = 500_000;

//	 LARGE SETTINGS
//	public static final int SIZE =    8_388_608;
//	public static final int N_ENTITIES = 10_000_000;

}
