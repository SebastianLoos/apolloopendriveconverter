package de.apollomasterbeuth.apolloconverter.osm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class Node {
	public Point geometry;
	public long id;
	
	public Node(double latitude, double longitude, long id) {
		this.geometry = new GeometryFactory().createPoint(new Coordinate(longitude, latitude));
		this.id = id;
	}
	
	public Point getGeometry() {
		return geometry;
	}
	public void setGeometry(Point geometry) {
		this.geometry = geometry;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
}
