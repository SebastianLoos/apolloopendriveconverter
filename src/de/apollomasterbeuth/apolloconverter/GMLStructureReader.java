package de.apollomasterbeuth.apolloconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXB;

import org.apache.lucene.util.SloppyMath;
import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationException;
import org.cts.op.CoordinateOperationFactory;
import org.cts.registry.EPSGRegistry;
import org.cts.registry.RegistryManager;
import org.locationtech.jts.geom.Coordinate;

import de.apollomasterbeuth.apolloconverter.gml.GMLBoundary;
import de.apollomasterbeuth.apolloconverter.gml.GMLData;
import de.apollomasterbeuth.apolloconverter.gml.GMLLane;
import de.apollomasterbeuth.logger.Log;
import de.berlin.broker.SBordstein;
import de.berlin.broker.SFahrbahnmarkierungLinie;
import net.opengis.wfs._2.FeatureCollection;

public class GMLStructureReader {
	private static Log log = new Log(GMLStructureReader.class.getName());
	
	private static String[] nonLaneMarkings = new String[] {"3", "5", "8"};
	
	public static GMLData read(String filename, int epsg) {
		
		GMLData gmlData = new GMLData();
		log.log("Reading GML data from file " + filename);
		try {
			FeatureCollection fc = readGMLFile(filename, epsg);
			
			gmlData.boundaries.addAll(
					fc.getMember().stream()
					.filter(x->x.getSBordstein()!=null)
					.parallel()
					.map(member->getBoundary(member.getSBordstein(), epsg))
					.flatMap(List::stream)
					.collect(Collectors.toList())
					);
			gmlData.lanes.addAll(fc.getMember().stream()
					.filter(x->x.getSFahrbahnmarkierungLinie()!=null)
					.parallel()
					.map(member->getLanes(member.getSFahrbahnmarkierungLinie(), epsg))
					.flatMap(List::stream)
					.collect(Collectors.toList())
					);
			
			log.log("Read " + gmlData.boundaries.size() + " boundaries and " + gmlData.lanes.size() + " lanes.");
			try {
				connectGeometries(gmlData, 0.00001);
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
	
	private static void connectGeometries(GMLData data, double bufferSize) {
		log.log("Connecting lanes...");
		GMLGeometryOperations.getConnectedGeometries(data.lanes, bufferSize, 0);
	}
	
	
	
	private static List<GMLBoundary> getBoundary(SBordstein boundary, int epsg){
		List<GMLBoundary> boundaries = new ArrayList<GMLBoundary>();
		boundary.getGeom().forEach(geometry->{
			geometry.getMultiCurve().getCurveMember().forEach(curveMember->{
				curveMember.getLineString().forEach(gmlLineString->{
					gmlLineString.getPosList().forEach(pos->{
						String coordinateString = pos.getValue();
						String[] coordinatesStrings = coordinateString.split(" ");
						Double[] coordinateValues = new Double[coordinatesStrings.length];
						List<Coordinate> coordinates = new ArrayList<Coordinate>();
						
						for (int j=0; j<coordinatesStrings.length; j++) {
							coordinateValues[j] = Double.parseDouble(coordinatesStrings[j]);
						}
						
						for (int j=0; j<Math.floor(coordinateValues.length/2d); j++) {
							coordinates.add(new Coordinate(coordinateValues[j*2], coordinateValues[j*2+1]));
						}
						
						Coordinate[] coordinateArray = transformCoordinates(coordinates.stream().toArray(Coordinate[]::new), epsg);
						
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
						Double[] coordinateValues = new Double[coordinatesStrings.length];
						List<Coordinate> coordinates = new ArrayList<Coordinate>();
						
						for (int j=0; j<coordinatesStrings.length; j++) {
							coordinateValues[j] = Double.parseDouble(coordinatesStrings[j]);
						}
						
						for (int j=0; j<Math.floor(coordinateValues.length/2d); j++) {
							coordinates.add(new Coordinate(coordinateValues[j*2], coordinateValues[j*2+1]));
						}
						
						Coordinate[] coordinateArray = transformCoordinates(coordinates.stream().toArray(Coordinate[]::new), epsg);
						
						List<Coordinate[]> splitCoordinateArrays = splitLineString(coordinateArray, 2d);
						
						splitCoordinateArrays.forEach(splitCoordinateArray->{
							lanes.add(new GMLLane(splitCoordinateArray));
						});
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
	
	private static Coordinate[] transformCoordinates(Coordinate[] coordinates, int epsg) {
		List<double[]> dstCoordinates = new ArrayList<double[]>();
		
		try {
			CRSFactory factory = new CRSFactory();
			RegistryManager rm = factory.getRegistryManager();
			rm.addRegistry(new EPSGRegistry());
			GeodeticCRS srcCrs = (GeodeticCRS)factory.getCRS("EPSG:"+epsg);
			GeodeticCRS dstCrs = (GeodeticCRS)factory.getCRS("EPSG:4326");
			Set<CoordinateOperation> coordOps = CoordinateOperationFactory.createCoordinateOperations(srcCrs, dstCrs);
			
			Stream.of(coordinates).forEach(x->{
				Optional<CoordinateOperation> op = coordOps.stream().findFirst();
				if (op.isPresent()) {
					try {
					    double[] coord = op.get().transform(new double[] {x.x, x.y});
						dstCoordinates.add(coord);
					} catch (IllegalCoordinateException | CoordinateOperationException e) {
						e.printStackTrace();
					}
				}
			});
			
			Coordinate[] coordinateArray = dstCoordinates.stream().map(x->new Coordinate(x[1], x[0])).toArray(Coordinate[]::new);
			
			return coordinateArray;
		} catch(Exception e) {
			e.printStackTrace();
			return new Coordinate[0];
		}
		
	}
}
