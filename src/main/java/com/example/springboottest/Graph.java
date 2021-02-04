package com.example.springboottest;

public class Graph {

	private  String graphName;
	private  String currentState;

	public Graph() {
	}
	public Graph(String graphName, String currentState) {
		this.graphName = graphName;
		this.currentState = currentState;
	}


	public  void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	public String getGraphName() {
		return graphName;
	}

	public String getCurrentState() {
		return currentState;
	}

	public  void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

}