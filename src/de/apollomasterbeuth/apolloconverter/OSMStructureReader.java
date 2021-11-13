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

import com.google.common.collect.ImmutableList;

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
			
			List<Way> roadWays = getRoadWays(root.getWayElements());
			log.log("Roads in OSM file: " + roadWays.size());
			List<WayNode> roadNodes = getRoadNodes(root.getNodeElements(), roadWays);
			log.log("Nodes contained in the roads: " + roadNodes.size());
			
			splitRoads(roadNodes,roadWays);
			log.log("Roads after split: " + roadWays.size());

			List<Node> trafficLightNodes = getTrafficLightNodes(root.getNodeElements());
			
			List<de.apollomasterbeuth.apolloconverter.osm.Road> roads = getOSMRoads(roadNodes, roadWays);
			
			
			environment.junctions = getJunctions(roadNodes, 0.0001);
			log.log("Junctions: " + environment.junctions.size());
			
			environment.roads.addAll(getRoads(roads, trafficLightNodes));
			
			environment.roads.forEach(road->{
				addLinkToRoad(road, roads, environment.roads);
			});	
		}
		catch(Exception e) {
			log.log("Error while creating environment:");
			log.log(e);
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
	
	private static void addLinkToRoad(Road road, List<de.apollomasterbeuth.apolloconverter.osm.Road> osmRoads, List<Road> roads) {
		osmRoads.stream().filter(x->Long.toString(x.way.id).equals(road.id)).findFirst().ifPresent(way->{
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
	
	private static List<Junction> getJunctions(List<WayNode> nodes, double junctionAreaSize){
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
	
	private static List<Node> getNodesOnWay(Road road, List<Node> nodes){
		List<Node> nodesOnWay = new ArrayList<Node>();
		
		org.locationtech.jts.geom.Geometry bufferZone = road.geometry.geometry.buffer(0.00002);
		
		nodes.forEach(node->{
			if (bufferZone.contains(node.geometry)) {
				nodesOnWay.add(node);
			}
		});
		
		return nodesOnWay;
	}
	
	private static List<de.apollomasterbeuth.apolloconverter.osm.Road> getOSMRoads(List<WayNode> roadNodes, List<Way> roadWays){
		List<de.apollomasterbeuth.apolloconverter.osm.Road> roads = new ArrayList<de.apollomasterbeuth.apolloconverter.osm.Road>();
		roadWays.forEach(roadWay->{
			List<WayNode> nodes = new ArrayList<WayNode>();
			for (long nodeID : roadWay.nodeIDs) {
				Optional<WayNode> wayNodeOptional = roadNodes.stream().filter(x->x.id==nodeID).findFirst();
				if (wayNodeOptional.isPresent()) {
					nodes.add(wayNodeOptional.get());
				} else {
					System.out.println(nodeID + " not found");
				}
			}
			//System.out.println(roadWay.id + ": Found " +nodes.size() + " nodes of " + roadWay.nodeIDs.length);
			de.apollomasterbeuth.apolloconverter.osm.Road road = new de.apollomasterbeuth.apolloconverter.osm.Road(roadWay, nodes.toArray(new WayNode[0]));
			roads.add(road);
		});
		return roads;
	}
	
	/**
	 * Gets all OSM nodes from a collection of OSM nodes that are contained in a collection of ways.
	 * @param nodes a collection of nodes to get the subset of nodes from.
	 * @param ways a collection of ways the nodes in the resulting collection should be part of.
	 * @return a collection of nodes connected to their 
	 */
	private static List<WayNode> getRoadNodes(Collection<NodeElement> nodes, Collection<Way> ways){
		List<WayNode> roadNodes = new ArrayList<WayNode>();
		
		ways.forEach(way->{
			for(int i = 0; i< way.nodeIDs.length; i++) {
				long currentId = way.nodeIDs[i];
				Optional<WayNode> nodeMatch = roadNodes.stream().filter(x -> x.id == currentId).findFirst();
				WayNode node = nodeMatch.isPresent() ? nodeMatch.get() : getWayNode(currentId, nodes);
				node.ways.add(way);

				if (!nodeMatch.isPresent()) {
					roadNodes.add(node);
				}
				
				if (i==0) {
					node.addLink(LinkDirection.STARTING, way);
				}
				if (i==way.nodeIDs.length-1) {
					node.addLink(LinkDirection.ENDING, way);
				}
			}
		});
		
		return roadNodes;
	}
	
	private static List<Road> getRoads(List<de.apollomasterbeuth.apolloconverter.osm.Road> osmRoads, List<Node> trafficLightNodes){
		List<Road> roads = new ArrayList<Road>();
		
		osmRoads.forEach(osmRoad->{			
			Road road = new Road();
			road.id = Long.toString(osmRoad.way.id);
			
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
			laneSection.singleSide = osmRoad.way.oneway;
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
	 * Gets all OSM ways considered to be a road from a list of OSM ways.
	 * @param wayElements A collection of OSM ways to select the roads from.
	 * @return A list of OSM ways considered to be roads.
	 */
	private static List<Way> getRoadWays(Collection<WayElement> wayElements){
		List<Way> roadWays = new ArrayList<Way>();
		wayElements.forEach(way->{
			if (isWayRoad(way)) {
				Way road = new Way();
				List<Long> waysTemp = new ArrayList<Long>(); 
				way.getNdElements().forEach(node->{
					waysTemp.add(node.getReference());
				});
				road.nodeIDs = waysTemp.stream().mapToLong(l -> l).toArray();
				road.id = way.getID();
				road.type = getWayType(way);
				road.oneway = way.isOneWay();
				roadWays.add(road);
			}
		});
		return roadWays;
	}
	
	private static List<Node> getTrafficLightNodes(ImmutableList<NodeElement> nodeElements){
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
	private static void splitRoads(List<WayNode> roadNodes, List<Way> roadWays) {
		// Get all WayNodes that are contained in more than one Way.
		for (WayNode wayNode : roadNodes.stream().filter(x->x.ways.size()>1).collect(Collectors.toList())){
			//Loop through the ways the node is contained in.
			for (int i=0; i<wayNode.ways.size(); i++){
				Way way = wayNode.ways.get(i);
				int nodePosition = way.getNodePosition(wayNode.id);
				//Only split the way if the node is neither the start or end geometry.
				if ((nodePosition!=0)&&(nodePosition!=way.nodeIDs.length-1)) {
					
					// Create new Way with random id
					Way newWay = new Way();
					newWay.id = randomLong();
					newWay.oneway = way.oneway;
					
					// Add the old and new Ways as a link to the WayNode at which the split occurred
					wayNode.addLink(LinkDirection.STARTING, newWay);
					wayNode.addLink(LinkDirection.ENDING, way);
					
					// The length of the Array to store the Node IDs of the new Way is equal to difference between the amount of Node IDs in the old Way 
					// and the position the split occurred.
					newWay.nodeIDs = new long[way.nodeIDs.length-nodePosition];
					
					// Array to store the new Node IDs of the old way. The length is equal to the position of the split plus one.
					long[] nodeIDsTemp = new long[nodePosition+1];
					
					// Adding the Node IDs up to and including the split to the new Node ID array of the old way 
					for (int k=0; k<nodeIDsTemp.length; k++) {
						nodeIDsTemp[k] = way.nodeIDs[k];
					}
					
					
					for (int k=0; k<newWay.nodeIDs.length; k++) {
						long id = way.nodeIDs[nodePosition+k];
						int counter = k;
						
						// Adding the new Node IDs to the new way.
						newWay.nodeIDs[k] = way.nodeIDs[nodePosition+k];
						
						// 
						roadNodes.stream().filter(x->x.id==id).forEach(node->{
							node.ways.add(newWay);
							if (counter!=0) {
								node.ways.remove(way);
								//System.out.println("Removed way " + way.id + " from node " + node.id);
							}
						});
					}
					
					way.nodeIDs = nodeIDsTemp;
					
					roadWays.add(newWay);
				}
			};
			
		};
	}
	
}
