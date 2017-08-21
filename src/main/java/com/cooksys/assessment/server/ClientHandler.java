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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cooksys.assessment.model.Message;
import com.cooksys.assessment.model.MessageCenter;
import com.cooksys.assessment.model.UserCenter;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;	
	private PrintWriter writer;
	private Map<String, ClientHandler> userMap = new HashMap<>();
	
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
				MessageCenter messageCenter = new MessageCenter(userMap);
				UserCenter userCenter = new UserCenter(userMap);
				
				if (message.getCommand().charAt(0) == '@') {
					log.info("user <{}> whispered to <{}> message <{}>", message.getUsername(), message.getCommand(), message.getContents());
					
					String username = message.getCommand().substring(1);
					message.setTimestamp(new Date().toString());
					String whisperMessage = mapper.writeValueAsString(message);
					String failedMessage = "User is not in this chat room";
					
					if (userCenter.userExists(username)) {
						sendDirectMessageToUser(userMap.get(username), whisperMessage);
					} else {
						sendDirectMessageToUser(userMap.get(message.getUsername()), failedMessage);
					}
					
				} else {
					switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						
						message.setTimestamp(new Date().toString());
						message.setContents(message.getUsername() + " has connected");
						String connectAlert = mapper.writeValueAsString(message);
						sendMessageToAllUsers(connectAlert);
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
						messages.sendDirectMessageToUser(userMap.get(message.getUsername()), response);
						break;
						
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						
						message.setTimestamp(new Date().toString());
						String responseBroadcast = mapper.writeValueAsString(message);
						messages.sendMessageToAllUsers(responseBroadcast);
						break;
						
					case "users":
						log.info("user <{}> requested User List : <{}>", message.getUsername(), userMap.keySet());
						System.out.println(users.getAllUsers());
						message.setTimestamp(new Date().toString());
						message.setContents("currently connected users: " + users.getAllUsers());
						String full = mapper.writeValueAsString(message);
						messages.sendDirectMessageToUser(userMap.get(message.getUsername()), full);
						break;
					}
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}
	
	
}

