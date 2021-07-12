package org.tkit.quarkus.jpa.test;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PagedQuery;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OrderDAO extends AbstractDAO<Order> {

    /**
     * Finds the entity by ID.
     *
     * @param id the entity ID.
     * @return the entity corresponding to the ID.
     * @throws DAOException if the method fails.
     */
    @Transactional(value = Transactional.TxType.SUPPORTS, rollbackOn = DAOException.class)
    public Order findByBID(final Long BID) throws DAOException {
        Order result = null;
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> root = cq.from(Order.class);
            Predicate bidPredicate = cb.equal(root.get(Order_.orderBID), BID);
            cq.where(bidPredicate);
            cq.select(root);
            TypedQuery<Order> query = getEntityManager().createQuery(cq);
            result = query.getSingleResult();

            return result;
        } catch (Exception e) {
            throw new DAOException(Errors.FIND_ENTITY_BY_ID_FAILED, e, entityName, BID);
        }
    }


}
