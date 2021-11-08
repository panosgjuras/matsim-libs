package org.matsim.counts;

import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.IdentityTransformation;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.Stack;

/**
 * Reader for counts and observations according to <code>counts_v2.xsd</code>.
 */
public class CountsReaderMatsimV2 extends MatsimXmlParser {

	public CountsReaderMatsimV2(final Counts counts) {
		this( new IdentityTransformation(), counts );
	}

	public CountsReaderMatsimV2(
			final CoordinateTransformation coordinateTransformation,
			final Counts counts) {
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {

	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {

	}
}
