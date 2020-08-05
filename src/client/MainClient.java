package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MainClient {

	public static void main(String[] args) {
		TrackingClient client = new TrackingClient();
		try {
			//put client online
			client.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
