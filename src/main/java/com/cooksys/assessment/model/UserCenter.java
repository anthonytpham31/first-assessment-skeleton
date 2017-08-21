package com.cooksys.assessment.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cooksys.assessment.server.ClientHandler;

public class UserCenter {
	
	private Map<String, ClientHandler> userMap;
	
	public UserCenter(Map<String, ClientHandler> userMap) {
		this.userMap = userMap;
	}

	public void addUser(String userName, ClientHandler client) {
		userMap.put(userName, client);
	}

	public void removeUser(String userName, ClientHandler client) {
		userMap.remove(userName, client);
	}

	public List<String> getAllUsers() {
		List<String> currentActiveUsers = new ArrayList<String>(userMap.keySet());
		return currentActiveUsers;
	}

	public boolean userExists(String username) {
		return userMap.containsKey(username);
	}
}
