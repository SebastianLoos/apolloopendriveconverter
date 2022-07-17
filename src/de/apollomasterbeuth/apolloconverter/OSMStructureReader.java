package de.apollomasterbeuth.apolloconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import de.apollomasterbeuth.apolloconverter.osm.Network;
import de.apollomasterbeuth.apolloconverter.osm.Node;
import de.apollomasterbeuth.apolloconverter.osm.Way;
import de.apollomasterbeuth.apolloconverter.osm.WayNode;
import de.apollomasterbeuth.apolloconverter.osm.WayNode.LinkDirection;
import de.apollomasterbeuth.logger.Log;
import de.uzl.itm.jaxb4osm.jaxb.NodeElement;
import de.uzl.itm.jaxb4osm.jaxb.OsmElement;
import de.uzl.itm.jaxb4osm.jaxb.WayElement;
import de.uzl.itm.jaxb4osm.tools.OsmUnmarshaller;

public class OSMStructureReader {
	
	// The values a "highway" needs to have in order to be considered a road to be imported into the network.
	
	private static Log log = new Log(OSMStructureReader.class.getName());
	
	/**
	 * Creates a network from a given OSM file.
	 * @param filename The filename of the file containing the OSM data.
	 * @param settings The settings to be used during the creation.
	 * @return A OSM road network.
	 * @throws Exception
	 */
	public static Network createNetwork(String filename, OSMReaderSettings settings) throws Exception {
		OsmElement root = readOSMFile(filename);
		
		Network network = calculateNetwork(root.getWayElements(), root.getNodeElements(), settings);
		log.log("Roads in OSM file: " + network.roadWays.size());
		log.log("Nodes contained in the roads: " + network.roadNodes.size());

		return network;
	}
	
	/**
	 * Reads a file with the specified file name and deserializes it into an OsmElement.
	 * @param filename The file name of the file to deserialize.
	 * @return An OsmElement containing the OSM data of the file.
	 * @throws Exception
	 */
	private static OsmElement readOSMFile(String filename) throws Exception {
		log.log(filename);
		File xmlFile = new File(filename);
		InputStream inputStream = new FileInputStream(xmlFile);
		
		return OsmUnmarshaller.unmarshal(inputStream);
	}
	
	/**
	 * Calculates a road network from OSM elements.
	 * @param wayElements OSM Way elements.
	 * @param nodeElements OSM Node elements.
	 * @return A network consisting of the OSM elements.
	 */
	private static Network calculateNetwork(Collection<WayElement> wayElements, Collection<NodeElement> nodeElements, OSMReaderSettings settings){
		List<Way> roadWays = new ArrayList<Way>();
		List<WayNode> wayNodes = new ArrayList<WayNode>();
		List<Node> trafficLightNodes = getTrafficLightNodes(nodeElements);
		
		wayElements.forEach(wayElement->{
			if (isWayRoad(wayElement, settings.highwayTags)) {
				Way way = new Way();
				List<WayNode> waysTemp = new ArrayList<WayNode>();
				Long[] nodeIDs = wayElement.getNdElements().stream().map(ndElement->ndElement.getReference()).toArray(Long[]::new);
				
				for(int i = 0; i< nodeIDs.length; i++) {
					long currentId = nodeIDs[i];
					Optional<WayNode> nodeMatch = wayNodes.stream().filter(x -> x.id == currentId).findFirst();
					WayNode node = nodeMatch.isPresent() ? nodeMatch.get() : getWayNode(currentId, nodeElements);
					node.ways.add(way);

					if (!nodeMatch.isPresent()) {
						wayNodes.add(node);
					}
					
					if (i==0) {
						node.addLink(LinkDirection.STARTING, way);
					}
					if (i==wayElement.getNdElements().size()-1) {
						node.addLink(LinkDirection.ENDING, way);
					}
					
					waysTemp.add(node);
				}
				way.nodes = waysTemp.toArray(new WayNode[0]);
				way.id = wayElement.getID();
				way.type = getWayType(wayElement);
				way.oneway = wayElement.isOneWay();
				roadWays.add(way);
			}
		});
		
		Network network = new Network();
		network.roadWays = roadWays;
		network.roadNodes = wayNodes;
		network.trafficLightNodes = trafficLightNodes;
		return network;
	}
	
	private static List<Node> getTrafficLightNodes(Collection<NodeElement> nodeElements){
		List<Node> trafficLightNodes = new ArrayList<Node>();
		nodeElements.forEach(node->{
			node.getTags().forEach((k,v)->{
				if ((k.equals("highway"))&&(v.equals("traffic_signals"))) {
					Node trafficLight = new Node(node.getLongitude(), node.getLatitude(), node.getID());
					trafficLightNodes.add(trafficLight);
				}
			});
		});
		return trafficLightNodes;
	}
	
	/**
	 * Gets a new WayNode element of the node with the provided id from a collection of nodes.
	 * @param id id of the node.
	 * @param nodeElements a collection of nodes to 
	 * @return a new WayNode element of the node with the provided id.
	 * @throws IllegalArgumentException when no node with the provided id is in the collection.
	 */
	private static WayNode getWayNode(long id, Collection<NodeElement> nodeElements) throws IllegalArgumentException {
		Optional<NodeElement> optionalNodeElement = nodeElements.stream().filter(nodeElement->nodeElement.getID() == id).findFirst();
		if (optionalNodeElement.isPresent()) {
			NodeElement nodeElement = optionalNodeElement.get();
			return new WayNode(nodeElement.getLongitude(), nodeElement.getLatitude(), nodeElement.getID());
		} else {
			log.error(String.format("Node %d not found", id));
			throw new IllegalArgumentException("No node with the provided id in collection.");
		}
	}
	
	/**
	 * Gets the type of the way from the highway tag.
	 * @param way the way to get the type from.
	 * @return the value of the highway tag from the way.
	 */
	private static String getWayType(WayElement way) {
		if (way.getTags().containsKey("highway")) {
			return way.getTagValue("highway");
		} else {
			return "unknown";
		}
	}
	
	/**
	 * Checks if the provided OSM way is to be considered a road.
	 * @param way OSM way to check.
	 * @return Whether the way is to be considered a road.
	 */
	private static boolean isWayRoad(WayElement way, String[] highwayTags) {
		if (way.getTags().containsKey("highway")) {
			String highwayTagValue = way.getTags().get("highway");
			return Arrays.stream(highwayTags).anyMatch(validHighwayTag-> validHighwayTag.equals(highwayTagValue));
		}
		
		return false;
	}
	
}
