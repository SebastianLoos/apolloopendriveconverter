package de.apollomasterbeuth.apolloconverter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class SpatialOperations {
	public static double angleBetween2Lines(Point startLine1, Point endLine1, Point startLine2, Point endLine2)
	{
	    double angle1 = Math.atan2(startLine1.getY() - endLine1.getY(), startLine1.getX() - endLine1.getX());
	    double angle2 = Math.atan2(startLine2.getY() - endLine2.getY(), startLine2.getX() - endLine2.getX());
	    return angle1-angle2;
	}
	
	public static boolean checkParallel(LineString lineString1, LineString lineString2) {
		double angle = Math.abs(angleBetween2Lines(lineString1.getStartPoint(), lineString1.getEndPoint(), lineString2.getStartPoint(), lineString2.getEndPoint()));
		return (angle<0.3||(angle>2.7)&&(angle<3.3)||(angle>6));
	}
	
	public static boolean checkParallel(Point startLine1, Point endLine1, Point startLine2, Point endLine2) {
		double angle = Math.abs(angleBetween2Lines(startLine1, endLine1, startLine2, endLine2));
		return (angle<0.3||(angle>2.7)&&(angle<3.3)||(angle>6));
	}
	
	public static double getDeterminant(Point p, Point startLine, Point endLine) {
		return (endLine.getX()-startLine.getX())*(p.getY()-startLine.getY())-(endLine.getY()-startLine.getY())*(p.getX()-startLine.getX());
	}
	
	public static Point getIntersection(Point startLine1, Point endLine1, Point startLine2, Point endLine2) {
		double a1 = endLine1.getY() - startLine1.getY();
		double b1 = startLine1.getX() - endLine1.getX();
		double c1 = a1*(startLine1.getX()) + b1*(startLine1.getY());
		
		double a2 = endLine2.getY() - startLine2.getY();
		double b2 = startLine1.getX() - endLine2.getX();
		double c2 = a2*(startLine2.getX()) + b2*(startLine2.getY());
		
		double det = a1*b2 - a2*b1;
		
		if (det==0) {
			return new GeometryFactory().createPoint();
		} else {
			double x = (b2*c1 - b1*c2)/det;
			double y = (a1*c2 - a2*c1)/det;
			
			return new GeometryFactory().createPoint(new Coordinate(x, y));
		}
	}
	
	public static double distance(Point p1, Point p2) {
		return distance(p1.getY(), p2.getY(), p1.getX(), p2.getX(), 0, 0);
	}
	
	public static double distance(double lat1, double lat2, double lon1,
	        double lon2, double el1, double el2) {

	    final int R = 6371; // Radius of the earth

	    double latDistance = Math.toRadians(lat2 - lat1);
	    double lonDistance = Math.toRadians(lon2 - lon1);
	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double distance = R * c * 1000; // convert to meters

	    double height = el1 - el2;

	    distance = Math.pow(distance, 2) + Math.pow(height, 2);

	    return Math.sqrt(distance);
	}
}
