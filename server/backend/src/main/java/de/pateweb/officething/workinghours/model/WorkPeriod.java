package de.pateweb.officething.workinghours.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 
 * @author Octavian Pate
 *
 */
@Entity
@Table(name = "workperiod")
@Data
public class WorkPeriod implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -7270726574793877083L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Long rfidUid;

    @Basic
    private Instant workDate;

    @Basic
    private Instant workStart;
    
    @Basic
    private Instant workFinish;
    
    private Long workDurationSeconds;    
    
    private Long startEventId;
    
    private Long finishEventId;

}
