package de.apollomasterbht.apolloconverter;

/**
 * Represents the settings for the OSMStructureReader.
 * @author Hayuki
 *
 */
public class OSMReaderSettings {
	
	/**
	 * Indicates whether to read traffic light objects.
	 */
	public boolean readTrafficLights;
	
	/**
	 * Collection of highway tags considered to be road ways.
	 */
	public String[] highwayTags;

}
