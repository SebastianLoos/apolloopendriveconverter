package de.apollomasterbht.apolloconverter.gml;

import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a road lane from the 2014 Berlin road inspection.
 * @author Hayuki
 *
 */
public class GMLLane extends GMLGeometry{
	
	/**
	 * Initializes a new instance of the GMLLane class.
	 * @param coordinates Coordinates composing the geometry of the lane.
	 */
	public GMLLane(Coordinate[] coordinates) {
		super(coordinates);
	}
	
	/**
	 * The distance from the center of the geometry to the center of the assigned road.
	 */
	public double distanceToRoad;
	
	@Override
	public String toString() {
		return super.geometry.toText();
	}
}
