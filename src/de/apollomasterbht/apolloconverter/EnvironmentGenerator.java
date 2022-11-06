package de.apollomasterbht.apolloconverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import de.apollomasterbht.apolloconverter.osm.Network;
import de.apollomasterbht.apolloconverter.osm.Node;
import de.apollomasterbht.apolloconverter.osm.Way;
import de.apollomasterbht.apolloconverter.osm.WayNode;
import de.apollomasterbht.apolloconverter.osm.WayNode.LinkDirection;
import de.apollomasterbht.apolloconverter.structure.BorderType;
import de.apollomasterbht.apolloconverter.structure.Center;
import de.apollomasterbht.apolloconverter.structure.Environment;
import de.apollomasterbht.apolloconverter.structure.Geometry;
import de.apollomasterbht.apolloconverter.structure.Junction;
import de.apollomasterbht.apolloconverter.structure.LaneSection;
import de.apollomasterbht.apolloconverter.structure.Link;
import de.apollomasterbht.apolloconverter.structure.NetworkGeometry;
import de.apollomasterbht.apolloconverter.structure.Road;
import de.apollomasterbht.apolloconverter.structure.Signal;
import de.apollomasterbht.logger.Log;

/**
 * Represents a generator for road environments from a road network.
 * @author Hayuki
 *
 */
public class EnvironmentGenerator {

	private static Log log = new Log(EnvironmentGenerator.class.getName());

	/**
	 * Creates a new road network environment from a given OSM road network.
	 * @param network The OSM network to be incorporated into the environment.
	 * @return A road network environment including the OSM road network.
	 */
	public static Environment createEnvironment(Network network) {
		Environment environment = new Environment();
		
		try {
			splitRoads(network.roadNodes, network.roadWays);
			log.log("Roads after split: " + network.roadWays.size());
			
			environment.junctions = generateJunctions(network.roadNodes, 0.0001);
			log.log("Junctions: " + environment.junctions.size());
			
			environment.roads.addAll(generateRoads(network.roadWays, network.trafficLightNodes));
			
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
	
	/**
	 * Generates junctions from a list of WayNodes.
	 * @param nodes WayNodes to scan for junctions.
	 * @param junctionAreaSize Size of the generated junction around the nodes.
	 * @return A collection of junctions.
	 */
	private static List<Junction> generateJunctions(Collection<WayNode> nodes, double junctionAreaSize){
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
	
	/**
	 * Gets all Nodes located on a given road.
	 * @param road Road the Nodes should be located on.
	 * @param nodes Collection of all Nodes to be checked for their location.
	 * @return A collection of Nodes located on the given road.
	 */
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
	
	/**
	 * Generates roads from a collection of Ways.
	 * @param osmRoads Collection of Ways to convert into roads.
	 * @param trafficLightNodes Collection of Nodes representing traffic lights to be included in the resulting roads.
	 * @return A collection of roads.
	 */
	private static List<Road> generateRoads(Collection<Way> osmRoads, Collection<Node> trafficLightNodes){
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
	
	private static long randomLong() {
	    long leftLimit = 1L;
	    long rightLimit = Integer.MAX_VALUE;
	    return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
	}
}
