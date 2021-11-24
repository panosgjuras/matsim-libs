/*
 *   *********************************************************************** *
 *   project: org.matsim.*
 *   *********************************************************************** *
 *                                                                           *
 *   copyright       : (C) 2021 by the members listed in the COPYING,        *
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

package org.matsim.core.events;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.population.PopulationUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.testcases.utils.EventsCollector;
import org.matsim.utils.eventsfilecomparison.EventsFileComparator;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.matsim.testcases.MatsimTestUtils.EPSILON;

public class EventAttributesTest {
	@Rule
	public final MatsimTestUtils utils = new MatsimTestUtils();


	//------- Write-and-Read tests ------------

	/**
	 * Let's see if attributes are written / read correctly.
	 * Here: 1 Event with 1 ObjectAttribute
	 */
	@Test
	public void testAttributes_1event_String() {
		Activity act1 = PopulationUtils.createActivityFromLinkId("testType", Id.create("link1", Link.class));
		final ActivityStartEvent activityStartEvent1 = new ActivityStartEvent(5668.27, Id.create("person1", Person.class),
				act1.getLinkId(), Id.create("f792", ActivityFacility.class), act1.getType(), new Coord(234., 5.67));
		activityStartEvent1.getObjectAttributes().putAttribute("AttributeABC", "ABC2134");

		final ActivityStartEvent event = XmlEventsTester.testWriteReadXml(utils.getOutputDirectory() + "testEvents.xml",
				activityStartEvent1);

		assertEquals(5668.27, event.getTime(), EPSILON);
		assertEquals("person1", event.getPersonId().toString());
		assertEquals("link1", event.getLinkId().toString());
		assertEquals("f792", event.getFacilityId().toString());
		assertEquals("testType", event.getActType());
		assertEquals(234., event.getCoord().getX(), 0.);
		assertEquals(5.67, event.getCoord().getY(), 0.);
		assertEquals("Number of ObjectAttributes is not correct", 1, event.getObjectAttributes().size());
		assertTrue("ObjectAttribute is missing", event.getObjectAttributes().getAsMap().containsKey("AttributeABC"));
		assertEquals("Value of ObjectAttribute is not correct", "ABC2134", event.getObjectAttributes().getAttribute("AttributeABC"));
	}

	/**
	 * Let's see if attributes are written / read correctly.
	 * Here: 1 Event with 2 ObjectAttributes
	 */
	@Test
	public void testAttributes_1event_String_2Attributes() {
		Activity act1 = PopulationUtils.createActivityFromLinkId("testType", Id.create("link1", Link.class));
		final ActivityStartEvent activityStartEvent1 = new ActivityStartEvent(5668.27, Id.create("person1", Person.class),
				act1.getLinkId(), Id.create("f792", ActivityFacility.class), act1.getType(), new Coord(234., 5.67));
		activityStartEvent1.getObjectAttributes().putAttribute("AttributeABC", "ABC2134");
		activityStartEvent1.getObjectAttributes().putAttribute("AttributeDEF", "ABC5678");

		final ActivityStartEvent event = XmlEventsTester.testWriteReadXml(utils.getOutputDirectory() + "testEvents.xml",
				activityStartEvent1);

		assertEquals(5668.27, event.getTime(), EPSILON);
		assertEquals("person1", event.getPersonId().toString());
		assertEquals("link1", event.getLinkId().toString());
		assertEquals("f792", event.getFacilityId().toString());
		assertEquals("testType", event.getActType());
		assertEquals(234., event.getCoord().getX(), 0.);
		assertEquals(5.67, event.getCoord().getY(), 0.);
		assertEquals("Number of ObjectAttributes is not correct", 2, event.getObjectAttributes().size());
		assertTrue("1st ObjectAttribute is missing", event.getObjectAttributes().getAsMap().containsKey("AttributeABC"));
		assertTrue("2md ObjectAttribute is missing", event.getObjectAttributes().getAsMap().containsKey("AttributeDEF"));
		assertEquals("Value of 1st ObjectAttribute is not correct", "ABC2134", event.getObjectAttributes().getAttribute("AttributeABC"));
		assertEquals("Value of 2nd ObjectAttribute is not correct", "ABC5678", event.getObjectAttributes().getAttribute("AttributeDEF"));
	}

	/**
	 * 	Let's see if attributes are written / read correctly.
	 * 	Here: 1 Event with 1 ObjectAttributes with an ObjectType
	 *
	 * TODO: Does this make sense with the Objects?
	 */
	@Test
	public void testAttributes_1event_Object() {
		Activity act1 = PopulationUtils.createActivityFromLinkId("testType", Id.create("link1", Link.class));
		final ActivityStartEvent activityStartEvent1 = new ActivityStartEvent(5668.27, Id.create("person1", Person.class),
				act1.getLinkId(), Id.create("f792", ActivityFacility.class), act1.getType(), new Coord(234., 5.67));
		activityStartEvent1.getObjectAttributes().putAttribute("NumberOfXYZ", Integer.valueOf(7));

		final ActivityStartEvent event = XmlEventsTester.testWriteReadXml(utils.getOutputDirectory() + "testEvents.xml",
				activityStartEvent1);

		assertEquals(5668.27, event.getTime(), EPSILON);
		assertEquals("person1", event.getPersonId().toString());
		assertEquals("link1", event.getLinkId().toString());
		assertEquals("f792", event.getFacilityId().toString());
		assertEquals("testType", event.getActType());
		assertEquals(234., event.getCoord().getX(), 0.);
		assertEquals(5.67, event.getCoord().getY(), 0.);
		assertEquals("Number of ObjectAttributes is not correct", 1, event.getObjectAttributes().size());
		assertTrue("ObjectAttribute is missing", event.getObjectAttributes().getAsMap().containsKey("NumberOfXYZ"));
		assertEquals("Value of ObjectAttribute is not correct", 2, event.getObjectAttributes().getAttribute("NumberOfXYZ"));
	}

//		-----

	/**
	 * Let's see if attributes are written / read correctly.
	 * Here: 2 Events with different ObjectAttributes
	 */
	@Test
	public void testAttributes_2events_String() {
		String filename = this.utils.getOutputDirectory() + "testEvents.xml";
		EventWriterXML writer = new EventWriterXML(filename);

		ActivityStartEvent actStartEvent1 = new ActivityStartEvent(3600.0, Id.create("Agent1", Person.class),
				Id.create("link1", Link.class), null, null , null);
		actStartEvent1.getObjectAttributes().putAttribute("OA1", "value1");
		actStartEvent1.getObjectAttributes().putAttribute("OA2", "200");

		ActivityStartEvent actStartEvent2 = new ActivityStartEvent(7200.0, Id.create("Agent2", Person.class),
				Id.create("link2", Link.class), null, null , null);
		actStartEvent2.getObjectAttributes().putAttribute("OA42", "value2");

		writer.handleEvent(actStartEvent1);
		writer.handleEvent(actStartEvent2);
		writer.closeFile();
		Assert.assertTrue(new File(filename).exists());

		EventsManager events = EventsUtils.createEventsManager();
		EventsCollector collector = new EventsCollector();
		events.addHandler(collector);
		events.initProcessing();
		// this is already a test: is the XML valid so it can be parsed again?
		new MatsimEventsReader(events).readFile(filename);
		events.finishProcessing();

		assertEquals("there must be 2 events.", 2, collector.getEvents().size());

		ActivityStartEvent event1 = (ActivityStartEvent) collector.getEvents().get(0);
		assertEquals("link1", event1.getLinkId().toString());
		assertEquals("Number of ObjectAttributes is not correct", 2, event1.getObjectAttributes().size());
		assertEquals("value1", event1.getObjectAttributes().getAttribute("OA1"));
		assertEquals("200", event1.getObjectAttributes().getAttribute("OA1"));

		ActivityStartEvent event2 = (ActivityStartEvent) collector.getEvents().get(1);
		assertEquals("link2", event2.getLinkId().toString());
		assertEquals("Number of ObjectAttributes is not correct", 1, event2.getObjectAttributes().size());
		assertEquals("value2", event2.getObjectAttributes().getAttribute("OA42"));
	}

	//------- Read-and-Write tests ------------
	//TODO
	/**
	 * Read in an eventsFile, write it out and compare the results
	 * Here: 2 Events with different ObjectAttributes
	 */
	@Test
	public void testReadWrite(){
		final String inputFile = utils.getClassInputDirectory() + "/testEvents.xml";
		final String outputFile = utils.getOutputDirectory() + "testEvents.xml";

		EventsManager events = EventsUtils.createEventsManager();

		//read in
		EventsCollector collector = new EventsCollector();
		events.addHandler(collector);
		events.initProcessing();

		new MatsimEventsReader(events).readFile(inputFile);
		events.finishProcessing();

		assertEquals("there must be 3 events.", 3, collector.getEvents().size());
		Event readEvent = collector.getEvents().iterator().next();

		//write out

		EventWriterXML writer = new EventWriterXML(outputFile);
		for (Event event : collector.getEvents()) {
			writer.handleEvent(event);
		}
		writer.closeFile();
		assertTrue(new File(outputFile).exists());

		//compare
		assertTrue(EventsFileComparator.compare(inputFile, outputFile) == EventsFileComparator.Result.FILES_ARE_EQUAL);
		//TODO: The above one is passing. The below one failing...
		MatsimTestUtils.compareFilesLineByLine(inputFile, outputFile);
	}


	//------- Intern ------------
	// TODO: Wie sehr wollen wir hier in die Tiefe gehen - analog @link(PopulationAttributeConversionTest) oder
	//  reichen uns die Standart-Converters?
//	private static class CustomClass {
//		private final String value;
//
//		private CustomClass(String value) {
//			this.value = value;
//		}
//
//		@Override
//		public boolean equals(Object o) {
//			if (this == o) return true;
//			if (o == null || getClass() != o.getClass()) return false;
//			CustomClass that = (CustomClass) o;
//			return Objects.equals(value, that.value);
//		}
//
//		@Override
//		public int hashCode() {
//			return Objects.hash(value);
//		}
//	}
//
//	private static class CustomClassConverter implements AttributeConverter<CustomClass> {
//
//		@Override
//		public CustomClass convert(String value) {
//			return new CustomClass(value);
//		}
//
//		@Override
//		public String convertToString(Object o) {
//			return ((CustomClass) o).value;
//		}
//	}
}
