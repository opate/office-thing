package de.pateweb.officething.workinghours.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import de.pateweb.officething.workinghours.model.StartedWorkPeriod;

public interface StartedWorkPeriodRepository extends CrudRepository<StartedWorkPeriod, Long>{

	public Optional<StartedWorkPeriod> findByUserId(Long userId);
}
