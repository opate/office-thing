package de.pateweb.officething.workinghours.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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

	@OneToOne
	@JoinColumn(name="user_id")	
    private User user;

    @Basic
    private Instant workStart;
    
    @Basic
    private Instant workFinish;
    
    private Long workDurationSeconds;    
    
	@OneToOne
	@JoinColumn(name="start_event_id")    
    private WorkEvent workStartEvent;
    
	@OneToOne
	@JoinColumn(name="finish_event_id")    
    private WorkEvent workFinishEvent;

}
