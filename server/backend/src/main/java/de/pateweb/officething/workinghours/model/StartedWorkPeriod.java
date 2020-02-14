package de.pateweb.officething.workinghours.model;

import java.io.Serializable;

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
@Table(name = "startedworkperiod")
@Data
public class StartedWorkPeriod implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = -5404963395114126910L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
	@OneToOne
	@JoinColumn(name="user_id")    
    private User user;
    
	@OneToOne
	@JoinColumn(name="work_period_id") 	
    private WorkPeriod workPeriod;
}
