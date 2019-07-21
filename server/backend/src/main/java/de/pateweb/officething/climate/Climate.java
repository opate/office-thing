package de.pateweb.officething.climate;

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
*/
@Entity
@Table(name = "climate")
@Data
public class Climate implements Serializable{
    private static final long serialVersionUID = -2952735913715107252L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Float humidity;
    
    private Float temperature;

    private Float pressureHpa;
    
    private Float gasKohm;
    
    private Float iaq;
    
    @Basic
    private Instant climateUpdatedAt;
	
}
