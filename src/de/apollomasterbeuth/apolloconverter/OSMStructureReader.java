package de.apollomasterbeuth.apolloconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import de.apollomasterbeuth.apolloconverter.osm.Node;
import de.apollomasterbeuth.apolloconverter.osm.NodeLinks;
import de.apollomasterbeuth.apolloconverter.osm.Way;
import de.apollomasterbeuth.apolloconverter.osm.WayNode;
import de.apollomasterbeuth.apolloconverter.osm.WayNodeConnection;
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
		List<Way> roadWays = new ArrayList<Way>();
		List<WayNode> roadNodes = new ArrayList<WayNode>();
		List<Node> trafficLightNodes = new ArrayList<Node>();
		
		root.getWayElements().forEach(way->{
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
		log.log("Roads: " + roadWays.size());
		
		root.getNodeElements().forEach(node->{
			node.getTags().forEach((k,v)->{
				if ((k.equals("highway"))&&(v.equals("traffic_signals"))) {
					Node trafficLight = new Node(node.getLongitude(), node.getLatitude(), node.getID());
					trafficLightNodes.add(trafficLight);
				}
			});
		});
		
		List<NodeLinks> nodeLinks = new ArrayList<NodeLinks>();
		
		roadWays.forEach(way->{
			
			way.nodes = new WayNode[way.nodeIDs.length];
			for(int i = 0; i< way.nodeIDs.length; i++) {
				long currentId = way.nodeIDs[i];
				WayNode node;
				Optional<WayNode> nodeMatch = roadNodes.stream().filter(x -> x.id == currentId).findFirst();
				if (!nodeMatch.isPresent()) {
					NodeElement nodeElement = root.getNodeElement(currentId);
					if (nodeElement!=null) {
						node = new WayNode(nodeElement.getLongitude(), nodeElement.getLatitude(), nodeElement.getID(), way, i);
						roadNodes.add(node);
					} else {
						continue;
					}
					
				} else {
					node = nodeMatch.get();
					node.ways.add(new WayNodeConnection(way, i));
				}
				
				way.nodes[i] = node;
				if (i==0) {
					Optional<NodeLinks> nodeLinksMatch = nodeLinks.stream().filter(x -> x.id == currentId).findFirst();
					if (!nodeLinksMatch.isPresent()) {
						NodeLinks nodeLinksTemp = new NodeLinks(node.id, node);
						List<Way> wayList = new ArrayList<Way>();
						wayList.add(way);
						nodeLinksTemp.links.put(true, wayList);
						nodeLinks.add(nodeLinksTemp);
						way.start = nodeLinksTemp;
					} else {
						NodeLinks nodeLinksTemp = nodeLinksMatch.get();
						
						if (nodeLinksTemp.links.containsKey(true)) {
							nodeLinksTemp.links.get(true).add(way);
						} else {
							List<Way> wayList = new ArrayList<Way>();
							wayList.add(way);
							nodeLinksTemp.links.put(true, wayList);
						}
						way.start = nodeLinksTemp;
					}
				}
				if (i==way.nodeIDs.length-1) {
					Optional<NodeLinks> nodeLinksMatch = nodeLinks.stream().filter(x -> x.id == currentId).findFirst();
					if (!nodeLinksMatch.isPresent()) {
						NodeLinks nodeLinksTemp = new NodeLinks(node.id, node);
						nodeLinksTemp.links = new HashMap<Boolean, List<Way>>();
						List<Way> wayList = new ArrayList<Way>();
						wayList.add(way);
						nodeLinksTemp.links.put(false, wayList);
						nodeLinks.add(nodeLinksTemp);
						way.end = nodeLinksTemp;
					} else {
						NodeLinks nodeLinksTemp = nodeLinksMatch.get();
						
						if (nodeLinksTemp.links.containsKey(false)) {
							nodeLinksTemp.links.get(false).add(way);
						} else {
							List<Way> wayList = new ArrayList<Way>();
							wayList.add(way);
							nodeLinksTemp.links.put(false, wayList);
						}
						way.end = nodeLinksTemp;
					}
				}
			}
		});
		
		log.log("Road nodes: " + roadNodes.size());
		
		for (int i=0; i<roadNodes.size(); i++) {
			WayNode wayNode = roadNodes.get(i);
			for (int j=0; j<wayNode.ways.size(); j++) {
				if (wayNode.ways.size() > 1) {
					WayNodeConnection wayNodeConnection = wayNode.ways.get(j);
					if ((wayNodeConnection.position!=0)&&(wayNodeConnection.position!=wayNodeConnection.way.nodes.length-1)) {
						Way way = new Way();
						way.id = randomLong();
						way.oneway = wayNodeConnection.way.oneway;
						
						Optional<NodeLinks> nodeLinksMatch = nodeLinks.stream().filter(x -> x.id == wayNode.id).findFirst();
						NodeLinks nodeLinksTemp;
						if (nodeLinksMatch.isPresent()) {
							nodeLinksTemp = nodeLinksMatch.get();
							if (nodeLinksTemp.links.containsKey(true)) {
								nodeLinksTemp.links.get(true).add(way);
							} else {
								List<Way> wayList = new ArrayList<Way>();
								wayList.add(way);
								nodeLinksTemp.links.put(true, wayList);
							}
							if (nodeLinksTemp.links.containsKey(false)) {
								nodeLinksTemp.links.get(false).add(wayNodeConnection.way);
							} else {
								List<Way> wayList = new ArrayList<Way>();
								wayList.add(wayNodeConnection.way);
								nodeLinksTemp.links.put(false, wayList);
							}
						} else {
							nodeLinksTemp = new NodeLinks(wayNode.id, wayNode);
							List<Way> wayList = new ArrayList<Way>();
							wayList.add(way);
							nodeLinksTemp.links.put(true, wayList);
							nodeLinks.add(nodeLinksTemp);
						}
						way.start = nodeLinksTemp;
						way.end = wayNodeConnection.way.end;
						List<Way> falseList = way.end.links.get(false);
						if(falseList.contains(wayNodeConnection.way)) {
							falseList.remove(wayNodeConnection.way);
						}
						falseList.add(way);
						
						wayNodeConnection.way.end = nodeLinksTemp;
						
						way.nodes = new WayNode[wayNodeConnection.way.nodes.length-wayNodeConnection.position];
						way.nodeIDs = new long[wayNodeConnection.way.nodes.length-wayNodeConnection.position];
						
						WayNode[] nodesTemp = new WayNode[wayNodeConnection.position+1];
						long[] nodeIDsTemp = new long[wayNodeConnection.position+1];
						
						for (int k=0; k<nodesTemp.length; k++) {
							nodesTemp[k] = wayNodeConnection.way.nodes[k];
							nodeIDsTemp[k] = wayNodeConnection.way.nodeIDs[k];
						}
						
						for (int k=0; k<way.nodes.length; k++) {
							way.nodes[k] = wayNodeConnection.way.nodes[wayNodeConnection.position+k];
							way.nodes[k].ways.add(new WayNodeConnection(way, k));
							if (k>0) {
								if (way.nodes[k].ways.removeIf(x->x.way==wayNodeConnection.way)) {
									//System.out.println("Old nodeConnection removed!");
								}
							}
						}
						
						wayNodeConnection.way.nodes = nodesTemp;
						wayNodeConnection.way.nodeIDs = nodeIDsTemp;
						
						roadWays.add(way);
					}
				}
				
			}
		}
		
		environment.junctions = getJunctions(nodeLinks, 0.0001);
		log.log("Junctions: " + environment.junctions.size());
		
		roadWays.forEach(way->{			
			Road road = new Road();
			road.id = Long.toString(way.id);
			
			Stream<Point> points = Arrays.stream(way.nodes).map(x->x.geometry);
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
			geometry.x = way.nodes[0].getGeometry().getX();
			geometry.y = way.nodes[0].getGeometry().getY();
			geometry.z = 0.0;
			geometry.length = SpatialOperations.distance(way.nodes[0].getGeometry(), way.nodes[way.nodes.length-1].getGeometry());
			
			geometry.geometry = new GeometryFactory().createLineString(Arrays.stream(way.nodes).map(x->new Coordinate(x.getGeometry().getX(), x.getGeometry().getY())).toArray(Coordinate[]::new));
			
			center.borderType = borderType;
			center.geometry = geometry;
			laneSection.center = center;
			laneSection.singleSide = way.oneway;
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
			
			environment.roads.add(road);
		});
		
		environment.roads.forEach(road->{
			roadWays.stream().filter(x->Long.toString(x.id).equals(road.id)).findFirst().ifPresent(way->{
				way.start.links.forEach((k,v)->{
					v.forEach(linkedWay->{
						if (!Long.toString(linkedWay.id).equals(road.id)) {
							environment.roads.stream().filter(x->x.id.equals(Long.toString(linkedWay.id))).findFirst().ifPresent(predecessor->{
								Link link = new Link();
								link.contactPoint = k;
								link.type = true;
								link.linkedRoad = predecessor;
								road.links.add(link);
							});
						}
					});
				});
				way.end.links.forEach((k,v)->{
					v.forEach(linkedWay->{
						if (!Long.toString(linkedWay.id).equals(road.id)) {
							environment.roads.stream().filter(x->x.id.equals(Long.toString(linkedWay.id))).findFirst().ifPresent(successor->{
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
			//log.log("Links on road: " + road.links.size());
		});	
			
		
		return environment;

	}
	
	private static List<Junction> getJunctions(List<NodeLinks> nodeLinks, double junctionAreaSize){
		List<Junction> junctions = new ArrayList<Junction>();
		nodeLinks.stream().filter(x->x.links.entrySet().stream().map(y->y.getValue().size()).reduce(0, (a, b) -> a+b) > 1).forEach(nodeLink->{
			Junction junction = new Junction();
			junction.nodeLinks = nodeLink;
			Point center = nodeLink.node.geometry;
			
			Arrays.stream(center.buffer(junctionAreaSize, 1).getCoordinates()).map(x->new GeometryFactory().createPoint(x)).collect(Collectors.toList()).forEach(point->{
				junction.outline.add(point);
			});
			junctions.add(junction);
		});
		
		return junctions;
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
	
	private static List<Node> getNodesOnWay(Road road, List<Node> nodes){
		List<Node> nodesOnWay = new ArrayList<Node>();
		
		org.locationtech.jts.geom.Geometry bufferZone = road.geometry.geometry.buffer(0.00002);
		
		nodes.forEach(node->{
			if (bufferZone.contains(node.geometry)) {
				nodesOnWay.add(node);
			}
		});
		
		//log.log("TrafficLights on node: " + nodesOnWay.size());
		return nodesOnWay;
	}

}
