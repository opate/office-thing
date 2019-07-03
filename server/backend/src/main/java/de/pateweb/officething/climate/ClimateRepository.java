package de.pateweb.officething.climate;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface ClimateRepository extends CrudRepository<Climate, Long> {

    Climate findTopByOrderByClimateUpdatedAtDesc();

    List<Climate> findTop1000ByOrderByClimateUpdatedAtDesc();
    
    Page<Climate> findByOrderByClimateUpdatedAtDesc(Pageable pageable);
}
