package de.apollomasterbeuth.apolloconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import de.apollomasterbeuth.apolloconverter.osm.Network;
import de.apollomasterbeuth.apolloconverter.osm.Node;
import de.apollomasterbeuth.apolloconverter.osm.Way;
import de.apollomasterbeuth.apolloconverter.osm.WayNode;
import de.apollomasterbeuth.apolloconverter.osm.WayNode.LinkDirection;
import de.apollomasterbeuth.apolloconverter.structure.BorderType;
import de.apollomasterbeuth.apolloconverter.structure.Center;
import de.apollomasterbeuth.apolloconverter.structure.Environment;
import de.apollomasterbeuth.apolloconverter.structure.Geometry;
import de.apollomasterbeuth.apolloconverter.structure.Junction;
import de.apollomasterbeuth.apolloconverter.structure.LaneSection;
import de.apollomasterbeuth.apolloconverter.structure.Link;
import de.apollomasterbeuth.apolloconverter.structure.NetworkGeometry;
import de.apollomasterbeuth.apolloconverter.structure.Road;
import de.apollomasterbeuth.apolloconverter.structure.Signal;
import de.apollomasterbeuth.logger.Log;
import de.uzl.itm.jaxb4osm.jaxb.NodeElement;
import de.uzl.itm.jaxb4osm.jaxb.OsmElement;
import de.uzl.itm.jaxb4osm.jaxb.WayElement;
import de.uzl.itm.jaxb4osm.tools.OsmUnmarshaller;

public class OSMStructureReader {
	
	// The values a "highway" needs to have in order to be considered a road to be imported into the network.
	public static final String[] highwayTags = {"primary","secondary","tertiary","residential","service","unclassified"};
	
	private static Log log = new Log(OSMStructureReader.class.getName());
	
	/**
	 * Creates a new road network environment from an OSM file with the specified file name.
	 * @param filename The file name of the OSM file to load the data from.
	 * @param settings The settings to use during the import of the data.
	 * @return An OSM road network environment.
	 */
	public static Environment createEnvironment(String filename, OSMReaderSettings settings) {
		Environment environment = new Environment();
		
		try {
			OsmElement root = readOSMFile(filename);
			
			Network network = calculateNetwork(root.getWayElements(), root.getNodeElements());
			log.log("Roads in OSM file: " + network.roadWays.size());
			log.log("Nodes contained in the roads: " + network.roadNodes.size());
			
			return createEnvironment(network, settings);
		}
		catch(Exception e) {
			log.log("Error while creating environment:");
			e.printStackTrace();
		}
		
		return environment;

	}
	
	/**
	 * Creates a new road network environment from a given OSM road network.
	 * @param network The OSM network to be incorporated into the environment.
	 * @param settings The settings to be used during the creation.
	 * @return A road network environment including the OSM road network.
	 */
	public static Environment createEnvironment(Network network, OSMReaderSettings settings) {
		Environment environment = new Environment();
		
		try {
			splitRoads(network.roadNodes, network.roadWays);
			log.log("Roads after split: " + network.roadWays.size());
			
			environment.junctions = getJunctions(network.roadNodes, 0.0001);
			log.log("Junctions: " + environment.junctions.size());
			
			environment.roads.addAll(getRoads(network.roadWays, network.trafficLightNodes));
			
			environment.roads.forEach(road->{
				addLinkToRoad(road, network.roadWays, environment.roads);
			});	
		}
		catch(Exception e) {
			log.log("Error while creating environment:");
			e.printStackTrace();
		}
		
		return environment;
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
	 * Links a road to other roads based on the links from the OSM way.
	 * @param road Road to add the links to.
	 * @param osmRoads Collection of OSM ways to get the links from.
	 * @param roads Collection of roads to select the roads to link the road to.
	 */
	private static void addLinkToRoad(Road road, Collection<Way> osmRoads, List<Road> roads) {
		osmRoads.stream().filter(x->Long.toString(x.id).equals(road.id)).findFirst().ifPresent(way->{
			way.getStart().links.forEach((k,v)->{
				v.forEach(linkedWay->{
					if (!Long.toString(linkedWay.id).equals(road.id)) {
						roads.stream().filter(x->x.id.equals(Long.toString(linkedWay.id))).findFirst().ifPresent(predecessor->{
							Link link = new Link();
							link.contactPoint = k;
							link.type = true;
							link.linkedRoad = predecessor;
							road.links.add(link);
						});
					}
				});
			});
			way.getEnd().links.forEach((k,v)->{
				v.forEach(linkedWay->{
					if (!Long.toString(linkedWay.id).equals(road.id)) {
						roads.stream().filter(x->x.id.equals(Long.toString(linkedWay.id))).findFirst().ifPresent(successor->{
							Link link = new Link();
							link.contactPoint = k;
							link.type = false;
							link.linkedRoad = successor;
							road.links.add(link);
						});
					}
				});
			});
		});
	}
	
	
	private static List<Junction> getJunctions(Collection<WayNode> nodes, double junctionAreaSize){
		List<Junction> junctions = new ArrayList<Junction>();
		nodes.stream().filter(x->x.links.entrySet().stream().map(y->y.getValue().size()).reduce(0, (a, b) -> a+b) > 1).forEach(node->{
			Junction junction = new Junction();
			junction.node = node;
			Point center = node.geometry;
			
			Arrays.stream(center.buffer(junctionAreaSize, 1).getCoordinates()).map(x->new GeometryFactory().createPoint(x)).collect(Collectors.toList()).forEach(point->{
				junction.outline.add(point);
			});
			junctions.add(junction);
		});
		
		return junctions;
	}
	
	private static List<Node> getNodesOnWay(Road road, Collection<Node> nodes){
		List<Node> nodesOnWay = new ArrayList<Node>();
		
		org.locationtech.jts.geom.Geometry bufferZone = road.geometry.geometry.buffer(0.00002);
		
		nodes.forEach(node->{
			if (bufferZone.contains(node.geometry)) {
				nodesOnWay.add(node);
			}
		});
		
		return nodesOnWay;
	}
	
	private static List<Road> getRoads(Collection<Way> osmRoads, Collection<Node> trafficLightNodes){
		List<Road> roads = new ArrayList<Road>();
		
		osmRoads.forEach(osmRoad->{			
			Road road = new Road();
			road.id = Long.toString(osmRoad.id);
			
			Stream<Point> points = Arrays.stream(osmRoad.nodes).map(x->x.geometry);
			LineString lineString = new GeometryFactory().createLineString(points.map(x->new Coordinate(x.getX(), x.getY())).toArray(Coordinate[]::new));
			road.geometry = new NetworkGeometry(lineString, new ArrayList<Point>());
			
			LaneSection laneSection = new LaneSection();
			Center center = new Center();
			BorderType borderType = new BorderType();
			Geometry geometry = new Geometry();
			
			borderType.color = "white";
			borderType.sOffset = 0;
			borderType.type = "solid";
			
			geometry.sOffset = 0;
			geometry.x = osmRoad.nodes[0].getGeometry().getX();
			geometry.y = osmRoad.nodes[0].getGeometry().getY();
			geometry.z = 0.0;
			geometry.length = SpatialOperations.distance(osmRoad.nodes[0].getGeometry(), osmRoad.nodes[osmRoad.nodes.length-1].getGeometry());
			
			geometry.geometry = lineString; //new GeometryFactory().createLineString(Arrays.stream(osmRoad.nodes).map(x->new Coordinate(x.getGeometry().getX(), x.getGeometry().getY())).toArray(Coordinate[]::new));
			
			//center.uid = UUID.randomUUID().toString();
			center.uid = road.id;
			center.borderType = borderType;
			center.geometry = geometry;
			laneSection.center = center;
			laneSection.singleSide = osmRoad.oneway;
			road.laneSections.add(laneSection);
			
			getNodesOnWay(road, trafficLightNodes).forEach(node->{
				Signal signal = new Signal();
				List<Point> outline = new ArrayList<Point>();
				for (int i=0; i<5; i++) {
					outline.add(node.getGeometry());
				}
				signal.outline = outline;
				signal.id = Long.toString(node.id);
				signal.type = "trafficLight";
				signal.layoutType = "mix3Vertical";
				road.signals.add(signal);
			});
			
			roads.add(road);
		});
		
		return roads;
	}
	
	/**
	 * Calculates a road network from OSM elements.
	 * @param wayElements OSM Way elements.
	 * @param nodeElements OSM Node elements.
	 * @return A network consisting of the OSM elements.
	 */
	private static Network calculateNetwork(Collection<WayElement> wayElements, Collection<NodeElement> nodeElements){
		List<Way> roadWays = new ArrayList<Way>();
		List<WayNode> wayNodes = new ArrayList<WayNode>();
		List<Node> trafficLightNodes = getTrafficLightNodes(nodeElements);
		
		wayElements.forEach(wayElement->{
			if (isWayRoad(wayElement)) {
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
	private static boolean isWayRoad(WayElement way) {
		if (way.getTags().containsKey("highway")) {
			String highwayTagValue = way.getTags().get("highway");
			return Arrays.stream(highwayTags).anyMatch(validHighwayTag-> validHighwayTag.equals(highwayTagValue));
		}
		
		return false;
	}
	
	private static long randomLong() {
	    long leftLimit = 1L;
	    long rightLimit = Integer.MAX_VALUE;
	    return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
	}
	
	private static Way splitWay(Way way, WayNode wayNode, int nodePosition) {
		
		return new Way();
	}
	
	/**
	 * Splits each of the provided ways at each node a different way intersects it.
	 * @param roadNodes
	 * @param roadWays
	 */
	private static void splitRoads(Collection<WayNode> roadNodes, Collection<Way> roadWays) {
		// Get all WayNodes that are contained in more than one Way.
		for (WayNode wayNode : roadNodes.stream().filter(x->x.ways.size()>1).collect(Collectors.toList())){
			//Loop through the ways the node is contained in.
			for (int i=0; i<wayNode.ways.size(); i++){
				Way way = wayNode.ways.get(i);
				int nodePosition = way.getNodePosition(wayNode.id);
				//Only split the way if the node is neither the start or end geometry.
				if ((nodePosition!=0)&&(nodePosition!=way.nodes.length-1)) {
					
					// Create new Way with random id
					Way newWay = new Way();
					newWay.id = randomLong();
					newWay.oneway = way.oneway;
					
					// Add the old and new Ways as a link to the WayNode at which the split occurred
					wayNode.addLink(LinkDirection.STARTING, newWay);
					wayNode.addLink(LinkDirection.ENDING, way);
					
					// The length of the Array to store the Node IDs of the new Way is equal to difference between the amount of Node IDs in the old Way 
					// and the position the split occurred.
					newWay.nodes = new WayNode[way.nodes.length-nodePosition];
					
					// Array to store the new Node IDs of the old way. The length is equal to the position of the split plus one.
					WayNode[] nodeIDsTemp = new WayNode[nodePosition+1];
					
					// Adding the Node IDs up to and including the split to the new Node ID array of the old way 
					for (int k=0; k<nodeIDsTemp.length; k++) {
						nodeIDsTemp[k] = way.nodes[k];
					}
					
					
					for (int k=0; k<newWay.nodes.length; k++) {						
						int counter = k;
						
						// Adding the new Node IDs to the new way.
						newWay.nodes[k] = way.nodes[nodePosition+k];
						
						// 
						way.nodes[nodePosition+k].ways.add(newWay);
						if (counter!= 0) {
							way.nodes[nodePosition+k].ways.remove(way);
						}
						
					}
					
					way.nodes = nodeIDsTemp;
					
					roadWays.add(newWay);
				}
			};
			
		};
	}
	
}
