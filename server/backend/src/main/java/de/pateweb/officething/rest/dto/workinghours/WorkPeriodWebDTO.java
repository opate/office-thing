package de.pateweb.officething.rest.dto.workinghours;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString(callSuper=false, includeFieldNames=true)
@RequiredArgsConstructor
@Data
public class WorkPeriodWebDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'['[VV]']'")
	private ZonedDateTime workStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'['[VV]']'")
	private ZonedDateTime workFinish;
	
	private String workDuration;
	
}