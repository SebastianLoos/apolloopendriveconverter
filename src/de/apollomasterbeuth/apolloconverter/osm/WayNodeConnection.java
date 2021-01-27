package de.apollomasterbeuth.apolloconverter.osm;

public class WayNodeConnection {
	public Way way;
	public int position;
	
	public WayNodeConnection(Way way, int position) {
		this.way = way;
		this.position = position;
	}
}
