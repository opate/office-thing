package de.pateweb.officething.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TemporalUtils {

	private static String timeZoneId;
	
	@Value("${general.timezoneid}")
	private void setTimeZoneId(String zoneId) {
		timeZoneId = zoneId;
	}	
    
    public static ZonedDateTime instantToUsersZdt(Instant instant)
    {
    	return ZonedDateTime.ofInstant(instant, ZoneId.of(timeZoneId));
    }
}
