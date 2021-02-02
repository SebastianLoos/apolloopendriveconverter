package de.apollomasterbeuth.apolloconverter.osm;

import java.util.HashMap;
import java.util.List;

public class NodeLinks {
	public long id;
	
	public Node node;
	
	//true = start, false = end
	public HashMap<Boolean, List<Way>> links = new HashMap<Boolean, List<Way>>();
}
