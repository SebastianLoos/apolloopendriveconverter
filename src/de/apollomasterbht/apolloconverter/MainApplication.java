package de.apollomasterbht.apolloconverter;

import de.apollomasterbht.apolloconverter.gml.GMLData;
import de.apollomasterbht.apolloconverter.osm.Network;
import de.apollomasterbht.apolloconverter.structure.Environment;

public class MainApplication {

	/**
	 * The main method for executing the conversion tool.
	 * @param args A minimum of 3 strings, where the first string is the path of the OSM file, the second string the path of the output file, the third and all following strings paths of GML files.
	 */
	public static void main(String[] args) {
		try {
			if (args.length >= 2) {;
				Network network = OSMStructureReader.createNetwork(args[0], new OSMReaderSettings() {{
					highwayTags = new String[] {"primary","secondary","tertiary","residential","service","unclassified"};
				}});
				Environment env = EnvironmentGenerator.createEnvironment(network);
				for (int i=2;i<args.length;i++) {
					GMLData data = GMLStructureReader.read(args[i], 25833);
					
					GMLDataAdder.addData(data, env, new GMLDataAdderConfig() {{
						nearestRoadBufferSize = 0.0003;
						mergeBoundariesBufferSize = 0.0000125;
						separateLaneThreshold = 0.2;
					}});
					
				}
				Converter.convertOSM(env, args[1]);
			}	
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}