package de.apollomasterbht.apolloconverter.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents an OSM node part of an OSM way.
 * @author Hayuki
 *
 */
public class WayNode extends Node{
	
	/**
	 * List of all ways this node is a part of.
	 */
	public List<Way> ways = new ArrayList<Way>();
	
	/**
	 * Map of the ways connecting to this node. Ways mapped to "true" begin, ways mapped to "false" end at this node.
	 */
	public HashMap<Boolean, List<Way>> links = new HashMap<Boolean, List<Way>>();
	
	/**
	 * Represents the direction a linked way is connected to the node to.
	 * @author Hayuki
	 *
	 */
	public enum LinkDirection {
		STARTING,
		ENDING
	}
	
	/**
	 * Initializes a new instance of the WayNode class.
	 * @param latitude Latitude of the node.
	 * @param longitude Longitude of the node.
	 * @param id ID of the node.
	 */
	public WayNode(double latitude, double longitude, long id) {
		super(latitude, longitude, id);
	}
	
	/**
	 * Initializes a new instance of the WayNode class.
	 * @param latitude Latitude of the node.
	 * @param longitude Longitude of the node.
	 * @param id ID of the node.
	 * @param way Way the node is a part of.
	 */
	public WayNode(double latitude, double longitude, long id, Way way) {
		super(latitude, longitude, id);
		ways.add(way);
	}
	
	/**
	 * Adds a link to a connected way.
	 * @param direction Direction the way is connected to the node.
	 * @param way Way that is connected to the node.
	 */
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
