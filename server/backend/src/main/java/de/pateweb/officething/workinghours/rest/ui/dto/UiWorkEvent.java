package de.pateweb.officething.workinghours.rest.ui.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString(callSuper=false, includeFieldNames=true)
@RequiredArgsConstructor
@Data
public class UiWorkEvent {
	
	private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime eventTime;
    
    private String clientInfo;
    
    private String rfidTag;
	
}
