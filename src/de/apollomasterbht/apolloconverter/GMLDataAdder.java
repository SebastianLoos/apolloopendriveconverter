package de.apollomasterbht.apolloconverter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.stream.Collectors;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import de.apollomasterbht.apolloconverter.gml.GMLBoundary;
import de.apollomasterbht.apolloconverter.gml.GMLData;
import de.apollomasterbht.apolloconverter.gml.GMLGeometry;
import de.apollomasterbht.apolloconverter.gml.GMLGeometryConnection;
import de.apollomasterbht.apolloconverter.gml.GMLLane;
import de.apollomasterbht.apolloconverter.structure.BorderType;
import de.apollomasterbht.apolloconverter.structure.Boundary;
import de.apollomasterbht.apolloconverter.structure.Environment;
import de.apollomasterbht.apolloconverter.structure.Geometry;
import de.apollomasterbht.apolloconverter.structure.Lane;
import de.apollomasterbht.apolloconverter.structure.Road;
import de.apollomasterbht.logger.Log;

public class GMLDataAdder {
	private static Log log = new Log(GMLDataAdder.class.getName());
	
	public static void addData(GMLData data, Environment env, GMLDataAdderConfig config) {
		addBoundaries(data.boundaries, env, config.nearestRoadBufferSize, config.drawHelperLines);
		addLanes(data.lanes, env, config.separateLaneThreshold, config.nearestRoadBufferSize);
		mergeBoundaries(env, config.mergeBoundariesBufferSize);
	}
	
	/**
	 * Adds GML data of street boundaries to the environment.
	 * @param gmlBoundaries The GML data containing data of the street boundaries.
	 * @param env The environment to add the data to.
	 */
	private static void addBoundaries(List<GMLBoundary> gmlBoundaries, Environment env, double nearestRoadBufferSize, boolean drawHelperLines) {
		log.log("Adding " + gmlBoundaries.size() + " boundary geometries:");
		long start = System.nanoTime();
		for (GMLBoundary gmlBoundary : gmlBoundaries){
			Optional<Road> roadOptional = getNearestRoad(gmlBoundary.geometry, env, nearestRoadBufferSize, true);
			if (roadOptional.isPresent()) {
				Road road = roadOptional.get();
				Boundary boundary = new Boundary();
				Geometry geometry = new Geometry();
				geometry.geometry = gmlBoundary.geometry;
				geometry.sOffset = 0.0;
				geometry.x = gmlBoundary.geometry.getStartPoint().getX();
				geometry.y = gmlBoundary.geometry.getStartPoint().getY();
				geometry.z = 0.0;
				geometry.length = gmlBoundary.geometry.getLength();
				boundary.geometry = geometry;
				boundary.type = (SpatialOperations.getDeterminant(gmlBoundary.geometry.getCentroid(), road.geometry.geometry.getStartPoint(), road.geometry.geometry.getEndPoint())<0) ? "leftBoundary" : "rightBoundary";
				
				if (drawHelperLines) {
					Boundary boundary2 = new Boundary();
					Geometry geometry2 = new Geometry();
					geometry2.geometry = SpatialOperations.getConnectingLine(road.geometry.geometry, gmlBoundary.geometry.getCentroid());
					geometry2.sOffset = 0.0;
					geometry2.x = gmlBoundary.geometry.getStartPoint().getX();
					geometry2.y = gmlBoundary.geometry.getStartPoint().getY();
					geometry2.z = 0.0;
					geometry2.length = gmlBoundary.geometry.getLength();
					boundary2.geometry = geometry2;
					boundary2.type = (SpatialOperations.getDeterminant(gmlBoundary.geometry.getCentroid(), road.geometry.geometry.getStartPoint(), road.geometry.geometry.getEndPoint())<0) ? "leftBoundary" : "rightBoundary";
					road.laneSections.get(0).boundaries.add(boundary2);
				}
				
				road.laneSections.get(0).boundaries.add(boundary);

			}
		}
		long end = System.nanoTime();
		long duration = (end - start)/1000000L;
		log.log("Boundaries added in " + duration + " ms");
	}
	
	/**
	 * Adds lanes from GML data to the environment.
	 * @param gmlLanes The GML lanes to add.
	 * @param env The environment to add the lanes to.
	 * @param separateLaneThreshold Threshold of percentage of variation when comparing the distance of two lanes to a road after which they will considered to be separate lanes.
	 * @param nearestRoadBufferSize Size of the buffer zone surrounding a geometry for limiting the search radius when finding the nearest road.
	 */
	private static void addLanes(List<GMLLane> gmlLanes, Environment env, double separateLaneThreshold, double nearestRoadBufferSize) {
		log.log("Adding " + gmlLanes.size() + " lane geometries:");
		long start = System.nanoTime();
		Map<Road, List<GMLLane>> lanes = new HashMap<Road, List<GMLLane>>();
		connectLanesToRoad(gmlLanes, env, nearestRoadBufferSize).entrySet().stream().forEach(x->lanes.put(x.getKey(), x.getValue()));
		Map<Road, Map<Integer, List<GMLLane>>> finalLanes = new HashMap<Road, Map<Integer, List<GMLLane>>>();
		lanes.entrySet().stream().forEach(lane->{
			log.log("New road with " + lane.getValue().size() + " gml lanes.");
			
			List<List<GMLLane>> seperatedLanes = separateLanes(lane.getValue(), separateLaneThreshold);
			List<List<GMLLane>> mergedSeperatedLanes = new ArrayList<List<GMLLane>>();
			Map<Double, List<GMLLane>> mergedSeperatedLanesByDistA = new HashMap<Double, List<GMLLane>>();
			Map<Double, List<GMLLane>> mergedSeperatedLanesByDistB = new HashMap<Double, List<GMLLane>>();
			seperatedLanes.forEach(seperatedLane->{
				//List<GMLLane> mergedLanes = mergeAllLanes(seperatedLane, 0.0001);
				List<GMLLane> mergedLanes = seperatedLane;
				mergedSeperatedLanes.add(mergedLanes);
			});
			mergedSeperatedLanes.stream().forEach(mergedSeperatedLane->{
				OptionalDouble avgDist = mergedSeperatedLane.stream().mapToDouble(mergesLane->SpatialOperations.distance(lane.getKey().geometry.geometry.getCentroid(), mergesLane.geometry.getCentroid())).average();
				OptionalDouble avgPos = mergedSeperatedLane.stream().mapToDouble(mergesLane->SpatialOperations.getDeterminant(lane.getKey().geometry.geometry.getCentroid(), mergesLane.geometry.getStartPoint(), mergesLane.geometry.getEndPoint())).average();
				if (avgDist.isPresent() && avgPos.isPresent()) {
					if (avgPos.getAsDouble()>0) {
						mergedSeperatedLanesByDistA.put(avgDist.getAsDouble(), mergedSeperatedLane);
					}
					else {
						mergedSeperatedLanesByDistB.put(avgDist.getAsDouble(), mergedSeperatedLane);
					}
				}
			});
			Map<Integer, List<GMLLane>> sortedLanes = new HashMap<Integer, List<GMLLane>>();
			for (int i=0; i<mergedSeperatedLanesByDistA.entrySet().size(); i++) {
				List<GMLLane> lanesToAdd = mergedSeperatedLanesByDistA.entrySet().stream().collect(Collectors.toList()).get(i).getValue();
				sortedLanes.put(i, lanesToAdd);
				log.log("Adding " + lanesToAdd.size() + " to side A");
			}
			for (int i=0; i<mergedSeperatedLanesByDistB.entrySet().size(); i++) {
				List<GMLLane> lanesToAdd = mergedSeperatedLanesByDistB.entrySet().stream().collect(Collectors.toList()).get(i).getValue();
				sortedLanes.put(-i, lanesToAdd);
				log.log("Adding " + lanesToAdd.size() + " to side B");
			}
			
			sortedLanes.put(1, lane.getValue());
			finalLanes.put(lane.getKey(), sortedLanes);
		});
		
		env.roads.forEach(road->{
			Map<Integer, List<GMLLane>> currentLanes = finalLanes.get(road);
			if (currentLanes != null) {
				currentLanes.entrySet().forEach(entry->{
					BorderType borderType = new BorderType();
					borderType.sOffset = 0;
					borderType.type = "solid";
					borderType.color = "white";
					
					Lane lane = new Lane();
					
					lane.uid = UUID.randomUUID().toString();
					lane.borderType = borderType;
					entry.getValue().forEach(gmlLane->{
						Geometry geometry = new Geometry();
						geometry.sOffset = gmlLane.distanceToRoad;
						geometry.length = SpatialOperations.distance(gmlLane.geometry.getStartPoint(), gmlLane.geometry.getEndPoint());
						geometry.x = gmlLane.geometry.getStartPoint().getX();
						geometry.y = gmlLane.geometry.getStartPoint().getY();
						geometry.z = 0;
						geometry.geometry = gmlLane.geometry;
						
						lane.borderGeometry.add(geometry);
					});
					
					lane.distanceFromRoad = 0;
					lane.lane = entry.getKey();
					
					if (lane.lane > 0) {
						road.laneSections.get(0).right.add(lane);
					} else if (lane.lane < 0) {
						road.laneSections.get(0).left.add(lane);
					}
				});
			}
		});
		long end = System.nanoTime();
		long duration = (end - start)/1000000L;
		log.log("Lanes added in " + duration + " ms");
	}
	
	/**
	 * Merges boundaries of the same type into fewer geometries.
	 * @param env The environment containing the boundaries.
	 * @param bufferSize Size of the buffer zone determining adjoining geometries.
	 */
	private static void mergeBoundaries(Environment env, double bufferSize) {
		
		/***
		 * Represents an endpoint of a boundary geometry.
		 * @author Hayuki
		 *
		 */
		class Endpoint {
			public Point point;
			public Boundary boundary;
			public Boolean isStartPoint;
			
			public Endpoint(Point point, Boundary boundary, Boolean isStartPoint) {
				this.point = point;
				this.boundary = boundary;
				this.isStartPoint = isStartPoint;
			}
		}
		
		/***
		 * Represents the connection of a boundary to another.
		 * @author Hayuki
		 *
		 */
		class Connection {
			public Boundary boundary;
			public Optional<Endpoint> predecessor;
			public Optional<Endpoint> successor;
			public Boolean processed;
			
			public Connection(Boundary boundary, Optional<Endpoint> predecessor, Optional<Endpoint> successor) {
				this.boundary = boundary;
				this.predecessor = predecessor;
				this.successor = successor;
				processed = false;
			}
			
			public Deque<Point> initiateProcessing(List<Connection> connections){
				Deque<Point> points = new ArrayDeque<Point>();
				processed = true;
				for (Coordinate coordinate : boundary.geometry.geometry.getCoordinates()) {
					points.add(new GeometryFactory().createPoint(coordinate));
				}
				if (predecessor.isPresent()) {
					connections.stream().filter(x->x.boundary==predecessor.get().boundary).findFirst().get().processConnection(points, predecessor.get().isStartPoint, true, connections);
				}
				if (successor.isPresent()) {
					connections.stream().filter(x->x.boundary==successor.get().boundary).findFirst().get().processConnection(points, successor.get().isStartPoint, false, connections);
				}
				return points;
			}
			
			public void processConnection(Deque<Point> points, Boolean startFromBeginning, Boolean prependPoints, List<Connection> connections) {
				if (!processed) {
					processed = true;
					if (startFromBeginning) {
						if (prependPoints) {
							for (int i=1; i<boundary.geometry.geometry.getCoordinates().length; i++) {
								points.addFirst(new GeometryFactory().createPoint(boundary.geometry.geometry.getCoordinateN(i)));
							}
							if (successor.isPresent()) {
								connections.stream().filter(x->x.boundary==successor.get().boundary).findFirst().get().processConnection(points, successor.get().isStartPoint, true, connections);
							}
						} else {
							for (int i=1; i<boundary.geometry.geometry.getCoordinates().length; i++) {
								points.add(new GeometryFactory().createPoint(boundary.geometry.geometry.getCoordinateN(i)));
							}
							if (successor.isPresent()) {
								connections.stream().filter(x->x.boundary==successor.get().boundary).findFirst().get().processConnection(points, successor.get().isStartPoint, false, connections);
							}
						}
					} else {
						if (prependPoints) {
							for (int i=boundary.geometry.geometry.getCoordinates().length-2; i>=0; i--) {
								points.addFirst(new GeometryFactory().createPoint(boundary.geometry.geometry.getCoordinateN(i)));
							}
							if (predecessor.isPresent()) {
								connections.stream().filter(x->x.boundary==predecessor.get().boundary).findFirst().get().processConnection(points, predecessor.get().isStartPoint, true, connections);
							}
						} else {
							for (int i=boundary.geometry.geometry.getCoordinates().length-2; i>=0; i--) {
								points.add(new GeometryFactory().createPoint(boundary.geometry.geometry.getCoordinateN(i)));
							}	
							if (predecessor.isPresent()) {
								connections.stream().filter(x->x.boundary==predecessor.get().boundary).findFirst().get().processConnection(points, predecessor.get().isStartPoint, false, connections);
							}
						}
					}
				}
			}
		}
		
		log.log("Merging boundaries");
		env.roads.forEach(road->{
			
			// Iterating through all lane sections
			road.laneSections.forEach(laneSection->{
				Map<String, List<Boundary>> boundariesByType = laneSection.boundaries.stream().collect(Collectors.groupingBy(x->x.type));
				laneSection.boundaries.clear();
				
				// Merging for each type individually
				boundariesByType.forEach((type, list)->{
					// List of start and end points of all boundaries for the comparison
					List<Endpoint> endpoints = new ArrayList<Endpoint>();
					list.forEach(boundary->{
						endpoints.add(new Endpoint(boundary.geometry.geometry.getStartPoint(), boundary, true));
						endpoints.add(new Endpoint(boundary.geometry.geometry.getEndPoint(), boundary, false));
					});
					
					List<Connection> boundaryConnections = new ArrayList<Connection>();
					
					/* Iterating through all boundaries and finding the nearest start and end points from the list (if present) 
					 * to the start and end point of each boundary and creating a connection object.
					*/
					for (Boundary boundary : list){
						Optional<Endpoint> neighbourStart = endpoints.stream().filter(x->x.boundary!=boundary)
								.filter(x->x.point.isWithinDistance(boundary.geometry.geometry.getStartPoint(), bufferSize)).min((e1, e2)->
							Double.compare(boundary.geometry.geometry.getStartPoint().distance(e1.point), 
								boundary.geometry.geometry.getStartPoint().buffer(bufferSize).distance(e2.point)));
						Optional<Endpoint> neighbourEnd = endpoints.stream().filter(x->x.boundary!=boundary)
								.filter(x->x.point.isWithinDistance(boundary.geometry.geometry.getEndPoint(), bufferSize)).min((e1, e2)->
							Double.compare(boundary.geometry.geometry.getEndPoint().buffer(bufferSize).distance(e1.point), 
								boundary.geometry.geometry.getEndPoint().buffer(bufferSize).distance(e2.point)));
						boundaryConnections.add(new Connection(boundary, neighbourStart, neighbourEnd));
					}
					
					// Iterating through all connection objects until all have been processed.
					while (boundaryConnections.stream().anyMatch(x->!x.processed)) {
						Connection connection = boundaryConnections.stream().filter(x->!x.processed).findFirst().get();
						Deque<Point> points = connection.initiateProcessing(boundaryConnections);
						Boundary newBoundary = new Boundary();
						newBoundary.type = type;
						Geometry geometry = new Geometry();
						geometry.geometry = new GeometryFactory().createLineString(points.stream().map(x->x.getCoordinate()).toArray(Coordinate[]::new));
						geometry.sOffset = 0.0;
						geometry.x = geometry.geometry.getStartPoint().getX();
						geometry.y = geometry.geometry.getStartPoint().getY();
						geometry.z = 0.0;
						geometry.length = geometry.geometry.getLength();
						newBoundary.geometry = geometry;
						laneSection.boundaries.add(newBoundary);
					}
					
					//log.log("Merged into " + newBoundaryCount + " boundaries");
				});
				
			});
		});
		log.log("Boundaries merged");
	}
	
	
	
	private static Map<Road,List<GMLLane>> connectLanesToRoad(List<GMLLane> gmlLanes, Environment env, double nearestRoadBufferSize) {
		log.log("Getting nearest road for " + gmlLanes.size() + " lanes");
		
		Map<Road,List<GMLLane>> lanes = new HashMap<Road,List<GMLLane>>();
		
		long start = System.nanoTime();
		gmlLanes.forEach(gmlLane->{
			Optional<Road> roadOptional = getNearestRoad(gmlLane.geometry, env, nearestRoadBufferSize, true);
			if (roadOptional.isPresent()) {
				Road road = roadOptional.get();
				gmlLane.connectedRoad = road;
				gmlLane.distanceToRoad = SpatialOperations.distance(road.geometry.geometry, gmlLane.geometry.getCentroid());
				if (lanes.containsKey(road)) {
					lanes.get(road).add(gmlLane);
					//lanes.get(road).add(new GMLLane(SpatialOperations.getConnectingLine(road.geometry.geometry, gmlLane.geometry.getCentroid()).getCoordinates()));
				} else {
					List<GMLLane> laneList = new ArrayList<GMLLane>();
					laneList.add(gmlLane);
					//laneList.add(new GMLLane(SpatialOperations.getConnectingLine(road.geometry.geometry, gmlLane.geometry.getCentroid()).getCoordinates()));
					lanes.put(road, laneList);
				}
			}
		});
		long end = System.nanoTime();
		long duration = (end - start)/1000000L;
		log.log("Lanes added in " + duration + " ms");
		log.log("Calculated " + lanes.size() + " road/lane pairs");
		return lanes;
	}
	
	/**
	 * Separates the provided lane geometries into lanes depending on their distance from the road center.
	 * @param lanes Lanes to be separated.
	 * @param thresholdPercentage Percentage of distance difference for lane geometries considered to be a lane.
	 * @return A collection of lane geometries grouped into lanes.
	 */
	private static List<List<GMLLane>> separateLanes(List<GMLLane> lanes, double thresholdPercentage) {
		lanes.sort((a,b) -> Double.compare(a.distanceToRoad, b.distanceToRoad));
		
		List<Double> difference = new ArrayList<Double>();
		for (int i=0; i<=lanes.size()-2; i++) {
			difference.add((lanes.get(i).distanceToRoad-lanes.get(i+1).distanceToRoad)/lanes.get(i).distanceToRoad);
		}
		log.log("Lane has " + difference.size() + " difference values.");
		List<List<GMLLane>> seperatedLanes = new ArrayList<List<GMLLane>>();
		List<GMLLane> currentLaneList = new ArrayList<GMLLane>();
		currentLaneList.add(lanes.get(0));
		for (int i=0; i<difference.size(); i++) {
			double diff = Math.abs(difference.get(i));
			if (diff >= thresholdPercentage) {
				System.out.println("Diff of " + diff + ". Added " + currentLaneList.size() + " to new lane.");
				seperatedLanes.add(currentLaneList);
				currentLaneList = new ArrayList<GMLLane>();
			}
			currentLaneList.add(lanes.get(i+1));
		}
		log.log("Added " + currentLaneList.size() + " to new lane.");
		seperatedLanes.add(currentLaneList);
		
		log.log("Lane split into " + seperatedLanes.size() + " lanes with " + seperatedLanes.stream().mapToInt(x->x.size()).sum() + " parts.");
		return seperatedLanes;
	}
	
	// Currently unused.
	private static List<GMLLane> mergeAllLanes(List<GMLLane> lanes, double threshold) {
		List<GMLLane> mergedLanes = new ArrayList<GMLLane>();
		Map<GMLGeometry, Boolean> mapStatus = new HashMap<GMLGeometry, Boolean>();
		lanes.forEach(lane->{
			mapStatus.put(lane, false);
		});
		lanes.forEach(lane->{
			if (!mapStatus.get(lane)){
				Deque<Point> points = new ArrayDeque<Point>();
				for (Coordinate coordinate : lane.geometry.getCoordinates()) {
					points.addLast(new GeometryFactory().createPoint(coordinate));
				}
				mapStatus.replace(lane, true);
				Optional<GMLGeometryConnection> nextPredecessor = getNextConnection(lane.predecessors, mapStatus);
				mergeLanes(points, nextPredecessor, true, threshold, mapStatus);
				Optional<GMLGeometryConnection> nextSuccessor = getNextConnection(lane.successors, mapStatus);
				mergeLanes(points, nextSuccessor, false, threshold, mapStatus);
				
				GMLLane newLane = new GMLLane(points.stream().map(point->new Coordinate(point.getX(), point.getY())).toArray(Coordinate[]::new));
				mergedLanes.add(newLane);
			}
		});
		log.log("Merged " + lanes.size() + " lane sections into " + mergedLanes.size() + " lane sections.");
		return mergedLanes;
	}
	
	// Currently unused.
	private static Deque<Point> mergeLanes(Deque<Point> points, Optional<GMLGeometryConnection> potentialConnection, boolean prepend, double threshold, Map<GMLGeometry, Boolean> mapStatus){
		if (potentialConnection.isPresent()) {
			GMLGeometryConnection connection = potentialConnection.get();
			mapStatus.replace(connection.geometry, true);
			if (prepend) {
				for (int i=connection.getDirectionalCoordinates().length; i>0; i--) {
					points.addFirst(new GeometryFactory().createPoint(connection.getDirectionalCoordinates()[i-1]));
				}
				if (connection.connectToBeginning) {
					return mergeLanes(points, getNextConnection(connection.geometry.successors, mapStatus), prepend, threshold, mapStatus);
				}
				else {
					return mergeLanes(points, getNextConnection(connection.geometry.predecessors, mapStatus), prepend, threshold, mapStatus);
				}
			} else {
				for (Coordinate coordinate : connection.getDirectionalCoordinates()) {
					points.addLast(new GeometryFactory().createPoint(coordinate));
				}
				if (connection.connectToBeginning) {
					return mergeLanes(points, getNextConnection(connection.geometry.successors, mapStatus), prepend, threshold, mapStatus);
				}
				else {
					return mergeLanes(points, getNextConnection(connection.geometry.predecessors, mapStatus), prepend, threshold, mapStatus);
				}
			}
		}
		else {
			return points;
		}
		
	}
	
	private static Optional<GMLGeometryConnection> getNextConnection(List<GMLGeometryConnection> connections, Map<? extends GMLGeometry, Boolean> mapStatus) {
		return connections.stream()
			.filter(connection->mapStatus.containsKey(connection.geometry))
			.filter(connection->!mapStatus.get(connection.geometry))
			.findFirst();
	}
	
	/**
	 * Gets the nearest road of a given linestring in the environment
	 * @param lineString The geometry to find the nearest road to.
	 * @param env The environment containing the roads.
	 * @param bufferSize Size of the buffer zone for determining nearby roads.
	 * @param ignoreParallelIfOtherwiseEmpty Indicates whether to ignore the requirement for a road to be parallel if no matching road is found.
	 * @return
	 */
	private static Optional<Road> getNearestRoad(LineString lineString, Environment env, double bufferSize, boolean ignoreParallelIfOtherwiseEmpty) {
		List<Road> nearbyRoads = env.roads.parallelStream().filter(road->road.geometry.geometry.buffer(bufferSize).intersects(lineString)).collect(Collectors.toList());
		//List<Road> nearbyRoads = env.roads;
		List<Road> parallelRoads = nearbyRoads.parallelStream().filter(road->SpatialOperations.checkParallel(lineString, road.geometry.geometry)).collect(Collectors.toList());
		Optional<Road> roadsOptional = parallelRoads.parallelStream().min((road1, road2)->Double.compare(SpatialOperations.distance(road1.geometry.geometry, lineString.getCentroid()),SpatialOperations.distance(road2.geometry.geometry, lineString.getCentroid())));
		if (!roadsOptional.isPresent() && ignoreParallelIfOtherwiseEmpty) {
			return nearbyRoads.parallelStream().min((road1, road2)->Double.compare(SpatialOperations.distance(road1.geometry.geometry, lineString.getCentroid()),SpatialOperations.distance(road2.geometry.geometry, lineString.getCentroid())));
		} else {
			return roadsOptional;
		}
	}
	
	
		
}
