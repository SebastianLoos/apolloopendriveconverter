package de.apollomasterbeuth.apolloconverter.osm;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class Way {
	public WayNode[] nodes;
	
	public String type;
	public long id;
	
	public boolean oneway;
	
	@Override
	public String toString() {
		return id + "";
	}
	
	public int getNodePosition(long nodeID) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i].id==nodeID) {
				return i;
			}
		}
		return -1;
	}
	
	public int getNodePosition(WayNode node) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i].id==node.id) {
				return i;
			}
		}
		return -1;
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
