package de.apollomasterbeuth.apolloconverter.osm;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class Road {
	public Way way;
	public WayNode[] nodes;
	
	public Road(Way way, WayNode[] nodes) {
		this.way = way;
		this.nodes = nodes;
	}
	
	public LineString calculateGeometry() {
		if (nodes!=null) {
			return new GeometryFactory().createLineString(Arrays.stream(nodes).map(x->x.geometry.getCoordinate()).toArray(Coordinate[]::new));
		} else {
			return new GeometryFactory().createLineString();
		}
	}
	
	public WayNode getStart() {
		return nodes[0];
	}
	
	public WayNode getEnd() {
		return nodes[nodes.length-1];
	}
}
