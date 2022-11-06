package de.apollomasterbht.apolloconverter.osm;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Represents an OSM way.
 * @author Hayuki
 *
 */
public class Way {
	/**
	 * The nodes forming the way.
	 */
	public WayNode[] nodes;
	
	/**
	 * The type of way.
	 */
	public String type;
	
	/**
	 * The ID of the way.
	 */
	public long id;
	
	/**
	 * Indicates whether the way is a one-way street.
	 */
	public boolean oneway;
	
	@Override
	public String toString() {
		return id + "";
	}
	
	/**
	 * Gets the position of the node with the provided ID in the way.
	 * @param nodeID The ID of the node to get the position of.
	 * @return the position of the node in the way or -1 if a node with the ID is not contained in the way.
	 */
	public int getNodePosition(long nodeID) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i].id==nodeID) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Gets the position of the provided node in the way.
	 * @param node The node to get the position of.
	 * @return the position of the node in the way or -1 if a node with the ID is not contained in the way.
	 */
	public int getNodePosition(WayNode node) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i].id==node.id) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Calculates the geometry of the way.
	 * @return the geometry of the way.
	 */
	public LineString calculateGeometry() {
		if (nodes!=null) {
			return new GeometryFactory().createLineString(Arrays.stream(nodes).map(x->x.geometry.getCoordinate()).toArray(Coordinate[]::new));
		} else {
			return new GeometryFactory().createLineString();
		}
	}
	
	/**
	 * Gets the starting node of the way.
	 * @return the first node of the way.
	 */
	public WayNode getStart() {
		return nodes[0];
	}
	
	/**
	 * Gets the ending node of the way.
	 * @return the last node of the way.
	 */
	public WayNode getEnd() {
		return nodes[nodes.length-1];
	}
}
