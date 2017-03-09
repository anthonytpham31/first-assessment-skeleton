package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			List<String> allUsers = new ArrayList<>();
			
			HashMap<String, String> userMap = new HashMap<>();
			
			while (!socket.isClosed()) {
				
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				allUsers.add(socket.getRemoteSocketAddress().toString());
				
				userMap.put(socket.getRemoteSocketAddress().toString(), message.getUsername());
				
				
				System.out.println(userMap);
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						message.setTimeStamp(new Date().toString());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						System.out.println(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						// Run through multiple users
						String responseBroadcast = mapper.writeValueAsString(message);
						writer.write(responseBroadcast);
						writer.flush();
						break;
					case "whispers":
						log.info("user <{}> whispered message <{}>", message.getUsername(), message.getContents());
						String whisper = mapper.writeValueAsString(message);
						writer.write(whisper);
						writer.flush();
						break;
					case "users":
						log.info("user <{}> User's List <{}>", message.getUsername(), message.getContents());
						String userLists = mapper.writeValueAsString(message);
						writer.write(userLists);
						writer.flush();
						break;

//					case "": // This is not needed for now, Could be used for something else; possibly repeat commands
//						log.info("user <{}> No Command", message.getUsername());
//						String noCommand = "Please Enter A Command";
//						writer.write(noCommand);
//						writer.flush();
//						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
