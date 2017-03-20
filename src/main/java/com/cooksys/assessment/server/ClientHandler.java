package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private static HashMap<PrintWriter, String> testMap = new HashMap<>();	
	// synchronize
	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			while (!socket.isClosed()) {
				
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				if (message.getCommand().charAt(0) == '@') {
					log.info("user <{}> whispered to <{}>", message.getUsername(), message.getCommand());
					
					for(Entry<PrintWriter, String> whisperTest : testMap.entrySet()) {
						if (message.getCommand().equals("@"+whisperTest.getValue())){
							message.setTimestamp(new Date().toString());
							
							String whisperMessage = mapper.writeValueAsString(message);
							
							whisperTest.getKey().write(whisperMessage);
							whisperTest.getKey().flush();
						} 
					}
					
				} else {
					switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						
						if(!testMap.containsValue(message.getUsername())) {
							testMap.put(writer, message.getUsername());
						}
						
						for (Entry<PrintWriter, String> writeAlerts : testMap.entrySet()) {
							message.setTimestamp(new Date().toString());
							message.setContents(message.getUsername() + " has connected");
							String alert = mapper.writeValueAsString(message);
							writeAlerts.getKey().write(alert);
							writeAlerts.getKey().flush();
						}
						
						break;
						
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						
						testMap.remove(writer, message.getUsername());
						
						for (Entry<PrintWriter, String> writeAlerts : testMap.entrySet()) {
							message.setTimestamp(new Date().toString());
							message.setContents(message.getUsername() + " has disconnected");
							String alert = mapper.writeValueAsString(message);
							writeAlerts.getKey().write(alert);
							writeAlerts.getKey().flush();
						}

						this.socket.close();
						break;
						
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						
						message.setTimestamp(new Date().toString());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
						
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						
						for (Entry<PrintWriter, String> writeBroad : testMap.entrySet()) {
							message.setTimestamp(new Date().toString());
							String responseBroadcast = mapper.writeValueAsString(message);
							writeBroad.getKey().write(responseBroadcast);
							writeBroad.getKey().flush();
						}

						break;
						
					case "users":
						log.info("user <{}> requested User List :", message.getUsername());
						
						message.setTimestamp(new Date().toString());
						message.setContents("currently connected users: " + testMap.values().toString());
						String full = mapper.writeValueAsString(message);
						writer.write(full);
						writer.flush();
						
						break;
					}
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
}

