package de.apollomasterbeuth.apolloconverter.osm;

public class Way {
	public long[] nodeIDs;
	
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
}
