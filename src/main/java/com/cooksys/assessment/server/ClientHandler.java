package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private String timeStamp = new Date().toString();
	private static HashMap<PrintWriter, String> testMap = new HashMap<>();	
	private String commandCounter;
	
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
							message.setTimestamp(timeStamp);
							String whisperMessage = mapper.writeValueAsString(message);
							whisperTest.getKey().write(whisperMessage);
							whisperTest.getKey().flush();
						} 
					}
					commandCounter = message.getCommand();
					
				} else {
					switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						
						for (Entry<PrintWriter, String> writeAlerts : testMap.entrySet()) {
							message.setTimestamp(timeStamp);
							message.setContents(message.getUsername() + " has connected");
							String alert = mapper.writeValueAsString(message);
							System.out.println(alert);
							writeAlerts.getKey().write(alert);
							writeAlerts.getKey().flush();
						}
						
						if(!testMap.containsValue(message.getUsername())) {
							testMap.put(writer, message.getUsername());
						}
						break;
						
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						
						for (Entry<PrintWriter, String> writeAlerts : testMap.entrySet()) {
							message.setTimestamp(timeStamp);
							message.setContents(message.getUsername() + " has disconnected");
							String alert = mapper.writeValueAsString(message);
							writeAlerts.getKey().write(alert);
							writeAlerts.getKey().flush();
						}

						testMap.remove(writer, message.getUsername());
						this.socket.close();
						break;
						
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						
						message.setTimestamp(timeStamp);
						String response = mapper.writeValueAsString(message);
						System.out.println(response);
						writer.write(response);
						writer.flush();
						commandCounter = message.getCommand();
						break;
						
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						
						for (Entry<PrintWriter, String> writeBroad : testMap.entrySet()) {
							message.setTimestamp(timeStamp);
							String responseBroadcast = mapper.writeValueAsString(message);
							writeBroad.getKey().write(responseBroadcast);
							writeBroad.getKey().flush();
						}
						commandCounter = message.getCommand();
						break;
						
					case "users":
						log.info("user <{}> requested User List :", message.getUsername());
						
						message.setTimestamp(timeStamp);
						message.setContents("currently connected users: " + testMap.values().toString());
						String full = mapper.writeValueAsString(message);
						writer.write(full);
						writer.flush();
						
						commandCounter = message.getCommand();
						break;
					}
				}
				if (message.getCommand() != "echo" || message.getCommand() != "broadcast" || message.getCommand() != "users") {
					message.setTimestamp(timeStamp);
					String commandIsContent = message.getCommand() + message.getContents();
					message.setCommand(commandCounter);
					message.setContents(commandIsContent);
					String full = mapper.writeValueAsString(message);
					writer.write(full);
					writer.flush();
					commandCounter = message.getCommand();
				}
			}	
				

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
