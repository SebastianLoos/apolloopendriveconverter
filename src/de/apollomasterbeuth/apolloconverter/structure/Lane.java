package de.apollomasterbeuth.apolloconverter.structure;

import java.util.ArrayList;
import java.util.List;

public class Lane {
	public BorderType borderType;
	
	public Geometry borderGeometry;
	
	public Geometry centerGeometry;
	
	public double distanceFromRoad;
	
	public int lane;
	
	public List<LaneLink> links = new ArrayList<LaneLink>();
}
