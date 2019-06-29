package de.pateweb.officething.workinghours.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import de.pateweb.officething.workinghours.model.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Long>{

	Optional<Customer> findByName(String customerName);
}
