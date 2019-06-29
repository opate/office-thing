package de.pateweb.officething.workinghours.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

/**
 * 
 * @author Octavian Pate
 *
 */
@Entity
@Table(name = "rfidtag")
@Data
public class RfidTag implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7596546288636917709L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    String rfidUidHex;
    
    Long rfidUid;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
	User user;
	
	String tagType;

	Instant validUntil;

    boolean deactivated;
    
    String info;
}
