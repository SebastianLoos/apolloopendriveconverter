package de.apollomasterbeuth.apolloconverter.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WayNode extends Node{
	
	public List<Way> ways = new ArrayList<Way>();
	
	public HashMap<Boolean, List<Way>> links = new HashMap<Boolean, List<Way>>();
	
	public enum LinkDirection {
		STARTING,
		ENDING
	}

	public WayNode(double latitude, double longitude, long id) {
		super(latitude, longitude, id);
	}
	
	public WayNode(double latitude, double longitude, long id, Way way) {
		super(latitude, longitude, id);
		ways.add(way);
	}
	
	public void addLink(LinkDirection direction, Way way) {
		if (direction == LinkDirection.STARTING) {
			if (links.containsKey(true)) {
				links.get(true).add(way);
			} else {
				List<Way> wayList = new ArrayList<Way>();
				links.put(true, wayList);
			}
		} else if (direction == LinkDirection.ENDING) {
			if (links.containsKey(false)) {
				links.get(false).add(way);
			} else {
				List<Way> wayList = new ArrayList<Way>();
				links.put(false, wayList);
			}
		}
	}
}
