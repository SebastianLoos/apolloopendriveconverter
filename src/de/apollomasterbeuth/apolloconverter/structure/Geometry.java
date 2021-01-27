package de.apollomasterbeuth.apolloconverter.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class Geometry {
	public LineString geometry;
	
	public double sOffset;
	
	public double x;
	public double y;
	public double z;
	
	public double length;
	
	public List<Point> points(){
		return Arrays.stream(geometry.getCoordinates()).map(x->new GeometryFactory().createPoint(x)).collect(Collectors.toList());
	}
}
