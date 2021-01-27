package de.apollomasterbeuth.apolloconverter.gml;

import java.util.ArrayList;
import java.util.List;

public class GMLGeometryConnection {
	
	public List<Connection> predecessors = new ArrayList<Connection>();
	
	public List<Connection> successors = new ArrayList<Connection>();
	
	public void addPredecessor(GMLGeometry geometry, Boolean connectToBeginning) {
		predecessors.add(new Connection(geometry, connectToBeginning));
	}
	
	public void addSuccessor(GMLGeometry geometry, Boolean connectToBeginning) {
		successors.add(new Connection(geometry, connectToBeginning));
	}
	
	private class Connection{
		
		public GMLGeometry geometry;
		
		public Boolean connectToBeginning;
		
		public Connection(GMLGeometry geometry, Boolean connectToBeginning) {
			this.geometry = geometry;
			this.connectToBeginning = connectToBeginning;
		}
	}

}
