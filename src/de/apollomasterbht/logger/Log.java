package de.apollomasterbht.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	
	private Logger log;
	
	public Log() {
		this(Logger.class.getName());
	}
	
	public Log(String className) {
		log = Logger.getLogger(className);
		FileHandler fh;
		try {
			fh = new FileHandler(log.getName()+".log", true);
			SimpleFormatter sf = new SimpleFormatter();
			fh.setFormatter(sf);
			log.addHandler(fh);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   // true forces append mode
		
	}
	
	public void log(Object obj) {
		//System.out.println(new java.sql.Date(System.currentTimeMillis()) + ": " + obj.toString());
		log.log(Level.INFO, new java.sql.Date(System.currentTimeMillis()) + ": " + obj.toString());
	}
	
	public void error(Object obj) {
		System.out.println(new java.sql.Date(System.currentTimeMillis()) + " ERROR: " + obj.toString());
	}
	
	public void debug(Object obj, Object obj2) {
		//System.out.println(new java.sql.Date(System.currentTimeMillis()) + " DEBUG: " + obj.toString());
		//System.out.println(new java.sql.Date(System.currentTimeMillis()) + " DEBUG: " + obj2.toString());
	}
	
	public void error(Object obj, Exception e) {
		System.out.println(new java.sql.Date(System.currentTimeMillis()) + " ERROR: " + obj.toString());
		e.printStackTrace();
	}
	
	public void warn(Object obj, Object obj2, Object obj3){
		System.out.println(new java.sql.Date(System.currentTimeMillis()) + " WARN: " + obj.toString());
		System.out.println(new java.sql.Date(System.currentTimeMillis()) + " WARN: " + obj2.toString());
		System.out.println(new java.sql.Date(System.currentTimeMillis()) + " WARN: " + obj3.toString());

	}
	
}
