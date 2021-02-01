package com.example.springboottest;

public class Graph {

	public String getGraphName() {
		return graphName;
	}

	public String getCurrentState() {
		return currentState;
	}

	private final String graphName;
	private final String currentState;

	public Graph(String graphName, String currentState) {
		this.graphName = graphName;
		this.currentState = currentState;
	}


}