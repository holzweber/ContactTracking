package server;

import static basics.Constants.CONTACT_DISTANCE;
import static basics.Constants.THRESHHOLD_AREA;
import static basics.Constants.THRESHHOLD_ENTITIES;
import static basics.Constants.SPLIT;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import basics.Area;
import basics.Contact;
import basics.Entity;

@SuppressWarnings("serial")
public class ContactRecursiveTask extends RecursiveTask<Set<Contact>> {

	private final Area area;
	
	public ContactRecursiveTask(Area area) {
		this.area = area;
	}

	@Override
	protected Set<Contact> compute() {
		// check if area is small enough, or it it has not a lot of entities
		if (hasFewEntities(area) || isSmallEnough(area) ) {
			// here we do a normal pairwise check and return the local hashmap
			Set<Contact> contacts = new HashSet<Contact>();
			for (Entity e1 : area.getEntities()) {
				for (Entity e2 : area.getEntities()) {
					if (!e1.equals(e2)) {
						if (e1.distance(e2) < CONTACT_DISTANCE) {
							Contact c = Contact.of(e1, e2);
							contacts.add(c);
						}
					}
				}
			}
			return contacts;
		} else {
			int i = 0; //local counter for array
			
			//here all the running tasks get stored
			ContactRecursiveTask tasks[] = new ContactRecursiveTask[SPLIT*SPLIT];
			
			for(Area a : area.split(SPLIT)) {
				tasks[i] = new ContactRecursiveTask(a);
				tasks[i].fork();
				i++;
			}
			
			//here the contacts of each task get stored
			Set<Contact> contacts = new HashSet<Contact>();
			
			//put all contacts into the list
			for(ContactRecursiveTask task : tasks) {
				contacts.addAll(task.join());
			}
			return contacts;
		}
	}

	private boolean hasFewEntities(Area area) {
		return area.getEntities().size() <= THRESHHOLD_ENTITIES;
	}

	private boolean isSmallEnough(Area area) {
		return area.getWidth() <= THRESHHOLD_AREA;
	}

}
