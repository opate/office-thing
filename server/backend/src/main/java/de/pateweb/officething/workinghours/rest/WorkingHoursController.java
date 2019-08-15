package de.pateweb.officething.workinghours.rest;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.workinghours.UserRole;
import de.pateweb.officething.workinghours.dao.CustomerRepository;
import de.pateweb.officething.workinghours.dao.RfidTagInUseRepository;
import de.pateweb.officething.workinghours.dao.RfidTagRepository;
import de.pateweb.officething.workinghours.dao.UserRepository;
import de.pateweb.officething.workinghours.dao.WorkEventRepository;
import de.pateweb.officething.workinghours.dao.WorkPeriodRepository;
import de.pateweb.officething.workinghours.model.Customer;
import de.pateweb.officething.workinghours.model.RfidTag;
import de.pateweb.officething.workinghours.model.RfidTagInUse;
import de.pateweb.officething.workinghours.model.User;
import de.pateweb.officething.workinghours.model.WorkEvent;
import de.pateweb.officething.workinghours.model.WorkPeriod;

public class WorkingHoursController {

	private static final Logger LOG = LoggerFactory.getLogger(WorkingHoursController.class);

	private final String UNAUTHORIZED = "Unauthorized";
	
	private Boolean createUserIfUnknown;

	private String timeZoneId;

	@Value("${workinghours.createUserIfUnknown}")
	public void setCreateUserIfUnknown(Boolean create) {
		createUserIfUnknown = create;
	}

	@Value("${general.timezoneid}")
	public void setTimeZoneId(String zoneId) {
		timeZoneId = zoneId;
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
	RfidTagInUseRepository rfidTagInUseRespository;


	
	/**
	 * Fast preview in browser as JSON
	 * 
	 * @return
	 */
	@GetMapping("/workperiod")
	public ResponseEntity<?> getWorkPeriodPerUser() {
		
		LOG.info("getWorkPeriodPerUser()");
		
		String currentUserEmail = "";

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			currentUserEmail = authentication.getName();
			LOG.info("user: {}", currentUserEmail);
		} else {
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

		if (!currentUserEmail.isEmpty()) {

			List<WorkPeriodWebDTO> returnList = getWorkPeriodWebDtoList(currentUserEmail);
			return new ResponseEntity<>(returnList, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

	}

	private List<WorkPeriodWebDTO> getWorkPeriodWebDtoList(String currentUserEmail) {
		
		Long hours; 
		Long minutes; 
		Long seconds;
		String duration;
		ZonedDateTime finish;

		List<WorkPeriodWebDTO> returnList = new ArrayList<>();
		
		Optional<User> userCandidate = userRepository.findFirstByEmail(currentUserEmail);

		if (userCandidate.isPresent()) {
			User user = userCandidate.get();
			List<WorkPeriod> findAllByRfidUidInOrderByWorkDateDesc = workPeriodRepository
					.findAllByRfidUidInOrderByWorkDateDesc(user.getCurrentRfidTag().getRfidUid());

			for (WorkPeriod e : findAllByRfidUidInOrderByWorkDateDesc) {
				Long totalSecs = e.getWorkDurationSeconds();

				duration = "";
				finish = null;

				if (totalSecs != null) {
					hours = totalSecs / 3600;
					minutes = (totalSecs % 3600) / 60;
					seconds = totalSecs % 60;
					duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
					finish = ZonedDateTime.ofInstant(e.getWorkFinish(), ZoneId.of(timeZoneId));
				}

				WorkPeriodWebDTO newWebDTO = new WorkPeriodWebDTO();
				newWebDTO.setWorkDuration(duration);
				newWebDTO.setWorkFinish(finish);
				newWebDTO.setWorkStart(ZonedDateTime.ofInstant(e.getWorkStart(), ZoneId.of(timeZoneId)));
				returnList.add(newWebDTO);
			}
		}
		
		return returnList;
	}

	/**
	 * just for com test with angular
	 * 
	 * @return
	 */
	@GetMapping("/workinghoursusers")
	public List<User> getWorkingHoursUser() {
		
		LOG.debug("getWorkingHoursUser()");
		
		return (List<User>) userRepository.findAll();
	}

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
	public ResponseEntity<?> newWorkEvent(@RequestParam("rfid_uid") String rfidUidHex,
			@RequestParam(name="event_time", required = false) String eventTimeUtc, @RequestParam(name="client", required=false) String client) {

		LOG.info("newWorkEvent() for rfid {}", rfidUidHex);

		// temporary part to support current Arduino code. Remove it when Arduino sends event_time in UTC
		if (eventTimeUtc == null)
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
			eventTimeUtc = ZonedDateTime.now().format(formatter);
		}
		
		if (client == null)
		{
			client = "unknown";
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
			newEventForUser.setClientInfo(client);
			newEventForUser.setEventTime(currentEventTime);
			newEventForUser.setRfidTag(rfidTag);

			eventRepository.save(newEventForUser);

			Optional<RfidTagInUse> tagInUseCandidate = rfidTagInUseRespository.findByRfidUid(rfidUid);

			if (tagInUseCandidate.isPresent()) {

				RfidTagInUse rfidTagInUse = tagInUseCandidate.get();
				
				WorkPeriod workPeriod = workPeriodRepository.findById(rfidTagInUse.getWorkPeriodId()).get();

				workPeriod.setWorkFinish(currentEventTime);
				workPeriod.setFinishEventId(newEventForUser.getId());

				Long durationSeconds = Duration.between(workPeriod.getWorkStart(), workPeriod.getWorkFinish())
						.toSeconds();
				workPeriod.setWorkDurationSeconds(durationSeconds);
				workPeriodRepository.save(workPeriod);

				rfidTagInUseRespository.deleteById(tagInUseCandidate.get().getId());

				String info = "End of WorkPeriod detected";
				LOG.info(info);
				
				return new ResponseEntity<>(info, HttpStatus.OK);
			}
			// tag is not in use so create new workperiod
			else {
				WorkPeriod newWorkPeriod = new WorkPeriod();
				newWorkPeriod.setRfidUid(rfidUid);
				newWorkPeriod.setStartEventId(newEventForUser.getId());
				newWorkPeriod.setWorkDate(newEventForUser.getEventTime());
				newWorkPeriod.setWorkStart(newEventForUser.getEventTime());

				workPeriodRepository.save(newWorkPeriod);

				RfidTagInUse newTagInUse = new RfidTagInUse();
				newTagInUse.setRfidUid(rfidUid);
				newTagInUse.setWorkPeriodId(newWorkPeriod.getId());

				rfidTagInUseRespository.save(newTagInUse);

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
				newEvent.setRfidTag(newRfidTag);

				eventRepository.save(newEvent);

				WorkPeriod newWorkPeriod = new WorkPeriod();
				newWorkPeriod.setRfidUid(rfidUid);
				newWorkPeriod.setStartEventId(newEvent.getId());
				newWorkPeriod.setWorkDate(newEvent.getEventTime());
				newWorkPeriod.setWorkStart(newEvent.getEventTime());

				workPeriodRepository.save(newWorkPeriod);

				RfidTagInUse newTagInUse = new RfidTagInUse();
				newTagInUse.setRfidUid(rfidUid);
				newTagInUse.setWorkPeriodId(newWorkPeriod.getId());

				rfidTagInUseRespository.save(newTagInUse);

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
