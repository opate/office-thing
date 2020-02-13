package de.pateweb.officething.workinghours.rest.ui;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.workinghours.dao.UserRepository;
import de.pateweb.officething.workinghours.dao.WorkPeriodRepository;
import de.pateweb.officething.workinghours.model.User;
import de.pateweb.officething.workinghours.model.WorkPeriod;
import de.pateweb.officething.workinghours.rest.ui.dto.UiUser;
import de.pateweb.officething.workinghours.rest.ui.dto.UiWorkEvent;
import de.pateweb.officething.workinghours.rest.ui.dto.UiWorkPeriod;

//@CrossOrigin(origins = "*")
@CrossOrigin()
@RestController
@RequestMapping(path = "/ui/workinghours")
public class UiWorkingHoursController {

	private static final Logger LOG = LoggerFactory.getLogger(UiWorkingHoursController.class);

	private final String UNAUTHORIZED = "Unauthorized";
	private final String DELETED = "Delted";

	private String timeZoneId;

	@Value("${general.timezoneid}")
	public void setTimeZoneId(String zoneId) {
		timeZoneId = zoneId;
	}

	@Autowired
	UserRepository userRepository;

	@Autowired
	WorkPeriodRepository workPeriodRepository;

	@DeleteMapping("/workperiod/{workPeriodIdToDelete}")
	public ResponseEntity<?> deleteWorkPeriod(@PathVariable("workPeriodIdToDelete") Long idToDelete)
	{
		LOG.debug("ui.deleteWorkPeriod()");
		
		String currentUserEmail = "";

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			currentUserEmail = authentication.getName();
			LOG.info("user: {}", currentUserEmail);
		} else {
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

		if (!currentUserEmail.isEmpty()) {
			
			WorkPeriod wpToDelete = workPeriodRepository.findById(idToDelete).get();
			
			workPeriodRepository.delete(wpToDelete);
			return new ResponseEntity<>(DELETED, HttpStatus.ACCEPTED);
			
		} else {
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}
	}
	
	@GetMapping("/workperiod")
	public ResponseEntity<?> getWorkPeriodPerUser() {

		LOG.debug("ui.getWorkPeriodPerUser()");

		String currentUserEmail = "";

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			currentUserEmail = authentication.getName();
			LOG.info("user: {}", currentUserEmail);
		} else {
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

		if (!currentUserEmail.isEmpty()) {

			List<UiWorkPeriod> returnList = getWorkPeriodWebDtoList(currentUserEmail);
			return new ResponseEntity<>(returnList, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

	}

	@GetMapping(value = "/validateLogin", produces = "application/json")
	public ResponseEntity<?> validateLogin() {

		LOG.debug("ui.validateLogin()");

		String currentUserEmail = "";

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!(authentication instanceof AnonymousAuthenticationToken)) {

			currentUserEmail = authentication.getName();
			LOG.info("user: {}", currentUserEmail);

			User user = userRepository.findFirstByEmail(currentUserEmail).get();

			UiUser currentUser = new UiUser();
			currentUser.setCustomerId(user.getCustomer().getId());
			currentUser.setEmail(currentUserEmail);
			currentUser.setGivenName(user.getGivenName());
			currentUser.setName(user.getName());
			currentUser.setRole(user.getRole().toString());
			currentUser.setRights(new ArrayList<>());
			currentUser.setUserId(user.getId());
			currentUser.setPassword(new String());

			return new ResponseEntity<>(currentUser, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

	}

	private List<UiWorkPeriod> getWorkPeriodWebDtoList(String currentUserEmail) {

		Long hours;
		Long minutes;
		Long seconds;
		String duration;
		ZonedDateTime finish;

		List<UiWorkPeriod> returnList = new ArrayList<>();

		Optional<User> userCandidate = userRepository.findFirstByEmail(currentUserEmail);

		if (userCandidate.isPresent()) {
			User user = userCandidate.get();
			List<WorkPeriod> findAllByRfidUidInOrderByWorkDateDesc = workPeriodRepository
					.findAllByUserIdInOrderByWorkDateDesc(user.getId());

			for (WorkPeriod workPeriod : findAllByRfidUidInOrderByWorkDateDesc) {
				Long totalSecs = workPeriod.getWorkDurationSeconds();

				duration = "";
				finish = null;

				if (totalSecs != null) {
					hours = totalSecs / 3600;
					minutes = (totalSecs % 3600) / 60;
					seconds = totalSecs % 60;
					duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
					finish = ZonedDateTime.ofInstant(workPeriod.getFinishWorkEvent().getEventTime(), ZoneId.of(timeZoneId));
				}
				
				UiWorkEvent startWorkEvent = new UiWorkEvent();
				startWorkEvent.setEventTime(ZonedDateTime.ofInstant(workPeriod.getStartWorkEvent().getEventTime(), ZoneId.of(timeZoneId)));
				startWorkEvent.setClientInfo(workPeriod.getStartWorkEvent().getClientInfo());
				// TODO
				//startWorkEvent.setRfidTag(rfidTag);

				UiWorkEvent finishWorkEvent = new UiWorkEvent();
				finishWorkEvent.setEventTime(ZonedDateTime.ofInstant(workPeriod.getFinishWorkEvent().getEventTime(), ZoneId.of(timeZoneId)));
				finishWorkEvent.setClientInfo(workPeriod.getFinishWorkEvent().getClientInfo());
				// TODO
				//finishWorkEvent.setRfidTag(rfidTag);
				
				UiWorkPeriod newWebDTO = new UiWorkPeriod();
				newWebDTO.setId(workPeriod.getId());
				newWebDTO.setWorkDuration(duration);
				newWebDTO.setStartWorkEvent(startWorkEvent);
				newWebDTO.setFinishWorkEvent(finishWorkEvent);
				returnList.add(newWebDTO);
			}
		}

		return returnList;
	}

}
