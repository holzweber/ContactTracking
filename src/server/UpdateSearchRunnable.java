package server;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import basics.Contact;
import basics.Entity;

public class UpdateSearchRunnable implements Runnable {

	private final TrackingServer ts;
	private final ClientInformation client;
	private final String[] coordinates;
	private final ByteBuffer buffer = ByteBuffer.allocate(256);

	public UpdateSearchRunnable(TrackingServer ts, ClientInformation client, String[] coordinates) {
		this.ts = ts;
		this.client = client;
		this.coordinates = coordinates;
	}

	@Override
	public void run() {
		Set<Contact> contacts = new HashSet<>();
		Set<Contact> contactsRet;
		client.getEntity().getLock().lock();
		try {
			// update position of entity
			client.getEntity().goTo(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
			contactsRet = ts.UpdateAndGetContact(client.getEntity());

			// filter contact list - only the contacts, which include the given
			// enitity should get back to the clienthandler
			for (Contact c : contactsRet) {
				// only put contacts with enitity of client into set
				if (c.getPair()[0].equals(client.getEntity()) || c.getPair()[1].equals(client.getEntity())) {
					contacts.add(c);
				}
			}
		} finally {
			client.getEntity().getLock().unlock();
		}
		for (Contact c : contacts) {

			if (!client.getContacts().contains(c)) { // check if contact is new
				client.addContacts(c);
				sendContacts(c);// send new contact to the client
			}
		}

	}

	/*
	 * If new Contacts were found, send them to the client
	 */
	private void sendContacts(Contact con) {
		try {
			buffer.clear();
			// serialize data
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			ObjectOutputStream os = new ObjectOutputStream(out); // serialize contact object

			os.writeObject(con);

			// put data into buffer
			buffer.put(out.toByteArray());
			// prepare buffer for writing
			buffer.flip();
			// write to clientchannel
			client.getChannel().write(buffer);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error writing to the client");
		}
	}
}
