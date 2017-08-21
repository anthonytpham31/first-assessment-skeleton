package com.cooksys.assessment.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.cooksys.assessment.server.ClientHandler;

public class MessageCenter {

	private Map<String, ClientHandler> userMap = new HashMap<>();
	private UserCenter userCenter;
	
	public MessageCenter(Map<String, ClientHandler> userMap) {
		super();
		this.userMap = userMap;
		this.userCenter = new UserCenter(this.userMap);
	}

	public void sendMessageToAllUsers(String messageToSend) {
		for (ClientHandler targetUser : userMap.values()) {
			sendMessageToUser(targetUser, messageToSend);
		}
	}

	public void sendMessageToUser(ClientHandler targetUser, String messageToSend) {
		targetUser.getWriter().write(messageToSend);
	}
	
	public void sendDirectMessageToUser(ClientHandler targetUser, String messageToSend, Message message) {
		
		message.setTimestamp(new Date().toString());

	}
	
}
