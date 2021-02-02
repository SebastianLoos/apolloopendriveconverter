package de.apollomasterbeuth.apolloconverter;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Geometry;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Geometry.PointSet;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Geometry.PointSet.Point;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Lane;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Lane.Border;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Link;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Link.Predecessor;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Link.Successor;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Junction;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Junction.Connection;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Lanes;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Lanes.LaneSection;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Lanes.LaneSection.Boundaries;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Lanes.LaneSection.Boundaries.Boundary;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Lanes.LaneSection.Center;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Lanes.LaneSection.Left;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Lanes.LaneSection.Right;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Signals;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.OpenDRIVE.Road.Signals.Signal;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Outline.CornerGlobal;
import org.openstreetmap.josm.plugins.apolloopendrive.xml.Outline;

import de.apollomasterbeuth.apolloconverter.osm.Way;
import de.apollomasterbeuth.apolloconverter.structure.Environment;
import de.apollomasterbeuth.logger.Log;

public class Converter {
	
	public static final String[] highwayTags = {"primary","tertiary","residential","service"};
	
	private static Log log = new Log();
	
	private String outputPath;
	
	public Converter(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public void convertOSM(Environment env) {
		try {
			OpenDRIVE openDriveRoot = new OpenDRIVE();
			List<Object> rootElements = openDriveRoot.getLinkOrGeometryOrOutline();
			env.roads.forEach(road->{
				//log.log("Converting " + road.id);
				Road odRoad = new Road();
				Lanes odLanes = new Lanes();
				Link odLink = new Link();
				road.links.forEach(link->{
					if (link.type) {
						Predecessor odPredecessor = new Predecessor();
						odPredecessor.setContactPoint(link.contactPoint ? "start" : "end");
						odPredecessor.setElementId(link.linkedRoad.id);
						odPredecessor.setElementType("road");
						odLink.getPredecessor().add(odPredecessor);
					} else {
						Successor odSuccessor = new Successor();
						odSuccessor.setContactPoint(link.contactPoint ? "start" : "end");
						odSuccessor.setElementId(link.linkedRoad.id);
						odSuccessor.setElementType("road");
						odLink.getSuccessor().add(odSuccessor);
					}
				});
				odRoad.getLink().add(odLink);
				
				road.laneSections.forEach(laneSection->{
					LaneSection odLaneSection = new LaneSection();
					Center odCenter = new Center();
					Lane odCenterLane = new Lane();
					Border odCenterBorder = new Border();
					Geometry odCenterGeometry = new Geometry();
					odCenterGeometry.setSOffset(Double.toString(laneSection.center.geometry.sOffset));
					odCenterGeometry.setY(Double.toString(laneSection.center.geometry.x));
					odCenterGeometry.setX(Double.toString(laneSection.center.geometry.y));
					odCenterGeometry.setZ(Double.toString(laneSection.center.geometry.z));
					odCenterGeometry.setLength(Double.toString(laneSection.center.geometry.length));

					PointSet odCenterPointSet = new PointSet();
					
					laneSection.center.geometry.points().forEach(point->{
						Point odPoint = new Point();
						odPoint.setX(Double.toString(point.getY()));
						odPoint.setY(Double.toString(point.getX()));
						odCenterPointSet.getPoint().add(odPoint);
					});
					
					odCenterGeometry.getPointSet().add(odCenterPointSet);
					odCenterBorder.getGeometry().add(odCenterGeometry);
					
					odCenterLane.getBorder().add(odCenterBorder);
					odCenterLane.setId("0");
					odCenterLane.setUid(UUID.randomUUID().toString());
					
					odCenter.getLane().add(odCenterLane);
					
					
					Left odLeft = new Left();
					
					laneSection.left.forEach(left->{
						Lane odLane = new Lane();
						Border odBorder = new Border();
						Geometry odBorderGeometry = new Geometry();
						odBorderGeometry.setLength(Double.toString(left.borderGeometry.length));
						odBorderGeometry.setSOffset(Double.toString(left.borderGeometry.sOffset));
						odBorderGeometry.setY(Double.toString(left.borderGeometry.x));
						odBorderGeometry.setX(Double.toString(left.borderGeometry.y));
						odBorderGeometry.setZ(Double.toString(left.borderGeometry.z));
						
						PointSet odBorderPointSet = new PointSet();
						
						left.borderGeometry.points().forEach(point->{
							Point odPoint = new Point();
							odPoint.setX(Double.toString(point.getY()));
							odPoint.setY(Double.toString(point.getX()));
							odBorderPointSet.getPoint().add(odPoint);
						});
						
						odBorderGeometry.getPointSet().add(odBorderPointSet);
						odBorder.getGeometry().add(odBorderGeometry);
						
						odLane.getBorder().add(odBorder);
						odLane.setId(Integer.toString(left.lane));
						odLane.setUid(UUID.randomUUID().toString());
						odLane.setType("driving");
						odLane.setTurnType("noTurn");
						
						odLeft.getLane().add(odLane);
					});
					
					Right odRight = new Right();
					
					laneSection.right.forEach(right->{
						Lane odLane = new Lane();
						Border odBorder = new Border();
						Geometry odBorderGeometry = new Geometry();
						odBorderGeometry.setLength(Double.toString(right.borderGeometry.length));
						odBorderGeometry.setSOffset(Double.toString(right.borderGeometry.sOffset));
						odBorderGeometry.setY(Double.toString(right.borderGeometry.x));
						odBorderGeometry.setX(Double.toString(right.borderGeometry.y));
						odBorderGeometry.setZ(Double.toString(right.borderGeometry.z));
						
						PointSet odBorderPointSet = new PointSet();
						
						right.borderGeometry.points().forEach(point->{
							Point odPoint = new Point();
							odPoint.setX(Double.toString(point.getY()));
							odPoint.setY(Double.toString(point.getX()));
							odBorderPointSet.getPoint().add(odPoint);
						});
						
						odBorderGeometry.getPointSet().add(odBorderPointSet);
						odBorder.getGeometry().add(odBorderGeometry);
						
						odLane.getBorder().add(odBorder);
						odLane.setId(Integer.toString(right.lane));
						odLane.setUid(UUID.randomUUID().toString());
						odLane.setType("driving");
						odLane.setTurnType("noTurn");
						
						odRight.getLane().add(odLane);
					});
					
					odLaneSection.getCenter().add(odCenter);
					odLaneSection.getLeft().add(odLeft);
					odLaneSection.getRight().add(odRight);
					odLaneSection.setSingleSide(Boolean.toString(laneSection.singleSide));
					
					Boundaries odBoundaries = new Boundaries();
					
					laneSection.boundaries.forEach(boundary->{
						Boundary odBoundary = new Boundary();
						Geometry odBoundaryGeometry = new Geometry();
						odBoundaryGeometry.setLength(Double.toString(boundary.geometry.length));
						odBoundaryGeometry.setSOffset(Double.toString(boundary.geometry.sOffset));
						odBoundaryGeometry.setY(Double.toString(boundary.geometry.x));
						odBoundaryGeometry.setX(Double.toString(boundary.geometry.y));
						odBoundaryGeometry.setZ(Double.toString(boundary.geometry.z));
						
						PointSet odBoundaryPointSet = new PointSet();
						
						boundary.geometry.points().forEach(point->{
							Point odPoint = new Point();
							odPoint.setX(Double.toString(point.getY()));
							odPoint.setY(Double.toString(point.getX()));
							odBoundaryPointSet.getPoint().add(odPoint);
						});
						
						odBoundaryGeometry.getPointSet().add(odBoundaryPointSet);
						odBoundary.getGeometry().add(odBoundaryGeometry);
						odBoundary.setType(boundary.type);
						odBoundaries.getBoundary().add(odBoundary);

					});
					
					odLaneSection.getBoundaries().add(odBoundaries);
					
					odLanes.getLaneSection().add(odLaneSection);
				});
				
				Signals odSignals = new Signals();
				
				road.signals.forEach(signal->{
					Signal odSignal = new Signal();
					Outline odOutline = new Outline();
					signal.outline.forEach(outline->{
						CornerGlobal odCornerGlobal = new CornerGlobal();
						odCornerGlobal.setX(Double.toString(outline.getY()));
						odCornerGlobal.setY(Double.toString(outline.getX()));
						odOutline.getCornerGlobal().add(odCornerGlobal);
					});
					odSignal.setId(signal.id);
					odSignal.setType(signal.type);
					odSignal.setLayoutType(signal.layoutType);
					odSignal.getOutline().add(odOutline);
					odSignals.getSignal().add(odSignal);
				});
				
				odRoad.getSignals().add(odSignals);
				odRoad.getLanes().add(odLanes);
				odRoad.setId(road.id);
				openDriveRoot.getLinkOrGeometryOrOutline().add(odRoad);
			});
			
			env.junctions.forEach(junction->{
				Junction odJunction = new Junction();
				Outline odOutline = new Outline();
				odJunction.setId(Long.toString(junction.nodeLinks.id));
				junction.outline.forEach(outline->{
					CornerGlobal odCornerGlobal = new CornerGlobal();
					odCornerGlobal.setX(Double.toString(outline.getY()));
					odCornerGlobal.setY(Double.toString(outline.getX()));
					odOutline.getCornerGlobal().add(odCornerGlobal);
				});
				int idCounter = 0;
				for (Map.Entry<Boolean, List<Way>> entry : junction.nodeLinks.links.entrySet()) {
					for (Way way : entry.getValue()) {
						Connection odConnection = new Connection();
						odConnection.setId(Integer.toString(idCounter));
						idCounter++;
						//TODO: Connection and Lane Links
						odJunction.getConnection().add(odConnection);
					}
				}
				odJunction.getOutline().add(odOutline);
				openDriveRoot.getLinkOrGeometryOrOutline().add(odJunction);
			});
			
			log.log(outputPath);
			OutputStream os = new FileOutputStream(outputPath);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(OpenDRIVE.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(openDriveRoot, os);
			
			os.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
