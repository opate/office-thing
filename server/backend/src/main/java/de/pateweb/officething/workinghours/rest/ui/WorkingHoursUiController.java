package de.pateweb.officething.workinghours.rest.ui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.workinghours.dao.WorkPeriodRepository;
import de.pateweb.officething.workinghours.model.WorkPeriod;
import de.pateweb.officething.workinghours.rest.ui.dto.User;

//@CrossOrigin(origins = "*")
@CrossOrigin()
@RestController
public class WorkingHoursUiController {
	
	private static final Logger LOG = LoggerFactory.getLogger(WorkingHoursUiController.class);

	@Autowired
	WorkPeriodRepository workPeriodRepository;	
	
	@GetMapping("/allworkperiods")
	public List<WorkPeriod> getAllWorkPeriods()
	{
		LOG.info("getAllWorkPeriods()");
		
		return (List<WorkPeriod>) workPeriodRepository.findAll();
	}
	
	@GetMapping(produces = "application/json")
	@RequestMapping({ "/validateLogin" })
	public User validateLogin() {
		return new User("User successfully authenticated");
	}	
}
