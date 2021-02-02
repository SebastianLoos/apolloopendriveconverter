package de.apollomasterbeuth.apolloconverter;

import de.apollomasterbeuth.apolloconverter.gml.GMLData;
import de.apollomasterbeuth.apolloconverter.structure.Environment;

public class MainApplication {

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