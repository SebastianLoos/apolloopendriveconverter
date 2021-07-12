package de.apollomasterbeuth.apolloconverter.gml;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

import de.apollomasterbeuth.apolloconverter.structure.Road;

public class GMLGeometry {
	
	public GMLLineString geometry;

	public List<GMLGeometryConnection> predecessors = new ArrayList<GMLGeometryConnection>();
	
	public List<GMLGeometryConnection> successors = new ArrayList<GMLGeometryConnection>();
	
	public Road connectedRoad;
	
	public String uid;

	public GMLGeometry(Coordinate[] coordinates) {
		geometry = new GMLLineString(CoordinateArraySequenceFactory.instance().create(coordinates), new GeometryFactory());
		uid = UUID.randomUUID().toString();
	}
	
}
