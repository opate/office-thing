package de.pateweb.officething.workinghours.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
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

    private User user;

    @Basic
    private Instant workDate;
    
    private Long workDurationSeconds;    
    
    private WorkEvent startWorkEvent;
    
    private WorkEvent finishWorkEvent;

}
