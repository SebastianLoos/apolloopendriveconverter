package de.apollomasterbeuth.apolloconverter.gml;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

import de.apollomasterbeuth.apolloconverter.structure.Road;

/**
 * Represents a geometry derived from an GML file.
 * @author Hayuki
 *
 */
public class GMLGeometry {
	
	/**
	 * The geometry information stored in the GML file.
	 */
	public GMLLineString geometry;
	
	/**
	 * Connections to other GML geometries connecting at the start of the geometry.
	 */
	public List<GMLGeometryConnection> predecessors = new ArrayList<GMLGeometryConnection>();
	
	/**
	 * Connections to other GML geometries connecting at the end of the geometry.
	 */
	public List<GMLGeometryConnection> successors = new ArrayList<GMLGeometryConnection>();
	
	/**
	 * Road the GML geometry has been assigned to.
	 */
	public Road connectedRoad;
	
	/**
	 * ID of the geometry.
	 */
	public String uid;

	/**
	 * Initializes a new instance of the GMLGeometry class.
	 * @param coordinates Coordinates of the geometry.
	 */
	public GMLGeometry(Coordinate[] coordinates) {
		geometry = new GMLLineString(CoordinateArraySequenceFactory.instance().create(coordinates), new GeometryFactory());
		uid = UUID.randomUUID().toString();
	}
	
}
