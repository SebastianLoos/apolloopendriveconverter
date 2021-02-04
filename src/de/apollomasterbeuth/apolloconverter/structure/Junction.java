package de.apollomasterbeuth.apolloconverter.structure;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;

import de.apollomasterbeuth.apolloconverter.osm.WayNode;

public class Junction {
	public WayNode node;
	
	public List<Point> outline = new ArrayList<Point>();
}
