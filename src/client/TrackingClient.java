package client;

import static basics.Constants.PORT;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import basics.Contact;
import basics.Entity;
import basics.WalkUtil;

public class TrackingClient {

	// communication with server
	private ByteBuffer bufferContacts;
	private CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
	// ID given by the server
	private int myId;
	// each client gets its own entity, which is moving
	private Entity myEntity; // synchronize??

	// each client stores the contacts data
	private List<Contact> contacts = new ArrayList<>();// stores the contacts, but does not need to be sync

	/*
	 * Default start-Position is 0
	 */
	public TrackingClient() {
		this(0, 0);
	}

	/*
	 * creating a new Client with given starting position
	 */
	public TrackingClient(int x, int y) {
		myEntity = Entity.at(x, y);
	}

	void start() throws UnknownHostException, IOException, InterruptedException, ClassNotFoundException {
		// Open up Connection to server
		// now we are using SocketChannels for the client
		SocketChannel channel = SocketChannel.open();
		channel.connect(new InetSocketAddress("localhost", PORT));

		bufferContacts = ByteBuffer.allocate(256);

		// recieve ID from server
		channel.read(bufferContacts);

		bufferContacts.flip();
		// client sent a new position data in form of string, decode it
		String s = Charset.forName("UTF-8").newDecoder().decode(bufferContacts).toString();
		//String s = new String(bufferContacts.array()).trim(); // trim is used, because buffer is bigger
		myId = Integer.parseInt(s);
		// set method for entity ID

		System.out.println("Client " + myId + " started and connected to server");

		Thread sendPositionThread = new Thread(() -> {

			try {
				while (!Thread.interrupted()) {

					String position = "POS" + myEntity.getX() + "," + myEntity.getY();
					// writing using charbuffers insted of a globally defined buffer

					channel.write(enc.encode(CharBuffer.wrap(position)));

					// client output
					System.out.println("I am at pos:" + myEntity.getX() + "," + myEntity.getY());

					// go to an other position
					myEntity = WalkUtil.walk(myEntity);
					WalkUtil.delay();
				}
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			}

		});
		sendPositionThread.start();

		Thread getContactDataThread = new Thread(() -> {

			while (!Thread.interrupted()) {
				try {
					// used buffer for channel reading needs to be cleared, otherwise
					// the convertion back to an Contact Object would be impossible
					bufferContacts.clear();
					// convert from buffer to Contact object
					channel.read(bufferContacts); // doing this blocking
					System.out.println("I am getting a new Contact");
					// transform channel message to contact object
					ByteArrayInputStream in = new ByteArrayInputStream(bufferContacts.array());
					ObjectInputStream is = new ObjectInputStream(in);

					Contact newContact = (Contact) is.readObject();

					// just for printing the client the information about the contact
					if (newContact.getPair()[0].getId() == myId) {
						System.out.println("Client " + myId + " got a new Contact: Entity ID was "
								+ newContact.getPair()[1].getId() + " at position: ["+newContact.getPair()[1].getX()+","+newContact.getPair()[1].getY()+"]");
					} else {
						System.out.println("Client " + myId + " got a new Contact: Entity ID was "
								+ newContact.getPair()[0].getId() + " at position: ["+newContact.getPair()[0].getX()+","+newContact.getPair()[0].getY()+"]");
					}
					// add converted contact to the list
					contacts.add(newContact);

				} catch (Exception ex) { // as soon as an error happens, we stop getting data from server
					// ex.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		});
		getContactDataThread.start();

		Scanner inputScanner = new Scanner(System.in);
		inputScanner.nextLine(); // if there is a next line, somebody pressed the enter button
		inputScanner.close();// closes scanner
		// end sender and reciever threads
		sendPositionThread.interrupt(); // throw InterruptedException
		getContactDataThread.interrupt();// throw InterruptedException
		// as soon as we interrupt the thread, the server will get an empty message from
		// the client, and end the connection therefore. so we dont need more to do here
		try {
			bufferContacts.clear();
			channel.close();
		} catch (Exception e) {

		}
		// shutdown message
		System.out.println("Client" + myId + " shutdown");

	}

}
