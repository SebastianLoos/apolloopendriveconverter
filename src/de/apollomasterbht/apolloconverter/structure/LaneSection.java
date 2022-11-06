package de.apollomasterbht.apolloconverter.structure;

import java.util.ArrayList;
import java.util.List;

public class LaneSection {
	
	public Center center;
	
	public List<Lane> right = new ArrayList<Lane>();
	
	public List<Lane> left = new ArrayList<Lane>(); 
	
	public List<Boundary> boundaries = new ArrayList<Boundary>();
	
	public boolean singleSide;

}
