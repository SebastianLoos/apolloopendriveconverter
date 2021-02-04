package de.apollomasterbeuth.apolloconverter.osm;

import java.util.List;

public class Road {
	public Way way;
	public List<WayNode> nodes;
	
	public WayNode getStart() {
		return nodes.get(0);
	}
	
	public WayNode getEnd() {
		return nodes.get(nodes.size()-1);
	}
}
