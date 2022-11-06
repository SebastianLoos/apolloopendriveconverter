package de.apollomasterbht.apolloconverter.gml;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.locationtech.jts.geom.Point;

public class GMLGeometryPrototype {
	public Deque<Point> points = new ArrayDeque<Point>();
	
	public List<GMLGeometryConnection> predecessors = new ArrayList<GMLGeometryConnection>();
	
	public List<GMLGeometryConnection> successors = new ArrayList<GMLGeometryConnection>();
}
