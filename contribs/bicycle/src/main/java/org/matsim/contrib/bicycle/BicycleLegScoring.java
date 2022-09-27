package org.matsim.contrib.bicycle; //NTUA TEAM: mistake with the name of the package - I recommend org.matsim.contrib.Lmile

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.ScoringParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class BicycleLegScoring implements SumScoringFunction.LegScoring, SumScoringFunction.ArbitraryEventScoring{
	// NTUA TEAM: becomes EscootLegScoring - new function for other modes are developed by NTUA.
	// NTUA TEAM: create a new class per mode
	private static final Logger log = Logger.getLogger( BicycleLegScoring.class ) ; // 

	private final CharyparNagelLegScoring delegate ; // default scoring function
	private final double marginalUtilityOfPerceivedSafety_m; // beta parameter of perceived safety - different per mode
	private double sumPerceivedSafety; // it gives sum(psafe*distance of link i) from UtilityUtils
	private double sumDistance;
	private double minPerceivedSafety;
	private final String bicycleMode; // NTUA TEAM: change to escootMode when we will update esoot trip execution functions
	private final Network network;
	private double additionalScore = 0.; // this the additional score we get, set to zero per plan (not per leg !!)
	
	BicycleLegScoring( final ScoringParameters params, Network network, Set<String> ptModes, BicycleConfigGroup bicycleConfigGroup ) {
		// NTUA TEAM: BicycleConfigGroup becomes LmileConfigGroup
		delegate = new CharyparNagelLegScoring( params, network, ptModes ) ;
		this.marginalUtilityOfPerceivedSafety_m = bicycleConfigGroup.getMarginalUtilityOfPerceivedSafety_m();
		this.bicycleMode = bicycleConfigGroup.getBicycleMode(); // NTUA TEAM: bicycleMode becomes escootMode
		this.network = network ;}

	private void calcLegScore( final Leg leg ) {
		if ( bicycleMode.equals( leg.getMode() ) ) {
			// if bicycle or escoot or walk or car, different additional score per mode....
			if (!isSameStartAndEnd(leg)) {
				NetworkRoute networkRoute = (NetworkRoute) leg.getRoute(); // get route of the leg
				List<Id<Link>> linkIds = new ArrayList<>(networkRoute.getLinkIds());
				linkIds.add(networkRoute.getEndLinkId());
				sumPerceivedSafety = 0.; // set of sumPerceivedSafety equal to zero before iterating of links 
				sumDistance = 0. ; // set of sumDistance equal to zero before iterating links
				minPerceivedSafety = 7;
				// Iterate over all links of the route (leg)...
				for (Id<Link> linkId : linkIds) {
					Link link = network.getLinks().get(linkId);
					double distance = link.getLength(); // this is the length of link i
					double scoreOnLink = BicycleUtilityUtils.computeLinkBasedScore(network.getLinks().get(linkId),
							marginalUtilityOfPerceivedSafety_m); // NTUA TEAM: definitely BicycleUtilityUtils is not necessary for this case
				    sumDistance += distance; // estimate the total distance of the leg
//				    sumPerceivedSafety += scoreOnLink; 
				    sumPerceivedSafety += scoreOnLink*distance; // estimate the total psafe * distance of the leg
//				    if (scoreOnLink < minPerceivedSafety) {
//				    	minPerceivedSafety = scoreOnLink;}
				}
              additionalScore += marginalUtilityOfPerceivedSafety_m*(sumPerceivedSafety/ sumDistance); // weighted mean with utils unit
//				additionalScore += marginalUtilityOfPerceivedSafety_m * sumPerceivedSafety; // with sum.... with real unit
//				additionalScore += marginalUtilityOfPerceivedSafety_m * minPerceivedSafety; 
				System.out.println(sumPerceivedSafety/ sumDistance);
//              System.out.println(sumPerceivedSafety);
//				System.out.println(minPerceivedSafety);

//				System.out.println(minPerceivedSafety);
				// at the end of the iteration of links, add the additional score to the existing one, so that the additional score
				// of the plan can be calculated.
			}
		}
	}
	private static boolean isSameStartAndEnd(Leg leg) {
		return leg.getRoute().getStartLinkId().toString().equals(leg.getRoute().getEndLinkId().toString());
	}

	@Override public void finish(){
		delegate.finish();
	}

	@Override public double getScore(){
		return delegate.getScore() + this.additionalScore ; // add the additional score to the default score of MATSim
	}

	@Override public void handleLeg( Leg leg ){
		delegate.handleLeg( leg );
		calcLegScore( leg );
	}

	@Override public void handleEvent( Event event ){
		delegate.handleEvent( event );
	}

}
