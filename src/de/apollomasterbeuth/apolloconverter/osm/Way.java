package de.apollomasterbeuth.apolloconverter.osm;

public class Way {
	public long[] nodeIDs;
	public WayNode[] nodes;
	
	public String type;
	public long id;
	
	public NodeLinks start;
	public NodeLinks end;
	
	public boolean oneway;
	
	@Override
	public String toString() {
		return id + "";
	}
}
