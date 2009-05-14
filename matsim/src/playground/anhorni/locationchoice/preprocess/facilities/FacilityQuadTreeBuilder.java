package playground.anhorni.locationchoice.preprocess.facilities;

import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.facilities.ActivityFacilities;
import org.matsim.core.api.facilities.ActivityFacility;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.locationchoice.LocationMutator;

public class FacilityQuadTreeBuilder {
	
	private static final Logger log = Logger.getLogger(LocationMutator.class);
	
	public QuadTree<ActivityFacility> buildFacilityQuadTree(String type, ActivityFacilities facilities) {
		
		TreeMap<Id, ActivityFacility> treeMap = new TreeMap<Id, ActivityFacility>();
		// get all types of activities
		for (ActivityFacility f : facilities.getFacilities().values()) {		
			if (!treeMap.containsKey(f.getId())) {
				treeMap.put(f.getId(), f);
			}	
		}
		return this.builFacQuadTree(type, treeMap);
	}
	
	
	public QuadTree<ActivityFacility> builFacQuadTree(String type, TreeMap<Id,ActivityFacility> facilities_of_type) {
		Gbl.startMeasurement();
		log.info(" building " + type + " facility quad tree");
		double minx = Double.POSITIVE_INFINITY;
		double miny = Double.POSITIVE_INFINITY;
		double maxx = Double.NEGATIVE_INFINITY;
		double maxy = Double.NEGATIVE_INFINITY;

		for (final ActivityFacility f : facilities_of_type.values()) {
			if (f.getCoord().getX() < minx) { minx = f.getCoord().getX(); }
			if (f.getCoord().getY() < miny) { miny = f.getCoord().getY(); }
			if (f.getCoord().getX() > maxx) { maxx = f.getCoord().getX(); }
			if (f.getCoord().getY() > maxy) { maxy = f.getCoord().getY(); }
		}
		minx -= 1.0;
		miny -= 1.0;
		maxx += 1.0;
		maxy += 1.0;
		System.out.println("        xrange(" + minx + "," + maxx + "); yrange(" + miny + "," + maxy + ")");
		QuadTree<ActivityFacility> quadtree = new QuadTree<ActivityFacility>(minx, miny, maxx, maxy);
		for (final ActivityFacility f : facilities_of_type.values()) {
			quadtree.put(f.getCoord().getX(),f.getCoord().getY(),f);
		}
		log.info("Number of facilities: " + quadtree.size());
		Gbl.printRoundTime();
		return quadtree;
	}
}
