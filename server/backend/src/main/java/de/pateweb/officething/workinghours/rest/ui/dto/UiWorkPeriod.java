package de.pateweb.officething.workinghours.rest.ui.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString(callSuper=false, includeFieldNames=true)
@RequiredArgsConstructor
@Data
public class UiWorkPeriod {

    private Long id;
	
    private UiWorkEvent startWorkEvent;

    private UiWorkEvent finishWorkEvent;
	
	private String workDuration;
	
}