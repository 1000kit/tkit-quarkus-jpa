package org.tkit.quarkus.jpa.test;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ORDER_BID")
public class OrderBID implements Serializable {
    
        /** Use uniq Sequence per Business ID Table */
        @Id
        @Column(name = "GUID")
        @SequenceGenerator(name = "GEN_ORDER", sequenceName = "SEQ_ORDER_BID", allocationSize = 1, initialValue = 1)
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GEN_ORDER")
        private Long guid;
}
