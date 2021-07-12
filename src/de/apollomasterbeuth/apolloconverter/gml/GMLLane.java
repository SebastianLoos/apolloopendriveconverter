package de.apollomasterbeuth.apolloconverter.gml;

import org.locationtech.jts.geom.Coordinate;

public class GMLLane extends GMLGeometry{
	
	public GMLLane(Coordinate[] coordinates) {
		super(coordinates);
	}
	
	public double distanceToRoad;
	
	@Override
	public String toString() {
		return super.geometry.toText();
	}
}
