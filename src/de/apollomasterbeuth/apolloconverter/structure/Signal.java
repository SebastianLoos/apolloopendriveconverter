package de.apollomasterbeuth.apolloconverter.structure;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;


public class Signal {
	public String id;
	public String type;
	public String layoutType;
	
	public List<Point> outline = new ArrayList<Point>();
	public List<SubSignal> subSignals = new ArrayList<SubSignal>();
	public List<String> stopLines = new ArrayList<String>();
}
