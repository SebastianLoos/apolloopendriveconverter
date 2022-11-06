package de.apollomasterbht.apolloconverter.gml;

import org.apache.lucene.util.SloppyMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Represents a LineString in an GML environment.
 * @author Hayuki
 *
 */
public class GMLLineString extends LineString{
	
	/**
	 * Randomly generated UID.
	 */
	private static final long serialVersionUID = 8356265379562065963L;
	
	/**
	 * Initializes a new instance of the GMLLineString class.
	 * @param points Points constructing the LineString.
	 * @param factory JTS Geometry factory to use for generating the geometry.
	 */
	public GMLLineString(CoordinateSequence points, GeometryFactory factory) {
		super(points, factory);
	}
	
	/**
	 * Gets the length of the LineString in meters.
	 */
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
