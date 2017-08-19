package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private static Map<String, ClientHandler> testMap = new HashMap<>();	
	private Map<String, ClientHandler> userMap = new HashMap<>();
	private PrintWriter writer;

	public ClientHandler(Socket socket) throws IOException {
		super();
		this.socket = socket;
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			while (!socket.isClosed()) {
				
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				if (message.getCommand().charAt(0) == '@') {
					log.info("user <{}> whispered to <{}> message <{}>", message.getUsername(), message.getCommand(), message.getContents());
					
					String username = message.getCommand().substring(1);
					message.setTimestamp(new Date().toString());
					String whisperMessage = mapper.writeValueAsString(message);
					String failedMessage = "User is not in this chat room";
					
					if (userExists(username)) {
						sendDirectMessageToUser(testMap.get(username), whisperMessage);
					} else {
						sendDirectMessageToUser(testMap.get(message.getUsername()), failedMessage);
					}
					
				} else {
					switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						
						message.setTimestamp(new Date().toString());
						message.setContents(message.getUsername() + " has connected");
						String connectAlert = mapper.writeValueAsString(message);
						sendMessageToAllUsers(connectAlert);
						System.out.println(connectAlert + " connect message");
						if(!userExists(message.getUsername())) {
							addUser(message.getUsername(), this);
						} else {
							message.setContents("Username <" + message.getUsername() + "> is taken, please choose another");
							String userExist = mapper.writeValueAsString(message);
							System.out.println(userExist + " should not show this");
							writer.write(userExist);
							writer.flush();
							this.socket.close();
						}
						
						break;
						
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						
						message.setTimestamp(new Date().toString());
						message.setContents(message.getUsername() + " has disconnected");
						String disconnectAlert = mapper.writeValueAsString(message);
						sendMessageToAllUsers(disconnectAlert);
						
						removeUser(message.getUsername(), this);
						
						this.socket.close();
						break;
						
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						message.setTimestamp(new Date().toString());
						String response = mapper.writeValueAsString(message);
						sendDirectMessageToUser(testMap.get(message.getUsername()), response);
						break;
						
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						
						message.setTimestamp(new Date().toString());
						String responseBroadcast = mapper.writeValueAsString(message);
						sendMessageToAllUsers(responseBroadcast);
						break;
						
					case "users":
						log.info("user <{}> requested User List : <{}>", message.getUsername(), testMap.keySet());
						System.out.println(getAllUsers());
						message.setTimestamp(new Date().toString());
						message.setContents("currently connected users: " + getAllUsers());
						String full = mapper.writeValueAsString(message);
						sendDirectMessageToUser(testMap.get(message.getUsername()), full);
						System.out.println(full);
						break;
					}
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	// Message methods; Might need to move to another class for readability
	
	private void sendMessageToAllUsers(String messageToSend) {
        for (ClientHandler targetUser : testMap.values()) {
			sendDirectMessageToUser(targetUser, messageToSend);
        }
    }

    private void sendDirectMessageToUser(ClientHandler targetUser, String messageToSend) {
    	targetUser.writer.write(messageToSend);
    }
    
    
    private void addUser(String userName, ClientHandler client) {
        testMap.put(userName, client);
    }
    
    private void removeUser(String username, ClientHandler client) {
    	testMap.remove(username, client);
    }
    
    private List<String> getAllUsers() {
        List<String> currentActiveUsers = new ArrayList<>(testMap.keySet());
        return currentActiveUsers;
    }

    private boolean userExists(String username) {
        return testMap.containsKey(username);
    }


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

