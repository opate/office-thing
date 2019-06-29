package de.pateweb.officething.workinghours.model;

import java.io.Serializable;

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
@Table(name = "rfidtaginuse")
@Data
public class RfidTagInUse implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = -5404963395114126910L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    private Long rfidUid;
    
    private Long workPeriodId;
}
