package org.tkit.quarkus.jpa.test;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PagedQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserDAO extends AbstractDAO<User> {

    public PagedQuery<User> pageUsers(Page page) {
        CriteriaQuery<User> cq = criteriaQuery();
        cq.from(User.class);
        return createPageQuery(cq, page);
    }

    public PagedQuery<User> pageUsers(UserSearchCriteria criteria, Page page) {
        CriteriaQuery<User> cq = criteriaQuery();
        Root<User> root = cq.from(User.class);
        cq.distinct(false);
        if (criteria != null) {
            List<Predicate> predicates = new ArrayList<>();
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(cb.like(root.get(User_.NAME), criteria.getName() + "%"));
            }

            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[0]));
            }
        }
        return createPageQuery(cq, page);
    }
}
