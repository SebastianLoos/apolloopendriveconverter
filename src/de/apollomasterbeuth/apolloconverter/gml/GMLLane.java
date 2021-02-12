package de.apollomasterbeuth.apolloconverter.gml;

import org.locationtech.jts.geom.Coordinate;

public class GMLLane extends GMLGeometry{
	
	public GMLGeometryConnection connection;

	public GMLLane(Coordinate[] coordinates) {
		super(coordinates);
		connection = new GMLGeometryConnection();
	}
	
}
