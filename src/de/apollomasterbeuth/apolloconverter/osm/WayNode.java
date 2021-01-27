package de.apollomasterbeuth.apolloconverter.osm;

import java.util.ArrayList;
import java.util.List;

public class WayNode extends Node{
	
	public List<WayNodeConnection> ways = new ArrayList<WayNodeConnection>();

	public WayNode(double latitude, double longitude, long id, Way way, int position) {
		super(latitude, longitude, id);
		ways.add(new WayNodeConnection(way, position));
	}
}
