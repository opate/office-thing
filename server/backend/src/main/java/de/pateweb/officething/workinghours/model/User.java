package de.pateweb.officething.workinghours.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import de.pateweb.officething.workinghours.UserRole;
import lombok.Data;

/**
 * 
 * @author Octavian Pate
 *
 */
@Entity
@Table(name = "users")
@Data
public class User implements Serializable{

	private static final long serialVersionUID = 5063235781132278933L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
	private RfidTag currentRfidTag;
	
	private boolean deactivated;
	
	private String name;
	
	private String givenName;
	
	private String password;
	
	private String email;
	
	@OneToMany(mappedBy = "user")
	private Set<RfidTag> allRfidTags;

    @ManyToOne
    @JoinColumn(name="customer_id", nullable=false)
	Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private UserRole role;
}