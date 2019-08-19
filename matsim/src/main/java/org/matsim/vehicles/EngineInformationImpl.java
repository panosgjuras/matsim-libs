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

package org.matsim.vehicles;

import org.matsim.utils.objectattributes.attributable.Attributes;

/**
 * @author dgrether
 */
public class EngineInformationImpl implements EngineInformation {

	private FuelType fuelType;
	private double gasConsumption;
	private Attributes attributes = new Attributes() ;

	/**
	 *
	 * @deprecated litersPerMeter were not longer set here.
	 * Please use EngineInformationImpl(FuelType fueltype) instead if you want to set the FuelType only (recommended)
	 * Please use VehicleUtils.setEngineInformation(...) instead if you also want to set FuelType and FuelConsumption (no longer recommended,
	 * because we want to have the FuelConsumption coming from the emissions contrib). This FuelConsumption here is only a very rough estimation
	 * without traffic condition.
	 */
	@Deprecated
	public EngineInformationImpl(FuelType fueltype, double literPerMeter) {
		this.setFuelType(fueltype);
		this.setFuelConsumption(literPerMeter);
	}

	public EngineInformationImpl(FuelType fueltype) {
		this.setFuelType(fueltype);
		this.setFuelConsumption(Double.NaN);
	}

    public EngineInformationImpl() {
    }

	@Override
	public FuelType getFuelType() {
		return this.fuelType;
	}

	@Override
	public double getFuelConsumption() {
		return this.gasConsumption;
	}

	@Override
	public void setFuelType(FuelType fueltype) {
		this.fuelType = fueltype;
	}

	@Override
	public void setFuelConsumption(double literPerMeter) {
		this.gasConsumption = literPerMeter ;
	}

	@Override public Attributes getAttributes(){
		return attributes ;
	}
}
