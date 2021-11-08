package org.matsim.counts;

import org.matsim.api.core.v01.BasicLocation;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Measurements of an observation that are grouped together, e.g. belonging to the same category / mode.
 *
 * This class supersedes {@link Counts}. To keep the same API this class is not final at the moment.
 */
public class Measurement<T> implements Identifiable<T>, BasicLocation, Iterable<Measurement.Record> {

	protected final Id<T> id;
	protected final Observation type;

	protected final NavigableSet<Record> records = new TreeSet<>();
	protected String group;
	protected Coord coord;
	protected String station;

	/**
	 * Create a new emtpy measurement.
	 *
	 * @param type observation type
	 */
	Measurement(Id<T> id, Observation type, String station) {
		this.id = id;
		this.type = type;
		this.station = station;
	}

	public Observation getType() {
		return type;
	}

	public String getStation() {
		return station;
	}

	public Measurement<T> setGroup(String group) {
		this.group = group;
		return this;
	}

	/**
	 * Identifier for group or mode
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Adds a data record to this measurement.
	 *
	 * @param from  from time (inclusive)
	 * @param to    to time (exclusive)
	 * @param value observed value
	 * @return this instance
	 */
	public Measurement<T> put(double from, double to, double value) {

		Record exists = getRecord(from);
		if (exists != null)
			throw new IllegalArgumentException("There is already an record for this range: " + exists);

		records.add(new Record(from, to, value));

		return this;
	}

	/**
	 * Removes a data record.
	 *
	 * @param from from time
	 * @return the removed instance, or null if non existed.
	 */
	@Nullable
	public Record remove(double from) {

		Record r = getRecord(from);
		if (r == null)
			return null;

		records.remove(r);
		return r;
	}

	/**
	 * Number of data records.
	 */
	public int size() {
		return records.size();
	}

	/**
	 * Return the record valid for {@code}. Null if there is no such record.
	 */
	@Nullable
	public Record getRecord(double time) {
		Record r = records.floor(new Record(time));
		if (r != null && time < r.to)
			return r;

		return null;
	}

	/**
	 * Gets all available records sorted by occurrence.
	 */
	public NavigableSet<Record> getRecords() {
		return records;
	}

	@Override
	public Iterator<Record> iterator() {
		return records.iterator();
	}

	@Override
	public Id<T> getId() {
		return id;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	@Override
	public Coord getCoord() {
		return coord;
	}

	/**
	 * Holds one measurement obtained for time {@code from} (inclusive) until {@code to} (exclusive).
	 */
	public static class Record implements Comparable<Record> {

		private final double from;
		private final double to;
		private double value;

		private Record(double from) {
			this.from = from;
			this.to = 0;
			this.value = 0;
		}

		Record(double from, double to, double value) {
			this.from = from;
			this.to = to;
			this.value = value;
		}

		public double getFrom() {
			return from;
		}

		public double getTo() {
			return to;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public double addValue(double value) {
			return this.value += value;
		}

		@Override
		public int compareTo(Record o) {
			return Double.compare(from, o.from);
		}

		@Override
		public String toString() {
			return "Record{" +
					"from=" + from +
					", to=" + to +
					", value=" + value +
					'}';
		}
	}
}
