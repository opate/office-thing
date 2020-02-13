package de.pateweb.officething.workinghours.dao;

import org.springframework.data.repository.CrudRepository;

import de.pateweb.officething.workinghours.model.WorkEvent;


/**
 * 
 * @author Octavian Pate
 *
 */
public interface WorkEventRepository extends CrudRepository<WorkEvent, Long> {

	WorkEvent findTopByOrderByIdDesc();
		
}