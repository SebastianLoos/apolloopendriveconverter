package de.apollomasterbeuth.apolloconverter.gml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.locationtech.jts.geom.Coordinate;

public class GMLGeometryConnection {
	
	public GMLGeometry geometry;
	
	public Boolean connectToBeginning;
	
	public GMLGeometryConnection(GMLGeometry geometry, Boolean connectToBeginning) {
		this.geometry = geometry;
		this.connectToBeginning = connectToBeginning;
	}
	
	public Coordinate[] getDirectionalCoordinates() {
		Coordinate[] coordinates =  geometry.geometry.getCoordinates();
		if (connectToBeginning) {
			return coordinates;
		} else {
			return IntStream.rangeClosed(1, coordinates.length)
		      .mapToObj(i -> coordinates[coordinates.length - i])
		      .toArray(Coordinate[]::new);
		}
	}
}
