package de.pateweb.officething.rest.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class LaMetricDTO {

	@AllArgsConstructor(access = AccessLevel.PUBLIC)
	@Data
	public class LaMetricEntry {

		String text;
		String icon;

	}

	List<LaMetricEntry> frames = new ArrayList<>();

    public void addEntry(String text, String icon) {
        frames.add(new LaMetricEntry(text, icon));
    }
}
