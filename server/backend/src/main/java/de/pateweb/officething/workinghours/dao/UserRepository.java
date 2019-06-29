package de.pateweb.officething.workinghours.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import de.pateweb.officething.workinghours.model.User;

/**
 * 
 * @author Octavian Pate
 *
 */
public interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findFirstByEmail(String email);
	
	Optional<User> findByCurrentRfidTagRfidUid(Long rfidUid);
}
