/* *********************************************************************** *
 * project: org.matsim.*
 * Volume.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.counts;
// import org.matsim.demandmodeling.gbl.Gbl;

/**
 * Data record for one volume observation.
 */
public final class Volume extends Measurement.Record {

	Volume(final int h, final double val) {
		
		/* no error checking needed as we use schema instead of dtd
		
		if ((h == -1)) {
			Gbl.errorMsg("[h="+h+", negative values are not allowed!]");
		}
		if ((val == -1)) {
			Gbl.errorMsg("[val="+val+", negative values are not allowed!]");
		}
		*/
		super(h * 3600, (h+1) * 3600, val);
	}

	public final int getHourOfDayStartingWithOne() {
		return (int) (getFrom() / 3600);
	}

	@Override
	public final String toString() {
		return "[" + this.getHourOfDayStartingWithOne() + "===" + this.getValue() + "]";
	}
}
