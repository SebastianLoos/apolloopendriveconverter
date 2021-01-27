package de.apollomasterbeuth.apolloconverter.structure;

import java.util.List;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class NetworkGeometry {
	
	public LineString geometry;
	public List<Point> points;

	public NetworkGeometry(LineString geometry, List<Point> points) {
		this.geometry = geometry;
		this.points = points;
	}
}
