package de.pateweb.officething.climate.rest;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class IaqDTO {
    
    private Float iaq;
    
    private String airQuality;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'['[VV]']'")
    private ZonedDateTime climateUpdatedAt;
}

