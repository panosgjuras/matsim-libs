package org.matsim.counts;

import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MatsimCountsTestV2 {

	@Test
	public void put() {


		Counts<Link> counts = new Counts<>();

		Measurement<Link> m = counts.createOrGetMeasurement(Id.createLinkId(1), Observation.COUNT, "test");

		m.setGroup("car");


		m.put(0, 900, 20);

		assertThat(m.getRecord(0).getValue())
				.isEqualTo(20);

		assertThat(m.getRecord(450).getValue())
				.isEqualTo(20);

		assertThat(m.getRecord(900))
				.isNull();


		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> m.put(20, 500, 100));
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> m.put(0, 500, 100));

		m.put(900, 910, 50);

		assertThat(m.getRecord(900).getValue())
				.isEqualTo(50);

	}
}