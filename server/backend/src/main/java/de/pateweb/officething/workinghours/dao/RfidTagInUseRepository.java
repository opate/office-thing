package de.pateweb.officething.workinghours.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import de.pateweb.officething.workinghours.model.RfidTagInUse;

public interface RfidTagInUseRepository extends CrudRepository<RfidTagInUse, Long>{

	public Optional<RfidTagInUse> findByRfidUid(Long rfidUid);
}
