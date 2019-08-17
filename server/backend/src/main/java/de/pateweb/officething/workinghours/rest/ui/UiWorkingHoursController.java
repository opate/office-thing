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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.workinghours.dao.UserRepository;
import de.pateweb.officething.workinghours.dao.WorkPeriodRepository;
import de.pateweb.officething.workinghours.model.User;
import de.pateweb.officething.workinghours.model.WorkPeriod;
import de.pateweb.officething.workinghours.rest.ui.dto.UiUser;
import de.pateweb.officething.workinghours.rest.ui.dto.UiWorkPeriod;

//@CrossOrigin(origins = "*")
@CrossOrigin()
@RestController
@RequestMapping(path = "/ui/workinghours")
public class UiWorkingHoursController {

	private static final Logger LOG = LoggerFactory.getLogger(UiWorkingHoursController.class);

	private final String UNAUTHORIZED = "Unauthorized";

	private String timeZoneId;

	@Value("${general.timezoneid}")
	public void setTimeZoneId(String zoneId) {
		timeZoneId = zoneId;
	}

	@Autowired
	UserRepository userRepository;

	@Autowired
	WorkPeriodRepository workPeriodRepository;

	@GetMapping("/workperiod")
	public ResponseEntity<?> getWorkPeriodPerUser() {

		LOG.info("ui.getWorkPeriodPerUser()");

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

	@GetMapping(produces = "application/json")
	@RequestMapping({"/validateLogin"})
	public ResponseEntity<?> validateLogin() {

		LOG.info("ui.validateLogin()");

		String currentUserEmail = "";

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!(authentication instanceof AnonymousAuthenticationToken)) 
		{
		
			currentUserEmail = authentication.getName();
			LOG.info("user: {}", currentUserEmail);

			User user = userRepository.findFirstByEmail(currentUserEmail).get();

			UiUser currentUser = new UiUser();
			currentUser.setCustomerId(user.getCustomer().getId());
			currentUser.setEmail(currentUserEmail);
			currentUser.setGiven_name(user.getGivenName());
			currentUser.setName(user.getName());
			currentUser.setRole(user.getRole());

			return new ResponseEntity<>(currentUser, HttpStatus.OK);

		} else 
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);

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

				UiWorkPeriod newWebDTO = new UiWorkPeriod();
				newWebDTO.setWorkDuration(duration);
				newWebDTO.setWorkFinish(finish);
				newWebDTO.setWorkStart(ZonedDateTime.ofInstant(e.getWorkStart(), ZoneId.of(timeZoneId)));
				newWebDTO.setRfidUid(e.getRfidUid());
				returnList.add(newWebDTO);
			}
		}

		return returnList;
	}

}
