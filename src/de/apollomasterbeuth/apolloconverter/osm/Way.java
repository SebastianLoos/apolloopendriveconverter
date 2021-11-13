package de.apollomasterbeuth.apolloconverter.osm;

public class Way {
	public long[] nodeIDs;
	public WayNode[] nodes;
	
	public String type;
	public long id;
	
	public boolean oneway;
	
	@Override
	public String toString() {
		return id + "";
	}
	
	public int getNodePosition(long nodeID) {
		for (int i=0; i<nodeIDs.length; i++) {
			if (nodeIDs[i]==nodeID) {
				return i;
			}
		}
		return -1;
	}
	
	public int getNodePosition(WayNode node) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i].id==node.id) {
				return i;
			}
		}
		return -1;
	}
}
