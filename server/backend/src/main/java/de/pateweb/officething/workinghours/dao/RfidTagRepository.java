package de.pateweb.officething.workinghours.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import de.pateweb.officething.workinghours.model.RfidTag;

public interface RfidTagRepository extends CrudRepository<RfidTag, Long>{

	public Optional<RfidTag> findByRfidUid(Long rfidUid);
}
