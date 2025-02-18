/*
 *   *********************************************************************** *
 *   project: org.matsim.*
 *   *********************************************************************** *
 *                                                                           *
 *   copyright       : (C)  by the members listed in the COPYING,        *
 *                     LICENSE and WARRANTY file.                            *
 *   email           : info at matsim dot org                                *
 *                                                                           *
 *   *********************************************************************** *
 *                                                                           *
 *     This program is free software; you can redistribute it and/or modify  *
 *     it under the terms of the GNU General Public License as published by  *
 *     the Free Software Foundation; either version 2 of the License, or     *
 *     (at your option) any later version.                                   *
 *     See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                           *
 *   ***********************************************************************
 *
 */

package org.matsim.contrib.freight.usecases.chessboard;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierUtils;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.contrib.freight.controler.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.controler.FreightActivity;
import org.matsim.contrib.freight.jsprit.VehicleTypeDependentRoadPricingCalculator;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.SumScoringFunction;

import com.google.inject.Inject;

/**
 * Defines example carrier scoring function (factory).
 *
 * <p>Just saw that there are some Deprecations. Needs to be adapted.
 *
 * @author stefan
 *
 */
public final class CarrierScoringFunctionFactoryImpl implements CarrierScoringFunctionFactory{

    /**
     *
     * Example activity scoring that penalizes missed time-windows with 1.0 per second.
     *
     * @author stefan
     *
     */
    static class DriversActivityScoring implements SumScoringFunction.BasicScoring, SumScoringFunction.ActivityScoring {

        private static final  Logger log = Logger.getLogger(DriversActivityScoring.class);

        private double score;
        private double timeParameter = 0.008;
        private double missedTimeWindowPenalty = 0.01;
        private FileWriter fileWriter;

        public DriversActivityScoring() {
            super();
        }

        @Override
        public void finish() {
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public void handleFirstActivity(Activity act) {
            handleActivity(act);
        }

        @Override
        public void handleActivity(Activity act) {
            if(act instanceof FreightActivity) {
				double actStartTime = act.getStartTime().seconds();

//                log.info(act + " start: " + Time.writeTime(actStartTime));
                TimeWindow tw = ((FreightActivity) act).getTimeWindow();
                if(actStartTime > tw.getEnd()){
                    double penalty_score = (-1)*(actStartTime - tw.getEnd())*missedTimeWindowPenalty;
                    if (!(penalty_score <= 0.0)) throw new AssertionError("penalty score must be negative");
//                    log.info("penalty " + penalty_score);
                    score += penalty_score;

                }
				double actTimeCosts = (act.getEndTime().seconds() -actStartTime)*timeParameter;
//                log.info("actCosts " + actTimeCosts);
                if (!(actTimeCosts >= 0.0)) throw new AssertionError("actTimeCosts must be positive");
                score += actTimeCosts*(-1);
            }
        }

        @Override
        public void handleLastActivity(Activity act) {
            handleActivity(act);
        }

    }

    static class VehicleEmploymentScoring implements SumScoringFunction.BasicScoring {

        private Carrier carrier;
        private FileWriter fileWriter;

        public VehicleEmploymentScoring(Carrier carrier) {
            super();
            this.carrier = carrier;
        }

        @Override
        public void finish() {

        }

        @Override
        public double getScore() {
            double score = 0.;
            CarrierPlan selectedPlan = carrier.getSelectedPlan();
            if(selectedPlan == null) return 0.;
            for(ScheduledTour tour : selectedPlan.getScheduledTours()){
                if(!tour.getTour().getTourElements().isEmpty()){
                    score += (-1)*tour.getVehicle().getType().getCostInformation().getFixedCosts();
                }
            }
            return score;
        }

    }

    /**
     * Example leg scoring.
     *
     * @author stefan
     *
     */
    static class DriversLegScoring implements SumScoringFunction.BasicScoring, SumScoringFunction.LegScoring {

        private static final  Logger log = Logger.getLogger(DriversLegScoring.class);

        private double score = 0.0;
        private final Network network;
        private final Carrier carrier;
        private Set<CarrierVehicle> employedVehicles;

        public DriversLegScoring(Carrier carrier, Network network) {
            super();
            this.network = network;
            this.carrier = carrier;
            employedVehicles = new HashSet<CarrierVehicle>();
        }


        @Override
        public void finish() {

        }


        @Override
        public double getScore() {
            return score;
        }

        private double getTimeParameter(CarrierVehicle vehicle) {
            return vehicle.getType().getCostInformation().getCostsPerSecond();
        }


        private double getDistanceParameter(CarrierVehicle vehicle) {
            return vehicle.getType().getCostInformation().getCostsPerMeter();
        }


//        private CarrierVehicle getVehicle(Id vehicleId) {
//            CarrierUtils.getCarrierVehicle(carrier, vehicleId);
//            if(carrier.getCarrierCapabilities().getCarrierVehicles().containsKey(vehicleId)){
//                return carrier.getCarrierCapabilities().getCarrierVehicles().get(vehicleId);
//            }
//            log.error("Vehicle with Id does not exists", new IllegalStateException("vehicle with id " + vehicleId + " is missing"));
//            return null;
//        }

        @Override
        public void handleLeg(Leg leg) {
            if(leg.getRoute() instanceof NetworkRoute){
                NetworkRoute nRoute = (NetworkRoute) leg.getRoute();
                Id vehicleId = nRoute.getVehicleId();
                CarrierVehicle vehicle = CarrierUtils.getCarrierVehicle(carrier, vehicleId);
                Gbl.assertNotNull(vehicle);
                if(!employedVehicles.contains(vehicle)){
                    employedVehicles.add(vehicle);
                }
                double distance = 0.0;
                double toll = 0.;
                if(leg.getRoute() instanceof NetworkRoute){
                    Link startLink = network.getLinks().get(leg.getRoute().getStartLinkId());
                    distance += startLink.getLength();
                    for(Id linkId : ((NetworkRoute) leg.getRoute()).getLinkIds()){
                        distance += network.getLinks().get(linkId).getLength();

                    }
                    distance += network.getLinks().get(leg.getRoute().getEndLinkId()).getLength();

                }

                double distanceCosts = distance*getDistanceParameter(vehicle);
                if (!(distanceCosts >= 0.0)) throw new AssertionError("distanceCosts must be positive");
                score += (-1) * distanceCosts;
        	double timeCosts = leg.getTravelTime().seconds() *getTimeParameter(vehicle);
                if (!(timeCosts >= 0.0)) throw new AssertionError("distanceCosts must be positive");
                score += (-1) * timeCosts;

            }
        }

    }


    static class TollScoring implements SumScoringFunction.BasicScoring, SumScoringFunction.ArbitraryEventScoring {

        private static final  Logger log = Logger.getLogger(TollScoring.class);

        private double score = 0.;
        private Carrier carrier;
        private Network network;
        private VehicleTypeDependentRoadPricingCalculator roadPricing;

        public TollScoring(Carrier carrier, Network network, VehicleTypeDependentRoadPricingCalculator roadPricing) {
            this.carrier = carrier;
            this.roadPricing = roadPricing;
            this.network = network;
        }

        @Override
        public void handleEvent(Event event) {
            if(event instanceof LinkEnterEvent){
                CarrierVehicle carrierVehicle = CarrierUtils.getCarrierVehicle(carrier, ((LinkEnterEvent) event).getVehicleId());
                if(carrierVehicle == null) throw new IllegalStateException("carrier vehicle missing");
                double toll = roadPricing.getTollAmount(carrierVehicle.getType().getId(),network.getLinks().get(((LinkEnterEvent) event).getLinkId() ),event.getTime() );
                if(toll > 0.) System.out.println("bing: vehicle " + carrierVehicle.getId() + " paid toll " + toll + "" );
                score += (-1) * toll;
            }
        }

//        private CarrierVehicle getVehicle(Id<Vehicle> vehicleId) {
//            if(carrier.getCarrierCapabilities().getCarrierVehicles().containsKey(vehicleId)){
//                return carrier.getCarrierCapabilities().getCarrierVehicles().get(vehicleId);
//            }
//            log.error("Vehicle with Id does not exists", new IllegalStateException("vehicle with id " + vehicleId + " is missing"));
//            return null;
//        }

        @Override
        public void finish() {

        }

        @Override
        public double getScore() {
            return score;
        }
    }

    private Network network;

    @Inject
    public CarrierScoringFunctionFactoryImpl(Network network) {
        super();
        this.network = network;
    }


    @Override
    public ScoringFunction createScoringFunction(Carrier carrier) {
        SumScoringFunction sf = new SumScoringFunction();
        DriversLegScoring driverLegScoring = new DriversLegScoring(carrier, network);
        VehicleEmploymentScoring vehicleEmployment = new VehicleEmploymentScoring(carrier);
//		DriversActivityScoring actScoring = new DriversActivityScoring();
        sf.addScoringFunction(driverLegScoring);
        sf.addScoringFunction(vehicleEmployment);
//		sf.addScoringFunction(actScoring);
        return sf;
    }



}
