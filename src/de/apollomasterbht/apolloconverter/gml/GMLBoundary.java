package de.apollomasterbht.apolloconverter.gml;


import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a road boundary from the 2014 Berlin road inspection.
 * @author Hayuki
 *
 */
public class GMLBoundary extends GMLGeometry {
	
	/**
	 * Initializes a new instance of the GMLBoundary class.
	 * @param coordinates Coordinates composing the geometry of the boundary.
	 */
	public GMLBoundary(Coordinate[] coordinates) {
		super(coordinates);
	}

}
