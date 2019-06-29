package de.pateweb.officething.workinghours.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import de.pateweb.officething.workinghours.model.WorkPeriod;


/**
 * 
 * @author Octavian Pate
 *
 */
public interface WorkPeriodRepository extends CrudRepository<WorkPeriod, Long> {

	Optional<WorkPeriod> findTopByOrderByIdDesc();
	
	Optional<WorkPeriod> findTopByRfidUidInOrderByIdDesc(Long rfidUid);
	
	List<WorkPeriod> findAllByRfidUidInOrderByWorkDateDesc(Long rfidUid);
}
