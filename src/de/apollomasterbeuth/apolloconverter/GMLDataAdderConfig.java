package de.apollomasterbeuth.apolloconverter;

public class GMLDataAdderConfig {
	/**
	 * Size of the buffer zone surrounding a geometry for limiting the search radius when finding the nearest road.
	 */
	public double nearestRoadBufferSize;
	
	/**
	 * Size of the buffer zone surrounding the end points of road boundary for limiting the search radius when finding boundaries to merge.
	 */
	public double mergeBoundariesBufferSize;
	
	/**
	 * Threshold of percentage of variation when comparing the distance of two lanes to a road after which they will considered to be separate lanes.
	 */
	public double separateLaneThreshold;
}
