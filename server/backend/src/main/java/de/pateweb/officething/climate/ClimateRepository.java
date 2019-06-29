package de.pateweb.officething.climate;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ClimateRepository extends CrudRepository<Climate, Long> {

    Climate findTopByOrderByClimateUpdatedAtDesc();

    List<Climate> findTop1000ByOrderByClimateUpdatedAtDesc();
}
