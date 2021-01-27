package de.apollomasterbeuth.apolloconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXB;

import org.apache.lucene.util.SloppyMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;

import de.apollomasterbeuth.apolloconverter.gml.GMLBoundary;
import de.apollomasterbeuth.apolloconverter.gml.GMLData;
import de.apollomasterbeuth.apolloconverter.gml.GMLGeometry;
import de.apollomasterbeuth.apolloconverter.gml.GMLLane;
import de.apollomasterbeuth.logger.Log;
import de.berlin.broker.SBordstein;
import de.berlin.broker.SFahrbahnmarkierungLinie;
import net.opengis.wfs._2.FeatureCollection;
import net.opengis.wfs._2.FeatureCollection.Member;

public class GMLStructureReader {
	private static Log log = new Log(GMLStructureReader.class.getName());
	
	private static String[] nonLaneMarkings = new String[] {"3", "5", "8"};
	
	public static GMLData read(String filename, int epsg) {
		
		GMLData gmlData = new GMLData();
		try {
			FeatureCollection fc = readGMLFile(filename, epsg);
			int count = 0;
			for (Member member : fc.getMember()){
				count++;
				if (member.getSBordstein()!=null) {
					log.log("Loading boundary data " + count + "/" + fc.getMember().size());
					gmlData.boundaries.addAll(getBoundary(member.getSBordstein(), epsg));
				}
				if (member.getSFahrbahnmarkierungLinie()!=null) {
					log.log("Loading lane data " + count + "/" + fc.getMember().size());
					gmlData.lanes.addAll(getLanes(member.getSFahrbahnmarkierungLinie(), epsg));
				}
			}
			try {
				getNeighbours(gmlData, 0.00001);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			log.log("Error while loading gml file:");
			log.log(e);
		}
		return gmlData;
	}

	public static FeatureCollection readGMLFile(String filename, int epsg) throws Exception {
		
		try {
			log.log("Reading file " + filename);
			File file = new File(filename);
			FeatureCollection fc = JAXB.unmarshal(file, FeatureCollection.class);
			log.log(fc.getMember().size() + " entries");
			return fc;
			
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception("Invalid file structure");
		}
	}
	
	private static double calculateDistance(Coordinate start, Coordinate end) {
		double lat1 = start.getX();
		double lon1 = start.getY();
		double lat2 = end.getX();
		double lon2 = end.getY();
		
		return SloppyMath.haversinMeters(lat1, lon1, lat2, lon2);
	}
	
	private static void getNeighbours(GMLData data, double bufferSize) {
		
		class Endpoint{
			public Point point;
			public GMLGeometry geometryObject;
			public Boolean isStartPoint;
			
			public Endpoint(Point point, GMLGeometry geometryObject, Boolean isStartPoint) {
				this.point = point;
				this.geometryObject = geometryObject;
				this.isStartPoint = isStartPoint;
			}
		}
		
		log.log("Get Neighbours of lanes:");

		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		data.lanes.forEach(lane->{
			endpoints.add(new Endpoint(lane.geometry.getStartPoint(), lane, true));
			endpoints.add(new Endpoint(lane.geometry.getEndPoint(), lane, false));
		});
		
		int count = 0;
		for (GMLLane lane : data.lanes){
			count++;
			log.log(count + "/" + data.lanes.size());
			
			endpoints.stream().filter(x->!x.geometryObject.equals(lane)).filter(x->lane.geometry.getStartPoint().buffer(bufferSize).intersects(x.point)).forEach(endpoint->{
				lane.connection.addPredecessor(endpoint.geometryObject, endpoint.isStartPoint);
				log.log("predecessor added");
			});
			endpoints.stream().filter(x->!x.geometryObject.equals(lane)).filter(x->lane.geometry.getEndPoint().buffer(bufferSize).intersects(x.point)).forEach(endpoint->{
				lane.connection.addSuccessor(endpoint.geometryObject, endpoint.isStartPoint);
				log.log("successor added");
			});
		}
	}
	
	private static List<GMLBoundary> getBoundary(SBordstein boundary, int epsg){
		
		List<GMLBoundary> boundaries = new ArrayList<GMLBoundary>();
		boundary.getGeom().forEach(geometry->{
			geometry.getMultiCurve().getCurveMember().forEach(curveMember->{
				curveMember.getLineString().forEach(gmlLineString->{
					gmlLineString.getPosList().forEach(pos->{
						String coordinateString = pos.getValue();
						String[] coordinatesStrings = coordinateString.split(" ");
						Double[] coordinates = new Double[coordinatesStrings.length];
						
						double length = Double.parseDouble(boundary.getLaenge());
						
						for (int j=0; j<coordinatesStrings.length; j++) {
							coordinates[j] = Double.parseDouble(coordinatesStrings[j]);
						}
						List<ProjCoordinate> srcCoordinates = new ArrayList<ProjCoordinate>();
						List<ProjCoordinate> dstCoordinates = new ArrayList<ProjCoordinate>();
						
						for (int j=0; j<Math.floor(coordinates.length/2d); j++) {
							srcCoordinates.add(new ProjCoordinate(coordinates[j*2], coordinates[j*2+1]));
						}
						
						CRSFactory factory = new CRSFactory();
						CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:"+epsg);
						CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:4326");
						BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
						
						srcCoordinates.forEach(x->{
							ProjCoordinate dstCoordinate = new ProjCoordinate();
							transform.transform(x, dstCoordinate);
							dstCoordinates.add(dstCoordinate);
						});
						
						Coordinate[] coordinateArray = dstCoordinates.stream().map(x->new Coordinate(x.y, x.x)).toArray(Coordinate[]::new);
						
						List<Coordinate[]> splitCoordinateArrays = splitLineString(coordinateArray, 2d);
						
						splitCoordinateArrays.forEach(splitCoordinateArray->{
							boundaries.add(new GMLBoundary(splitCoordinateArray));
						});
					});
				});
			});
		});
		
		return boundaries;
	}
	
	private static List<GMLLane> getLanes(SFahrbahnmarkierungLinie lane, int epsg){
		
		List<GMLLane> lanes = new ArrayList<GMLLane>();
		if (Arrays.stream(nonLaneMarkings).anyMatch(x->x.equals(lane.getFbl()))){
			return lanes;
		}
		lane.getGeom().forEach(geometry->{
			geometry.getMultiCurve().getCurveMember().forEach(curveMember->{
				curveMember.getLineString().forEach(gmlLineString->{	
					gmlLineString.getPosList().forEach(pos->{
						String coordinateString = pos.getValue();
						String[] coordinatesStrings = coordinateString.split(" ");
						Double[] coordinates = new Double[coordinatesStrings.length];
						
						double length = Double.parseDouble(lane.getLaenge());
						
						for (int j=0; j<coordinatesStrings.length; j++) {
							coordinates[j] = Double.parseDouble(coordinatesStrings[j]);
						}
						List<ProjCoordinate> srcCoordinates = new ArrayList<ProjCoordinate>();
						List<ProjCoordinate> dstCoordinates = new ArrayList<ProjCoordinate>();
						
						for (int j=0; j<Math.floor(coordinates.length/2d); j++) {
							srcCoordinates.add(new ProjCoordinate(coordinates[j*2], coordinates[j*2+1]));
						}
						
						CRSFactory factory = new CRSFactory();
						CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:"+epsg);
						CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:4326");
						BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
						
						srcCoordinates.forEach(x->{
							ProjCoordinate dstCoordinate = new ProjCoordinate();
							transform.transform(x, dstCoordinate);
							dstCoordinates.add(dstCoordinate);
						});
						
						Coordinate[] coordinateArray = dstCoordinates.stream().map(x->new Coordinate(x.y, x.x)).toArray(Coordinate[]::new);
						lanes.add(new GMLLane(coordinateArray));
					});
				});
			});
		});
		
		return lanes;
	}
	
	private static List<Coordinate[]> splitLineString(Coordinate[] lineStringCoordinateArray, double minLength){
		List<Coordinate[]> coordinateArrayList = new ArrayList<Coordinate[]>();
		if (minLength < 0) {
			coordinateArrayList.add(lineStringCoordinateArray);
		} else {
			List<Coordinate> coordinateList = new ArrayList<Coordinate>();
			for (int i=0; i<lineStringCoordinateArray.length; i++) {
				if (coordinateList.isEmpty()) {
					coordinateList.add(lineStringCoordinateArray[i]);
				} else {
					coordinateList.add(lineStringCoordinateArray[i]);
					if (calculateDistance(coordinateList.get(0),lineStringCoordinateArray[i])>=minLength) {
						coordinateArrayList.add(coordinateList.toArray(new Coordinate[0]));
						coordinateList.clear();
						coordinateList.add(lineStringCoordinateArray[i]);
					} else {
						if (i==lineStringCoordinateArray.length-1) {
							coordinateArrayList.add(coordinateList.toArray(new Coordinate[0]));
						}
					}
				}
			}
		}
		return coordinateArrayList;
	}
}
