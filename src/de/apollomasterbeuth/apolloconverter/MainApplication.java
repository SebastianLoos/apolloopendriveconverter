package de.apollomasterbeuth.apolloconverter;

import de.apollomasterbeuth.apolloconverter.gml.GMLData;
import de.apollomasterbeuth.apolloconverter.structure.Environment;

public class MainApplication {

	/**
	 * The main method for executing the conversion tool.
	 * @param args A minimum of 3 strings, where the first string is the path of the OSM file, the second string the path of the output file, the third and all following strings paths of GML files.
	 */
	public static void main(String[] args) {
		if (args.length >= 2) {
			OSMReaderSettings settings = new OSMReaderSettings() {{
				convertTrafficLights = true;
			}};
			Environment env = OSMStructureReader.createEnvironment(args[0], settings);
			for (int i=2;i<args.length;i++) {
				GMLData data = GMLStructureReader.read(args[i], 25833);
				GMLDataAdder.addData(data, env);
			}
			Converter conv = new Converter(args[1]);
			conv.convertOSM(env);
		}
	}

}