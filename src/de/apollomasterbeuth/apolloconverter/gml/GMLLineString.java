package de.apollomasterbeuth.apolloconverter.gml;

import org.apache.lucene.util.SloppyMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class GMLLineString extends LineString{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8356265379562065963L;

	public GMLLineString(CoordinateSequence points, GeometryFactory factory) {
		super(points, factory);
	}

	public double getLength() {
		return calculateDistance(getStartPoint().getCoordinate(), getEndPoint().getCoordinate());
	}
	
	private double calculateDistance(Coordinate start, Coordinate end) {
		double lat1 = start.getX();
		double lon1 = start.getY();
		double lat2 = end.getX();
		double lon2 = end.getY();
		
		return SloppyMath.haversinMeters(lat1, lon1, lat2, lon2);
	}
}
