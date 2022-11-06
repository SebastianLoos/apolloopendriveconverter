package de.apollomasterbht.apolloconverter.osm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Represents an OSM node.
 * @author Hayuki
 *
 */
public class Node {
	/**
	 * Geometry of the node.
	 */
	public Point geometry;
	
	/**
	 * ID of the node.
	 */
	public long id;
	
	/***
	 * Initializes a new instance of the Node class.
	 * @param latitude Latitude of the node.
	 * @param longitude Longitude of the node.
	 * @param id ID of the node.
	 */
	public Node(double latitude, double longitude, long id) {
		this.geometry = new GeometryFactory().createPoint(new Coordinate(longitude, latitude));
		this.id = id;
	}
	
	/**
	 * Gets the geometry of the node.
	 * @return the geometry of the node.
	 */
	public Point getGeometry() {
		return geometry;
	}
	
	/**
	 * Sets the geometry of the node.
	 * @param geometry The geometry of the node.
	 */
	public void setGeometry(Point geometry) {
		this.geometry = geometry;
	}
	
	/**
	 * Gets the ID of the node.
	 * @return the ID of the node.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Sets the ID of the node.
	 * @param id The ID of the node.
	 */
	public void setId(long id) {
		this.id = id;
	}
}
