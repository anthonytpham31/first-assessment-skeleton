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
	private static HashSet<PrintWriter> writers = new HashSet<>();
	private static List<String> socketList = new ArrayList<>();
	
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
				writers.add(writer);
				
				if(!socketList.contains(message.getUsername())) {
					socketList.add(message.getUsername());
				}

				if (message.getCommand().charAt(0) == '@') {
					message.setTimeStamp(new Date().toString());
					message.setContents("In Construction");
					String whisperMessage = mapper.writeValueAsString(message);
					writer.write(whisperMessage);
					writer.flush();
					
				} else {
					switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						
						for (PrintWriter writeAlerts : writers) {
							message.setTimeStamp(new Date().toString());
							message.setContents(message.getUsername() + " has connected");
							String alert = mapper.writeValueAsString(message);
							writeAlerts.write(alert);
							writeAlerts.flush();
						}
						
						break;
						
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						
						for (PrintWriter writeAlerts : writers) {
							message.setTimeStamp(new Date().toString());
							message.setContents(message.getUsername() + " has disconnected");
							String alert = mapper.writeValueAsString(message);
							writeAlerts.write(alert);
							writeAlerts.flush();
						}
						
						this.socket.close();
						break;
						
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						
						message.setTimeStamp(new Date().toString());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
						
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						
						for (PrintWriter writeBroad : writers) {
							message.setTimeStamp(new Date().toString());
							String responseBroadcast = mapper.writeValueAsString(message);
							System.out.println(responseBroadcast);
							writeBroad.write(responseBroadcast);
							writeBroad.flush();
						}

						break;
						
					case "users":
						log.info("user <{}> requested User List :", message.getUsername());
						
						
						message.setTimeStamp(new Date().toString());
						message.setContents("currently connected users: " + socketList.toString());
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
