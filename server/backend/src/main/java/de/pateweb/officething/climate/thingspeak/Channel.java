package de.pateweb.officething.climate.thingspeak;

/**
 * Based on https://github.com/angryelectron/thingspeak-java
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class Channel {

	private static final Logger LOG = LoggerFactory.getLogger(Channel.class);

	private static final String APIURL = "http://api.thingspeak.com/update";
	private static final String APIHEADER = "X-THINGSPEAKAPIKEY";
	private final Integer channelId;
	private String readAPIKey;
	private String writeAPIKey;
	private RestTemplate restTemplate;

	public Channel(Integer usedChannelId, String writeKey, String readKey) {
		this.channelId = usedChannelId;
		this.readAPIKey = readKey;
		this.writeAPIKey = writeKey;

		this.restTemplate = new RestTemplate();
	}

	public Integer updateEntry(Entry entry) {

		LOG.debug("updateEntry()");

		HttpHeaders headers = new HttpHeaders();
		headers.add(APIHEADER, this.writeAPIKey);
		headers.add("Connection", "close");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(entry.getUpdateMap(), headers);

		Integer result = 0;

		try {

			ResponseEntity<String> response = restTemplate.postForEntity(APIURL, request, String.class);
			result = Integer.valueOf(response.getBody());
			
			if (response.getStatusCodeValue() != 200)
			{
				LOG.error("Thingspeak update failed. Status code: {}", response.getStatusCodeValue());
			}

		} catch (Exception e) 
		{
			LOG.error(e.getMessage());
		}

		return result;
	}
}
