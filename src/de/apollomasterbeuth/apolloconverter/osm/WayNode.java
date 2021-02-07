package de.apollomasterbeuth.apolloconverter.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WayNode extends Node{
	
	public List<Way> ways = new ArrayList<Way>();
	
	public HashMap<Boolean, List<Way>> links = new HashMap<Boolean, List<Way>>();

	public WayNode(double latitude, double longitude, long id, Way way) {
		super(latitude, longitude, id);
		ways.add(way);
	}
}
