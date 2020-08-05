package server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import basics.Contact;
import basics.Entity;

public class ClientInformation {

	// channel of client
	private SocketChannel myChannel;
	private final Entity clientEnt;

	// list of contacts of this entity
	private final List<Contact> contactSet;

	public ClientInformation(SocketChannel myChannel, int ID) {
		this.myChannel = myChannel;
		clientEnt = Entity.of(ID, 0, 0); // create entity at pos 0,0 with given ID form server
		contactSet = Collections.synchronizedList(new ArrayList<>()); // more then 1 thread can add to this list
	}

	public Entity getEntity() {
		return clientEnt;
	}

	public List<Contact> getContacts() {
		return contactSet;
	}

	public SocketChannel getChannel() {
		return myChannel;
	}

	public void addContacts(Contact c) {
		contactSet.add(c);
	}
}
