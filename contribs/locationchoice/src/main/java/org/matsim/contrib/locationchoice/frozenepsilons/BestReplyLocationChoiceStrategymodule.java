/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.locationchoice.frozenepsilons;

import java.util.*;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.locationchoice.frozenepsilons.DestinationChoiceContext.ActivityFacilityWithIndex;
import org.matsim.contrib.locationchoice.router.BackwardFastMultiNodeDijkstra;
import org.matsim.contrib.locationchoice.router.BackwardFastMultiNodeDijkstraFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.router.FastMultiNodeDijkstraFactory;
import org.matsim.core.router.MultiNodeDijkstra;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.timing.TimeInterpretation;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.utils.objectattributes.ObjectAttributes;

import javax.inject.Provider;

/**
 * Idea of this should be as follows: all persons and all facilities have k values.  frozen epsilon will be generated on the fly from those two values.  together with frozen
 * epsilon, the location choice is indeed best reply.
 */
final class BestReplyLocationChoiceStrategymodule extends AbstractMultithreadedModule {

	private static final Logger log = Logger.getLogger( BestReplyLocationChoiceStrategymodule.class );
	private final Provider<TripRouter> tripRouterProvider;

	private ObjectAttributes personsMaxEpsUnscaled;
	private DestinationSampler sampler;
	private TreeMap<String, QuadTree<ActivityFacilityWithIndex>> quadTreesOfType = new TreeMap<>();
//	private TreeMap<String, ActivityFacilityImpl []> facilitiesOfType = new TreeMap<>();
	private final Scenario scenario;
	private DestinationChoiceContext lcContext;
	private HashSet<String> flexibleTypes;
	private final LeastCostPathCalculatorFactory forwardMultiNodeDijsktaFactory;
	private final LeastCostPathCalculatorFactory backwardMultiNodeDijsktaFactory;
	private final Map<Id<ActivityFacility>, Id<Link>> nearestLinks;
	private final TimeInterpretation timeInterpretation;

	public static double useScaleEpsilonFromConfig = -99.0;
	private ScoringFunctionFactory scoringFunctionFactory;
	private Map<String, TravelTime> travelTimes;
	private Map<String, TravelDisutilityFactory> travelDisutilities;

	public BestReplyLocationChoiceStrategymodule( Provider<TripRouter> tripRouterProvider, DestinationChoiceContext lcContext, ObjectAttributes personsMaxDCScoreUnscaled,
								    ScoringFunctionFactory scoringFunctionFactory, Map<String, TravelTime> travelTimes, Map<String, TravelDisutilityFactory> travelDisutilities,
								    TimeInterpretation timeInterpretation) {
		super(lcContext.getScenario().getConfig().global());
		this.tripRouterProvider = tripRouterProvider;
		this.scoringFunctionFactory = scoringFunctionFactory;
		this.travelTimes = travelTimes;
		this.travelDisutilities = travelDisutilities;
		this.timeInterpretation = timeInterpretation;

		FrozenTastesConfigGroup dccg = ConfigUtils.addOrGetModule( lcContext.getScenario().getConfig(), FrozenTastesConfigGroup.class );
		if (!FrozenTastesConfigGroup.Algotype.bestResponse.equals( dccg.getAlgorithm() )) {
			throw new RuntimeException("wrong class for selected location choice algorithm type; aborting ...") ;
		}
		this.lcContext = lcContext;
		this.scenario = lcContext.getScenario();
		this.personsMaxEpsUnscaled = personsMaxDCScoreUnscaled;
		this.forwardMultiNodeDijsktaFactory = new FastMultiNodeDijkstraFactory(true);
		this.backwardMultiNodeDijsktaFactory = new BackwardFastMultiNodeDijkstraFactory(true);

		// create cache which is used in ChoiceSet
		// instead of just the nearest link we probably should check whether the facility is attached to a link? cdobler, oct'14
		this.nearestLinks = new HashMap<>();
		for (ActivityFacility facility : this.scenario.getActivityFacilities().getFacilities().values()) {
			if (facility.getLinkId() != null)
				this.nearestLinks.put(facility.getId(), facility.getLinkId());
			else
				this.nearestLinks.put(facility.getId(), Objects.requireNonNull( NetworkUtils.getNearestLink( scenario.getNetwork(), facility.getCoord() ) ).getId() );
		}

		this.flexibleTypes = this.lcContext.getFlexibleTypes();
		this.initTrees();
		this.sampler = new DestinationSampler(
			  this.lcContext.getPersonsKValuesArray(),
			  this.lcContext.getFacilitiesKValuesArray(),
			  dccg );
	}

	/**
	 * Initialize the quadtrees of all available activity types
	 */
	private void initTrees() {
		log.info("Doing location choice for activities: " + this.flexibleTypes.toString());

		for (String flexibleType : this.flexibleTypes) {
			Tuple<QuadTree<ActivityFacilityWithIndex>, ActivityFacilityImpl[]> tuple = this.lcContext.getQuadTreeAndFacilities(flexibleType);
			this.quadTreesOfType.put(flexibleType, tuple.getFirst());
//			this.facilitiesOfType.put(flexibleType, tuple.getSecond());
		}
	}

	@Override
	protected final void beforeFinishReplanningHook() {
		Gbl.printMemoryUsage();
	}

	@Override
	public final PlanAlgorithm getPlanAlgoInstance() {

		ReplanningContext replanningContext = this.getReplanningContext();

		MultiNodeDijkstra forwardMultiNodeDijkstra = (MultiNodeDijkstra) this.forwardMultiNodeDijsktaFactory.createPathCalculator(
			  this.scenario.getNetwork(),
			  travelDisutilities.get(TransportMode.car).createTravelDisutility(travelTimes.get(TransportMode.car)),
			  travelTimes.get(TransportMode.car));

		BackwardFastMultiNodeDijkstra backwardMultiNodeDijkstra = (BackwardFastMultiNodeDijkstra) this.backwardMultiNodeDijsktaFactory.createPathCalculator(
			  this.scenario.getNetwork(),
			  travelDisutilities.get(TransportMode.car).createTravelDisutility(travelTimes.get(TransportMode.car)),
			  travelTimes.get(TransportMode.car));

		// this one corresponds to the "frozen epsilon" paper(s)
		// the random number generators are re-seeded anyway in the dc module. So we do not need a MatsimRandom instance here

		TripRouter tripRouter = tripRouterProvider.get();
		int iteration = replanningContext.getIteration();

		return new BestReplyLocationChoicePlanAlgorithm(this.quadTreesOfType, this.personsMaxEpsUnscaled,
			  this.lcContext, this.sampler, tripRouter, forwardMultiNodeDijkstra, backwardMultiNodeDijkstra, scoringFunctionFactory, iteration, this.nearestLinks, timeInterpretation);
	}
}
