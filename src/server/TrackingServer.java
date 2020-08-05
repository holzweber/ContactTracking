package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import basics.Area;
import basics.Contact;
import basics.Entity;
import static basics.Constants.SERVER_TIMEOUT;
import static basics.Constants.SPLIT;
import static basics.Constants.PORT;
import static basics.Constants.SIZE;

public class TrackingServer {

	/**
	 * Terminate Flag is used to close sever process
	 */
	private boolean terminate = false;

	/**
	 * Each new client gets it's own ID
	 */
	private static int ID = 1;

	/**
	 * ServerSocketChannel is used for connection with the client
	 */
	private ServerSocketChannel serverSocket;

	/**
	 * Shared ressources for all client Handler
	 *
	 */
	private final Area area = new Area(0, 0, SIZE, SIZE);
	private volatile Area[] splittedAreas;

	/*
	 * service is used, so for each new position of a certain client, we start a
	 * thread which will search for conatacts and also update the position in the
	 * rea correctly
	 */
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	/**
	 * Set of all found Contacts so far
	 */
	private final Set<Contact> foundContacts;

	// Storing the active entites in this list
	public TrackingServer() {
		foundContacts = new CopyOnWriteArraySet<>();
		// splitting the large area into subareas
		splittedAreas = area.split(SPLIT);
	}

	void start() {

		try {
			// prepare selector, which will react to different action
			Selector selector = Selector.open();
			// create server connection
			serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress("localhost", PORT));
			// put server in non blocking mode
			serverSocket.configureBlocking(false);
			// reigster the accept event in the selector
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			// bytebuffer used for reading position string and writing
			// contact objects
			ByteBuffer buffer = ByteBuffer.allocate(256);
			// charsets used for stringchatting
			CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();
			CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
			while (!terminate) {
				try {
					// select events of this particular moment
					selector.select(SERVER_TIMEOUT);
					// list used for iterating over pending events
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();
					while (iter.hasNext() && !terminate) {

						// get event key
						SelectionKey key = iter.next();
						// get entity, which is attached to the event, so we know,
						// which client is sending its position
						ClientInformation attachment = (ClientInformation) key.attachment(); // get attached entity

						// check if a new client wants to connect to the server
						if (key.isAcceptable()) {
							// encoder used for charbuffering(sending strings to the client)
							System.out.println("Client with ID=" + ID + " joined the tracking!");
							// accept new client
							SocketChannel client = serverSocket.accept();
							// non blocking mode
							client.configureBlocking(false);
							// register key to the selector, so we can read position data
							SelectionKey k = client.register(selector, SelectionKey.OP_READ);

							// create new ClientInformation with given ID and attach it to the client source
							k.attach(new ClientInformation(client, ID)); // attach entity

							// send ID to the client using charbuffer encoding
							String s = ID + "";
							client.write(enc.encode(CharBuffer.wrap(s)));
							ID++;
						}

						// event was a position data of a already registered client
						if (key.isReadable()) {
							// prepare for reading data as string from channel
							buffer.clear();
							SocketChannel client = (SocketChannel) key.channel();
							int inputbytes = client.read(buffer); // store number read bytes
							if (inputbytes < 0) {// client has closed if this happens
								client.close();// get rid of the client
								setEntityOffline(attachment.getEntity()); // get entity out of area
								System.out.println("Set client ID=" + attachment.getEntity().getId() + "offline");
								break;
							}
							// prepare buffer for getting coorindates
							buffer.flip();
							// client sent a new position data in form of string, decode it
							String str = dec.decode(buffer).toString();
							str = str.substring(3);
							// get coordinates of the string without POS-Tag
							String[] coordinates = str.split(",");// x is in 0, y is in 1
							// the executor service will now calculate new contacts and update the position
							// of the client, which we just got the position from
							executorService.submit(new UpdateSearchRunnable(this, attachment, coordinates));
						}
						iter.remove();
					}

				} catch (Exception e) {
					// if an error occurs during channels conversation
					terminate(); // end connection because data conversion failed, error with clients
				}
			}
		} catch (Exception e) {
			terminate();
			e.printStackTrace();
		} finally {
			try {

				serverSocket.close();
				System.out.println("Closed Server");
			} catch (IOException ex) {
				System.out.println("Error closing the Server socket");
			}
		}
	}

	/**
	 * get splitted areas
	 * 
	 * @return Area Array
	 */
	public Area[] getAreaArray() {
		return splittedAreas;
	}

	/**
	 * This method is used to shutdown the tracking server.
	 */
	public void terminate() {
		terminate = true;
		executorService.shutdown();
	}

	/**
	 * The client handler calls this method, in order to update the the position in
	 * the subarea
	 * 
	 * @param clientEnt
	 */
	public Set<Contact> UpdateAndGetContact(Entity clientEnt) {
		// check if client is even in a area, or it has changed
		if (clientEnt.getArea() != null && !clientEnt.getArea().isWithin(clientEnt)) {
			clientEnt.getArea().delete(clientEnt);
			clientEnt.setArea(null, -1);
		}
		// we get in here, if entity is no longer in an area
		if (clientEnt.getArea() == null) {
			// search new area
			for (int i = 0; i < splittedAreas.length; i++) {
				// check if entity is in a new area, where it was not stored before
				if (splittedAreas[i].isWithin(clientEnt)) {
					splittedAreas[i].add(clientEnt);
					clientEnt.setArea(splittedAreas[i], i);
					break;
				}
			}
		}

		Area clientArea = clientEnt.getArea();
		int clientID = clientEnt.getAreaID(); // store ID of Area in the splitted array, for bounding check

		List<Contact> returnContactArray = new ArrayList<>();

		List<Integer> neighList;

		// create a neighbour list if it is not existing yet
		if (clientArea.getNeighbourList() == null) {
			neighList = new ArrayList<>();

			// check for left area
			if (clientID % SPLIT != 0) {
				neighList.add(clientID - 1);
			}
			// check for right area
			if (clientID + 1 % SPLIT != 0) {
				neighList.add(clientID + 1);
			}
			// above center
			if (clientID - SPLIT > 0) {
				neighList.add(clientID - SPLIT);
			}
			// under center
			if (clientID + SPLIT < SPLIT * SPLIT) {
				neighList.add(clientID + SPLIT);
			}
			// left up diag.
			if (clientID - SPLIT > 0 && clientID % SPLIT != 0) {
				neighList.add(clientID - SPLIT - 1);
			}
			// right up diag.
			if (clientID - SPLIT > 0 && clientID + 1 % SPLIT != 0) {
				neighList.add(clientID - SPLIT + 1);
			}
			// right down diag.
			if (clientID + SPLIT < SPLIT * SPLIT && clientID + 1 % SPLIT != 0) {
				neighList.add(clientID + SPLIT + 1);
			}
			// left down diag.
			if (clientID + SPLIT < SPLIT * SPLIT && clientID % SPLIT != 0) {
				neighList.add(clientID + SPLIT - 1);
			}
			// store list in given subarea
			clientArea.setNeighbourList(neighList);
		} else { // list is already existing
			neighList = clientArea.getNeighbourList();
		}
		foundContacts.addAll(checkContacts(clientArea, neighList));
		return foundContacts;
	}

	/*
	 * method used to check for contacts in given area
	 */
	private Set<Contact> checkContacts(Area center, List<Integer> neighList) {
		/*
		 * create a local model of the area, where we will append the entities of the
		 * neighbourhood areas within the range
		 */
		Area temp = new Area(center.getLeft(), center.getTop(), center.getWidth(), center.getHeight());
		// load entities of the area, where client is stored
		temp.setEntities(center.getEntities());

		// load possible contacts of neighbour areas
		for (int i : neighList) {
			for (Entity e : splittedAreas[i].getEntities()) {
				if (center.isClose(e)) {
					temp.add(e);
				}
			}
		}

		// calculate contacts of the area and return the set
		ContactRecursiveTask crt = new ContactRecursiveTask(temp);
		return crt.compute();
	}

	/**
	 * Method used to remove Entity from the area, because client got offline
	 */
	public void setEntityOffline(Entity e) {
		e.getLock().lock();
		try {
			e.getArea().delete(e);
		} finally {
			e.getLock().unlock();
		}
	}
}
