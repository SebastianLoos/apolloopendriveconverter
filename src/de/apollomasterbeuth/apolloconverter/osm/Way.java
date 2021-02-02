package de.apollomasterbeuth.apolloconverter.osm;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class Way {
	public long[] nodeIDs;
	public WayNode[] nodes;
	
	public String type;
	public long id;
	
	public NodeLinks start;
	public NodeLinks end;
	
	public boolean oneway;
	
	public LineString calculateGeometry() {
		if (nodes!=null) {
			return new GeometryFactory().createLineString(Arrays.stream(nodes).map(x->x.geometry.getCoordinate()).toArray(Coordinate[]::new));
		} else {
			return new GeometryFactory().createLineString();
		}
	}
	
	@Override
	public String toString() {
		return id + "";
	}
}
