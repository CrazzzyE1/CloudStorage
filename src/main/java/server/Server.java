package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
	private static final Logger LOGGER = LogManager.getLogger(Server.class.getName());
	public Server() {
		ExecutorService service = Executors.newFixedThreadPool(4);
		try (ServerSocket server = new ServerSocket(1235)){
			LOGGER.info("Server started");
			while (true) {
				service.execute(new ClientHandler(server.accept()));
				LOGGER.info("Client connected");
			}
		} catch (IOException e) {
//			e.printStackTrace();
			LOGGER.info("Something went wrong");
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
