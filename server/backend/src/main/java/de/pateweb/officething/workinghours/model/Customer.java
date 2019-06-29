package de.pateweb.officething.workinghours.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "customer")
@Data
public class Customer implements Serializable{

	private static final long serialVersionUID = 1629060071152844587L;

	public static final String DEFAULT_CUSTOMER_NAME = "DefaultCustomer";
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	private String name;
	
	private String contact;
	
    @OneToMany
    @JoinColumn(name="id")
	private Set<User> users;
}
