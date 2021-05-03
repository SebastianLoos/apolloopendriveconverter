package de.apollomasterbeuth.apolloconverter.gml;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

public class GMLGeometry {
	
	public GMLLineString geometry;

	public GMLGeometryConnection connection;

	public GMLGeometry(Coordinate[] coordinates) {
		geometry = new GMLLineString(CoordinateArraySequenceFactory.instance().create(coordinates), new GeometryFactory());
		connection = new GMLGeometryConnection();
	}
	
}
