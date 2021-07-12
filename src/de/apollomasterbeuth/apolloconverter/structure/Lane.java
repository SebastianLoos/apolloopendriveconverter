package de.apollomasterbeuth.apolloconverter.structure;

import java.util.ArrayList;
import java.util.List;

import de.apollomasterbeuth.apolloconverter.gml.GMLLane;

public class Lane {
	public String uid;
	
	public BorderType borderType;
	
	public List<Geometry> borderGeometry = new ArrayList<Geometry>();
	
	public List<Geometry> centerGeometry = new ArrayList<Geometry>();
	
	public double distanceFromRoad;
	
	public int lane;
	
	public List<LaneLink> links = new ArrayList<LaneLink>();
	
	public GMLLane sourceLane;
}
