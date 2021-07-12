package de.apollomasterbeuth.apolloconverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import de.apollomasterbeuth.apolloconverter.gml.GMLGeometry;
import de.apollomasterbeuth.apolloconverter.gml.GMLGeometryConnection;
import de.apollomasterbeuth.logger.Log;

public class GMLGeometryOperations {
	
	private static Log log = new Log(GMLGeometryOperations.class.getName());
	
	public static void getConnectedGeometries(List<? extends GMLGeometry> geometries, double bufferSize, int maxNeighbours) {
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

		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		geometries.forEach(geometry->{
			endpoints.add(new Endpoint(geometry.geometry.getStartPoint(), geometry, true));
			endpoints.add(new Endpoint(geometry.geometry.getEndPoint(), geometry, false));
		});
		
		long start = System.nanoTime();
		geometries.parallelStream().forEach(geometry->{
			endpoints
				.parallelStream()
				.filter(x->!x.geometryObject.equals(geometry))
				.filter(x->geometry.geometry.getStartPoint()
					.buffer(bufferSize)
					.intersects(x.point))
				.forEach(endpoint->{
					geometry.predecessors.add(new GMLGeometryConnection(endpoint.geometryObject, endpoint.isStartPoint));
				});
			endpoints
				.parallelStream()
				.filter(x->!x.geometryObject.equals(geometry))
				.filter(x->geometry.geometry.getEndPoint()
					.buffer(bufferSize)
					.intersects(x.point))
				.forEach(endpoint->{
					geometry.successors.add(new GMLGeometryConnection(endpoint.geometryObject, endpoint.isStartPoint));
				});
		});
		long end = System.nanoTime();

		long duration = (end - start)/1000000L;
		log.log("Geometries connected in: " + duration + " ms");
	}
	
	public static Collection<Point> convertCoordinatesToPointCollection(Coordinate[] coordinates){
		return Arrays.stream(coordinates).map(coordinate->new GeometryFactory().createPoint(coordinate)).collect(Collectors.toCollection(ArrayList::new));
	}

}
