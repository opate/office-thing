package de.pateweb.officething.stock;

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
@Table(name = "stock")
@Data
public class Stock implements Serializable {

    private static final long serialVersionUID = -2952735900715107252L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private float stockValue;

    @Basic
    private Instant stockUpdatedAt;


}
