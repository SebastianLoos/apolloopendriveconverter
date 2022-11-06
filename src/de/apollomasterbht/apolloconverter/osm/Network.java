package de.apollomasterbht.apolloconverter.osm;

import java.util.Collection;

/**
 * Represents a road network consisting of OSM ways and nodes.
 * @author Hayuki
 *
 */
public class Network {
	/**
	 * OSM ways representing a road.
	 */
	public Collection<Way> roadWays;
	
	/**
	 * OSM nodes referenced in a way representing a road.
	 */
	public Collection<WayNode> roadNodes;
	
	/**
	 * OSM nodes representing a traffic light.
	 */
	public Collection<Node> trafficLightNodes;
}
