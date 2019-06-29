package de.pateweb.officething.climate.thingspeak;
/**
 * Based on https://github.com/angryelectron/thingspeak-java
 */
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Date;

public class Entry {

    /**
     * The names of these private members must match the JSON fields in a
     * channel feed returned by ThingSpeak. If they don't, GSON might not be
     * able to deserialize the JSON feed into Entry objects. Note that
     * 'longitude' and 'latitude' are returned by feeds, but 'lat' and 'long'
     * are used when updating.
     */
    private Date created_at;
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;
    private String field6;
    private String field7;
    private String field8;


    private final MultiValueMap<String, String> updateMap = new LinkedMultiValueMap<>();

    /**
     * Get a map of all fields in a format compatible with the API's update
     * parameters.  Used internally by
     * @return Field map.
     */
    MultiValueMap<String, String> getUpdateMap() {
        return updateMap;
    }

    /**
     * Get data for a field.  Fields must be enabled via the web in the Channel's
     * settings.
     * @param field 1-8
     * @return Field data; null for status feeds,  undefined fields, and field
     * feeds where field was not specified.
     */
    public Object getField(Integer field) {
        switch(field) {
            case 1:
                return field1;
            case 2:
                return field2;
            case 3:
                return field3;
            case 4:
                return field4;
            case 5:
                return field5;
            case 6:
                return field6;
            case 7:
                return field7;
            case 8:
                return field8;
        }
        throw new IllegalArgumentException("Invalid field.");
    }

    /**
     * Set the value for a field.  Fields must be enabled via the web in the Channel's
     * settings.
     * @param field 1-8.
     * @param value Value for field.
     */
    public void setField(Integer field, String value) {
        switch(field) {
            case 1:
                field1 = value;
                updateMap.add("field1", value);
                return;
            case 2:
                field2 = value;
                updateMap.add("field2", value);
                return;
            case 3:
                field3 = value;
                updateMap.add("field3", value);
                return;
            case 4:
                field4 = value;
                updateMap.add("field4", value);
                return;
            case 5:
                field5 = value;
                updateMap.add("field5", value);
                return;
            case 6:
                field6 = value;
                updateMap.add("field6", value);
                return;
            case 7:
                field7 = value;
                updateMap.add("field7", value);
                return;
            case 8:
                field8 = value;
                updateMap.add("field8", value);
                return;
        }
        throw new IllegalArgumentException("Invalid field.");
    }

    /**
     * Set the created date of an entry. If not explicitly set, the channel update time is used.
     * Useful when entries are not created and updated at the same time (offline mode, queuing to avoid rate-limiting, etc.)
     * @param created date which will be send to thingspeak
     */
    public void setCreated(Date created) {
        this.created_at = created;
        updateMap.add("created_at", created.toString());
    }

    /**
     * Get date on which this channel entry was created.  to adjust timezones.
     * @return Date.
     */
    public Date getCreated() {
        return created_at;
    }

}
