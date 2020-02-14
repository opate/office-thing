package de.pateweb.officething.workinghours.rest;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.workinghours.UserRole;
import de.pateweb.officething.workinghours.dao.CustomerRepository;
import de.pateweb.officething.workinghours.dao.StartedWorkPeriodRepository;
import de.pateweb.officething.workinghours.dao.RfidTagRepository;
import de.pateweb.officething.workinghours.dao.UserRepository;
import de.pateweb.officething.workinghours.dao.WorkEventRepository;
import de.pateweb.officething.workinghours.dao.WorkPeriodRepository;
import de.pateweb.officething.workinghours.model.Customer;
import de.pateweb.officething.workinghours.model.RfidTag;
import de.pateweb.officething.workinghours.model.StartedWorkPeriod;
import de.pateweb.officething.workinghours.model.User;
import de.pateweb.officething.workinghours.model.WorkEvent;
import de.pateweb.officething.workinghours.model.WorkPeriod;

/**
 * REST endpoints for office-thing iot
 * 
 * @author Octavian Pate
 *
 */
@CrossOrigin()
@RestController
public class WorkingHoursController {

	private static final Logger LOG = LoggerFactory.getLogger(WorkingHoursController.class);

	private static final String UNAUTHORIZED = "Unauthorized";
	
	private Boolean createUserIfUnknown;

	@Value("${workinghours.createUserIfUnknown}")
	public void setCreateUserIfUnknown(Boolean create) {
		createUserIfUnknown = create;
	}

	@Autowired
	UserRepository userRepository;

	@Autowired
	WorkEventRepository eventRepository;

	@Autowired
	WorkPeriodRepository workPeriodRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	RfidTagRepository rfidTagRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	StartedWorkPeriodRepository startedWorkPeriodRespository;

	/**
	 * 
	 * 
	 * @param rfidUidHex
	 * @param eventTimeUtc  Format example: 2014-12-03T10:15:30.00Z
	 * @param client
	 * @return
	 */
	
	//addNewWorkingHoursEvent(@RequestParam("rfid_uid") String rfidUid)
	@PostMapping("/workevent")
	public ResponseEntity<?> newWorkEventByRfid(@RequestParam("rfid_uid") String rfidUidHex,
											@RequestParam(name="event_time", required = false) String eventTimeUtc, 
											@RequestParam(name="client", required=false) String client) 
	{

		LOG.info("newWorkEvent() for rfid {}", rfidUidHex);

		// temporary part to support current Arduino code. Remove it when Arduino sends event_time in UTC
		if (eventTimeUtc == null)
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
			eventTimeUtc = ZonedDateTime.now().format(formatter);
			if (client == null)
			{
				client = "unknown_client_current_timestamp";
			} else
			{
				client = "unknown_client_with_timestamp";
			}
		}
		
		
		Long rfidUid = Long.parseLong(rfidUidHex.replaceAll(":", ""), 16);

		RfidTag rfidTag = null;

		Optional<RfidTag> rfidTagCandidate = rfidTagRepository.findByRfidUid(rfidUid);
		if (rfidTagCandidate.isPresent()) {

			rfidTag = rfidTagCandidate.get();

			if (rfidTag.isDeactivated()
					|| (rfidTag.getValidUntil() != null && rfidTag.getValidUntil().isAfter(Instant.now()))) {
				LOG.info("RfidTag (UID: {}) event requested but tag is deactivated or valid date is e.", rfidUid);
				return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
			}

			User eventUser = rfidTag.getUser();

			if (eventUser.isDeactivated()) {
				LOG.info("User (Email: {}) tryed to check-in an event but user is deactivated.", eventUser.getEmail());
				return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
			}

			Instant currentEventTime = Instant.parse(eventTimeUtc);

			WorkEvent newEventForUser = new WorkEvent();
			newEventForUser.setClientInfo(client+rfidUidHex);
			newEventForUser.setEventTime(currentEventTime);
			newEventForUser.setUser(eventUser);
			newEventForUser.setRfidTag(rfidTag);

			eventRepository.save(newEventForUser);

			Optional<StartedWorkPeriod> startedWorkPeriodCandidate = startedWorkPeriodRespository.findByUserId(eventUser.getId());

			if (startedWorkPeriodCandidate.isPresent()) {

				StartedWorkPeriod startedWorkPeriod = startedWorkPeriodCandidate.get();
				
				WorkPeriod workPeriod = startedWorkPeriod.getWorkPeriod();

				workPeriod.setWorkFinishEvent(newEventForUser);
				workPeriod.setWorkFinish(newEventForUser.getEventTime());

				Long durationSeconds = Duration.between(workPeriod.getWorkStartEvent().getEventTime(), workPeriod.getWorkFinishEvent().getEventTime())
						.toSeconds();
				workPeriod.setWorkDurationSeconds(durationSeconds);
				workPeriodRepository.save(workPeriod);

				startedWorkPeriodRespository.delete(startedWorkPeriod);

				String info = "End of WorkPeriod detected";
				LOG.info(info);
				
				return new ResponseEntity<>(info, HttpStatus.OK);
			}
			// no startedworkperiod found so create new workperiod
			else {
				
				WorkPeriod newWorkPeriod = new WorkPeriod();
				newWorkPeriod.setWorkStart(newEventForUser.getEventTime());
				newWorkPeriod.setWorkStartEvent(newEventForUser);
				newWorkPeriod.setUser(eventUser);

				workPeriodRepository.save(newWorkPeriod);
				
				StartedWorkPeriod newStartedWorkPeriod = new StartedWorkPeriod();
				newStartedWorkPeriod.setUser(eventUser);
				newStartedWorkPeriod.setWorkPeriod(newWorkPeriod);
				
				startedWorkPeriodRespository.save(newStartedWorkPeriod);

				String info = "Begin of WorkPeriod detected";
				LOG.info(info);
				
				return new ResponseEntity<>(info, HttpStatus.CREATED);
			}

		}
		// rfid tag is unknown: store this rfidtag and create new user if indicated
		else {
			if (createUserIfUnknown) {
				Customer defaultCustomer = customerRepository.findById(1L).get();

				Date now = new Date();

				User newUser = new User();
				newUser.setCustomer(defaultCustomer);
				newUser.setRole(UserRole.USER);
				newUser.setCurrentRfidTag(null);
				newUser.setDeactivated(false);
				newUser.setEmail(String.valueOf(rfidUid));
				newUser.setGivenName("auto created GivenName " + now);
				newUser.setName("auto created Name" + now);
				newUser.setPassword(passwordEncoder.encode(rfidUidHex));

				userRepository.save(newUser);

				RfidTag newRfidTag = new RfidTag();
				newRfidTag.setDeactivated(false);
				newRfidTag.setInfo("auto created at " + now);
				newRfidTag.setRfidUid(rfidUid);
				newRfidTag.setRfidUidHex(rfidUidHex);
				newRfidTag.setTagType("unknown");
				newRfidTag.setUser(newUser);

				rfidTagRepository.save(newRfidTag);

				newUser.setCurrentRfidTag(newRfidTag);
				userRepository.save(newUser);

				WorkEvent newEvent = new WorkEvent();
				newEvent.setClientInfo(client);
				newEvent.setEventTime(Instant.parse(eventTimeUtc));
				newEvent.setUser(newUser);

				eventRepository.save(newEvent);

				WorkPeriod newWorkPeriod = new WorkPeriod();
				newWorkPeriod.setUser(newUser);
				newWorkPeriod.setWorkStartEvent(newEvent);
				newWorkPeriod.setWorkStart(newEvent.getEventTime());

				workPeriodRepository.save(newWorkPeriod);

				StartedWorkPeriod newStartedWorkPeriod = new StartedWorkPeriod();
				newStartedWorkPeriod.setUser(newUser);
				newStartedWorkPeriod.setWorkPeriod(newWorkPeriod);

				startedWorkPeriodRespository.save(newStartedWorkPeriod);

				String info = "This RFID tag is unknown, so new user has been created. WorkPeriod begin saved.";
				
				LOG.info(info);
				
				return new ResponseEntity<>(info, HttpStatus.ACCEPTED);

			}
			// unknown users not allowed
			else {
				
				String info = "This RFID Tag is unknown";
				LOG.info("{} and createUserIfUnknown is false", info);
				
				return new ResponseEntity<>(info, HttpStatus.UNAUTHORIZED);
			}
		}

	}
}
