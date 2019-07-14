package de.pateweb.officething.stock.rest;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 *
 * @author Octavian Pate
 */
@Data
public class StockDTO {

	private float stockValue;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'['[VV]']'")
	private ZonedDateTime stockUpdatedAt;

}
