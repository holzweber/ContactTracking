package server;

import java.util.Scanner;

public class MainServer {

	public static void main(String[] args) {
		TrackingServer server = new TrackingServer();

		Thread shutdownCheck = new Thread(() -> {// implement runnable with lambda
			System.out.println("Started Server");
			System.out.println("Press Enter, in order to shutdown server");
			Scanner inputScanner = new Scanner(System.in);
			inputScanner.nextLine(); // if there is a next line, somebody pressed the enter button
			inputScanner.close();// closes scanner
			server.terminate(); // sets flag in trackingserver to false
		});
		shutdownCheck.setDaemon(true);
		shutdownCheck.start();
		// start server application
		server.start();
	}

}
