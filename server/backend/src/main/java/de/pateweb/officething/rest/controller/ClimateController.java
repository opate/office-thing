package de.pateweb.officething.rest.controller;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.climate.Climate;
import de.pateweb.officething.climate.ClimateRepository;
import de.pateweb.officething.climate.thingspeak.Channel;
import de.pateweb.officething.climate.thingspeak.Entry;
import de.pateweb.officething.rest.dto.ClimateDTO;
import de.pateweb.officething.rest.dto.LaMetricDTO;
import de.pateweb.officething.utils.TemporalUtils;

@RestController
public class ClimateController {

	private static final Logger LOG = LoggerFactory.getLogger(ClimateController.class);

	@Autowired
	ClimateRepository climateRepository;

	private String apiwritekey;

	private Integer channelid;

	// properties set in Other Sources/src/main/resources/application.properties
	@Value("${thingspeak.climate.channelid}")
	public void setChannelId(Integer cid) {
		channelid = cid;
	}

	@Value("${thingspeak.climate.apiwritekey}")
	public void setApiWriteKey(String writeKey) {
		apiwritekey = writeKey;
	}


	
	/**
	 * 	 {
	    "frames": [
	        {
	            "text": "humidity",
	            "icon": "i3359"
	        },
	        {
	            "text": "temp",
	            "icon": "i2056"
	        },
	        {
	            "text": "time",
	            "icon": "i1820"
	        }
	    ]
	 }
	 * @return
	 */
	@GetMapping("/climate/lametric/last")
	public LaMetricDTO getLastClimateForLaMetricLCD() {
		
		LOG.debug("getLastClimateForLaMetricLCD()");

		Climate climateResult;

		climateResult = climateRepository.findTopByOrderByClimateUpdatedAtDesc();

		if (climateResult == null) {
			return new LaMetricDTO();
		}

		ZonedDateTime zdt = TemporalUtils.instantToUsersZdt(climateResult.getClimateUpdatedAt());
		String updateTime = DateTimeFormatter.ofPattern("HH:mm").format(zdt);

		LaMetricDTO result = new LaMetricDTO();
		result.addEntry(Float.toString(climateResult.getHumidity()) + "%", "i3359");
		result.addEntry(Float.toString(climateResult.getTemperature()) + "°", "i2056");
		result.addEntry(updateTime + "h", "i2056");

		return result;
	}

	@GetMapping("/climate/last")
	public ClimateDTO getLastClimate() {
		
		LOG.debug("getLastClimate()");

		Climate climateResult;

		climateResult = climateRepository.findTopByOrderByClimateUpdatedAtDesc();

		if (climateResult == null) {
			return new ClimateDTO();
		}

		ClimateDTO response = new ClimateDTO();
		response.setClimateUpdatedAt(TemporalUtils.instantToUsersZdt(climateResult.getClimateUpdatedAt()));
		response.setHumidity(climateResult.getHumidity());
		response.setTemperature(climateResult.getTemperature());
		return response;
	}
	
	@GetMapping ("climate/last/{count}")
	public List<ClimateDTO> getLastXclimates(@PathVariable String count) {
		
		List<ClimateDTO> climateDtoList = new ArrayList<>();

		Iterable<Climate> climates = climateRepository.findByOrderByClimateUpdatedAtDesc(PageRequest.of(0, Integer.valueOf(count)));
		
		Iterator<Climate> it = climates.iterator();
		while (it.hasNext()) {
			
			Climate result = it.next();
			
			ClimateDTO climateDTO = new ClimateDTO();
			climateDTO.setClimateUpdatedAt(TemporalUtils.instantToUsersZdt(result.getClimateUpdatedAt()));
			climateDTO.setHumidity(result.getHumidity());
			climateDTO.setTemperature(result.getTemperature());
			climateDtoList.add(climateDTO);
		}

		return climateDtoList;		
		
	}

	@GetMapping("/climate")
	public List<ClimateDTO> getAllClimates() {

		LOG.debug("getAllClimates()");
		
		List<ClimateDTO> climateDtoList = new ArrayList<>();

		Iterable<Climate> climates = climateRepository.findTop1000ByOrderByClimateUpdatedAtDesc();
		Iterator<Climate> it = climates.iterator();
		while (it.hasNext()) {
			
			Climate result = it.next();
			
			ClimateDTO climateDTO = new ClimateDTO();
			climateDTO.setClimateUpdatedAt(TemporalUtils.instantToUsersZdt(result.getClimateUpdatedAt()));
			climateDTO.setHumidity(result.getHumidity());
			climateDTO.setTemperature(result.getTemperature());
			climateDtoList.add(climateDTO);
		}

		return climateDtoList;
	}

	/**
	 * 
	 * @param temperature
	 * @param humidity
	 * @return
	 */
	@PostMapping("/climate")
	public ResponseEntity<Climate> addNewClimateValues(@RequestParam("temp") float temperature,
			@RequestParam("hum") float humidity) {

		LOG.debug("addNewClimateValues()");
		
		Instant now = Instant.now();

		Climate newClimate = new Climate();
		newClimate.setHumidity(humidity);
		newClimate.setTemperature(temperature);
		newClimate.setClimateUpdatedAt(now);

		climateRepository.save(newClimate);

		pushToClimateThingspeakChannel(temperature, humidity, Date.from(TemporalUtils.instantToUsersZdt(now).toInstant()));

		return new ResponseEntity<>(null, HttpStatus.CREATED);
	}

	private void pushToClimateThingspeakChannel(float temperature, float humidity, Date created) {
		
		LOG.debug("pushToClimateThingspeakChannel()");
		
		String apiWriteKey = apiwritekey;
		Channel channel = new Channel(channelid, apiWriteKey, null);

		Entry entry = new Entry();
		entry.setField(1, Float.toString(temperature));
		entry.setField(2, Float.toString(humidity));
		entry.setCreated(created);
		Integer entryId;
		try {
			entryId = channel.updateEntry(entry);
			LOG.debug("Thingspeak updated. New entryId: {}", entryId);
		} catch (Exception ex) {
			LOG.error("Could not update Thingspeak Channel. ErrorMessage: {}", ex.getMessage());
		}

	}
}
