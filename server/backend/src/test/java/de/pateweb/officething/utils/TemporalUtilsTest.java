package de.pateweb.officething.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class TemporalUtilsTest {

	@Test
	public void testInstantToUsersZdt() throws Exception {

		Instant now = Instant.now();
		
		DateTimeFormatter formatter =
			    DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
			                     .withLocale( Locale.GERMANY )
			                     .withZone( ZoneId.systemDefault() );		
		
		System.out.println("now: " + formatter.format(now));
		
		ZonedDateTime expected = ZonedDateTime.ofInstant(now, ZoneId.of("Europe/Berlin"));
		
		System.out.println("expected: " + expected);

		Assert.assertEquals(expected, TemporalUtils.instantToUsersZdt(now));
	}

}
