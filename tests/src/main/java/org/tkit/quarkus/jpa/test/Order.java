package org.tkit.quarkus.jpa.test;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.tkit.quarkus.jpa.models.AbstractBusinessTraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TABLE_ORDER")
public class Order extends AbstractBusinessTraceableEntity {

    private String orderTitle;

    /** **************** BUSINESS ID Section **********************************. */
    /**
     *  The work order number. Used object to generate a business Long ID
     *   BID is used from SUPER Class - so the BID can be loaded directly without join
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "BID")
    private OrderBID orderBID;

    @PrePersist
    private void prePersisteMethod() {
        if(orderBID == null) {orderBID = new OrderBID();};
    }
}
