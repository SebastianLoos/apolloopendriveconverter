package de.apollomasterbeuth.apolloconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
	public static final String[] highwayTags = {"primary","secondary","tertiary","residential","service","unclassified"};
	
	private static Log log = new Log(OSMStructureReader.class.getName());
	
	public static Environment read(List<org.openstreetmap.josm.data.osm.Node> nodes, List<org.openstreetmap.josm.data.osm.Way> ways) 
	{
		System.out.println("importing " + nodes.size() + " nodes and " + ways.size() + " ways");
		return new Environment();
	}
	
	public static Environment createEnvironment(String filename, OSMReaderSettings settings) {
		Environment environment = new Environment();
		
		OsmElement root;
		
		try {
			root = readOSMFile(filename);
		} catch (Exception e) {
			System.out.println("Error while reading file:");
			e.printStackTrace();
			return environment;
		}
		
		log.log(root.getVersion());
		List<Way> roadWays = getRoadWays(root.getWayElements());
		log.log("Roads: " + roadWays.size());
		List<WayNode> roadNodes = getRoadNodes(roadWays, root);
		log.log("Road nodes: " + roadNodes.size());
		
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
			
		
		return environment;

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
	
	private static List<WayNode> getRoadNodes(List<Way> roadWays, OsmElement root){
		List<WayNode> roadNodes = new ArrayList<WayNode>();
		
		roadWays.forEach(way->{
			for(int i = 0; i< way.nodeIDs.length; i++) {
				long currentId = way.nodeIDs[i];
				WayNode node;
				Optional<WayNode> nodeMatch = roadNodes.stream().filter(x -> x.id == currentId).findFirst();
				if (!nodeMatch.isPresent()) {
					NodeElement nodeElement = root.getNodeElement(currentId);
					if (nodeElement!=null) {
						node = new WayNode(nodeElement.getLongitude(), nodeElement.getLatitude(), nodeElement.getID(), way);
						roadNodes.add(node);
					} else {
						System.out.println("Element not found!");
						continue;
					}
				} else {
					node = nodeMatch.get();
					node.ways.add(way);
				}
				
				if (i==0) {
					if (node.links.containsKey(true)) {
						node.links.get(true).add(way);
					} else {
						List<Way> wayList = new ArrayList<Way>();
						node.links.put(true, wayList);
					}
				}
				if (i==way.nodeIDs.length-1) {
					if (node.links.containsKey(false)) {
						node.links.get(false).add(way);
					} else {
						List<Way> wayList = new ArrayList<Way>();
						node.links.put(false, wayList);
					}
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
	
	private static List<Way> getRoadWays(ImmutableList<WayElement> wayElements){
		List<Way> roadWays = new ArrayList<Way>();
		wayElements.forEach(way->{
			way.getTags().forEach((k,v)->{
				if ((k.equals("highway"))&&(Arrays.stream(highwayTags).anyMatch(x -> v.equals(x)))) {
					Way road = new Way();
					List<Long> waysTemp = new ArrayList<Long>(); 
					way.getNdElements().forEach(node->{
						waysTemp.add(node.getReference());
					});
					road.nodeIDs = waysTemp.stream().mapToLong(l -> l).toArray();
					road.id = way.getID();
					road.type = v;
					road.oneway = way.isOneWay();
					roadWays.add(road);
				}
			});
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
	
	private static long randomLong() {
	    long leftLimit = 1L;
	    long rightLimit = Integer.MAX_VALUE;
	    return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
	}
	
	private static OsmElement readOSMFile(String filename) throws Exception {
		log.log(filename);
		File xmlFile = new File(filename);
		InputStream inputStream = new FileInputStream(xmlFile);
		
		return OsmUnmarshaller.unmarshal(inputStream);
	}
	
	private static void splitRoads(List<WayNode> roadNodes, List<Way> roadWays) {
		for (WayNode wayNode : roadNodes.stream().filter(x->x.ways.size()>1).collect(Collectors.toList())){
			for (int i=0;i<wayNode.ways.size();i++){
				Way way = wayNode.ways.get(i);
				int nodePosition = way.getNodePosition(wayNode.id);
				//System.out.println("nodePosition of node " + wayNode.id + " in way " + way.id + " : " + nodePosition + " of " + (way.nodeIDs.length-1));
				if ((nodePosition!=0)&&(nodePosition!=way.nodeIDs.length-1)) {
					Way newWay = new Way();
					newWay.id = randomLong();
					newWay.oneway = way.oneway;
					
					if (wayNode.links.containsKey(true)) {
						wayNode.links.get(true).add(newWay);
					} else {
						List<Way> wayList = new ArrayList<Way>();
						wayList.add(newWay);
						wayNode.links.put(true, wayList);
					}
					if (wayNode.links.containsKey(false)) {
						wayNode.links.get(false).add(way);
					} else {
						List<Way> wayList = new ArrayList<Way>();
						wayList.add(way);
						wayNode.links.put(false, wayList);
					}
					
					newWay.nodeIDs = new long[way.nodeIDs.length-nodePosition];
					
					long[] nodeIDsTemp = new long[nodePosition+1];
					
					//Adding the nodeIDs to the reduced nodeID array of the "old" way 
					for (int k=0; k<nodeIDsTemp.length; k++) {
						nodeIDsTemp[k] = way.nodeIDs[k];
					}
					
					for (int k=0; k<newWay.nodeIDs.length; k++) {
						long id = way.nodeIDs[nodePosition+k];
						int counter = k;
						
						newWay.nodeIDs[k] = way.nodeIDs[nodePosition+k];
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
