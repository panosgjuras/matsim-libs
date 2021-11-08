/* *********************************************************************** *
 * project: org.matsim.*
 * Count.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;

/**
 * Class holding counts data.
 *
 * Should be deprecated and is now built on top newer measurement API.
 * @param <T>
 */
public final class Count<T> extends Measurement<T> implements Identifiable<T> {

	Count(final Id<T> linkId2, final String stationName) {
		super(linkId2, Observation.COUNT, stationName);
	}

	/**
	 * Creates and adds a {@link Volume} to the {@link Count}ing station.
	 * @param h indicating the hour-of-day. <b><i>Note: the hours for a counting 
	 * 		station must be from 1-24, and <b><i>not</i></b> from 0-23, 
	 * 		otherwise the {@link MatsimCountsReader} will throw an error.
	 * 		</i></b>
	 * @param val the total number of vehicles counted during hour <code>h</code>.
	 * @return the {@link Count}ing station's {@link Volume}.
	 */
	public final Volume createVolume(final int h, final double val) {
		if ( h < 1 ) {
			throw new RuntimeException( "counts start at 1, not at 0.  If you have a use case where you need to go below one, "
					+ "let us know and we think about it, but so far we had numerous debugging sessions because someone inserted counts at 0.") ;
		}

		Record exists = getRecord(h * 3600);
		if (exists != null)
			throw new IllegalArgumentException("There is already an record for this range: " + exists);

		Volume v = new Volume(h, val);
		records.add(v);
		return v;
	}

	@Override
	public Measurement<T> put(double from, double to, double value) {
		Record exists = getRecord(from);
		if (exists != null)
			throw new IllegalArgumentException("There is already an record for this range: " + exists);

		if ((to - from) != 3600)
			throw new IllegalArgumentException("From and to must span exactly one hour when using counts.");

		records.add(new Volume((int) (from / 3600), value));
		return this;
	}

	public final void setCsId(final String cs_id) {
		this.station = cs_id;
	}


	public final String getCsLabel() {
		return getStation();
	}

	public final Volume getMaxVolume() {
		Volume v_max = null;
		double max = -1.0;
		for (Record v : this.records) {
			if (v.getValue() > max) { max = v.getValue(); v_max = (Volume) v; }
		}
		return v_max;
	}

	public final Volume getVolume(final int h) {
		return (Volume) getRecord(h * 3600);
	}

	public final Map<Integer, Volume> getVolumes() {
		return records.stream().collect(Collectors.toMap(r -> (int) (r.getFrom() / 3600), r -> (Volume) r));
	}

	public void setCoord(final Coord coord) {
		this.coord = coord;
	}

	/** @return Returns the exact coordinate, where this counting station is
	 * located, or null if no exact location is available.
	 **/
	public Coord getCoord() {
		return this.coord;
	}

	@Override
	public final String toString() {
		return "[Loc_id=" + this.getId() + "]" +
		"[cs_id=" + this.getStation() + "]" +
		"[nof_volumes=" + this.size() + "]";
	}

}
