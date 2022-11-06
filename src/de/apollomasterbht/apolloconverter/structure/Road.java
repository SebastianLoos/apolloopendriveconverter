package de.apollomasterbht.apolloconverter.structure;

import java.util.ArrayList;
import java.util.List;

public class Road {
	
	public String id;
	
	public List<LaneSection> laneSections = new ArrayList<LaneSection>();
	
	public List<Link> links = new ArrayList<Link>();
	
	public List<Signal> signals = new ArrayList<Signal>();
	
	public NetworkGeometry geometry;
	
}
