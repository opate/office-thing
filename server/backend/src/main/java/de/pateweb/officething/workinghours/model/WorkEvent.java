package de.pateweb.officething.workinghours.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Basic;
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
 * @author Octavian
 *
 */
@Entity
@Table(name = "workevent")
@Data
public class WorkEvent implements Serializable{

	private static final long serialVersionUID = -1674041729853844631L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

	@OneToOne
	@JoinColumn(name="rfid_tag_id")
    private RfidTag rfidTag;
	
    @Basic
    private Instant eventTime;

    private String clientInfo;
}
