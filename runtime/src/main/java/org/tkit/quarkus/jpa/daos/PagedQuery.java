package org.tkit.quarkus.jpa.daos;

import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.stream.Stream;

/**
 * The page query.
 *
 * @param <T> the entity class.
 */
public class PagedQuery<T> {

    /**
     * The entity manager.
     */
    private EntityManager em;

    /**
     * The search criteria.
     */
    private CriteriaQuery<T> criteria;

    /**
     * The search count criteria.
     */
    private CriteriaQuery<Long> countCriteria;

    /**
     * The current page.
     */
    private Page page;

    /**
     * Default constructor.
     *
     * @param em       the entity manager.
     * @param criteria the search criteria
     * @param page     the start page.
     */
    public PagedQuery(EntityManager em, CriteriaQuery<T> criteria, Page page) {
        this.em = em;
        this.criteria = criteria;
        this.page = page;
        this.countCriteria = createCountCriteria(em, criteria);
    }

    public PageResult<T> getPageResult() {
        try {
            // get count
            Long count = em.createQuery(countCriteria).getSingleResult();
            // get stream
            Stream<T> stream = em.createQuery(criteria)
                    .setFirstResult(page.number() * page.size())
                    .setMaxResults(page.size())
                    .getResultStream();
            // create page result
            return new PageResult<T>(count, stream, page);
        } catch (Exception ex) {
            String entityClass = criteria.getResultType() != null ? criteria.getResultType().getName() : null;
            throw new DAOException(Errors.GET_PAGE_RESULT_ERROR, ex, page.number(), page.size(), entityClass);
        }
    }

    /**
     * Gets the current page.
     *
     * @return the current page.
     */
    public Page getPage() {
        return page;
    }

    /**
     * Gets the search count criteria.
     *
     * @return the search count criteria.
     */
    public CriteriaQuery<Long> countCriteria() {
        return countCriteria;
    }

    /**
     * Move to the previous page.
     *
     * @return the page query.
     */
    public PagedQuery<T> previous() {
        if (page.number() > 0) {
            page = Page.of(page.number() - 1, page.size());
        }
        return this;
    }

    /**
     * Move to the next page.
     *
     * @return the page query.
     */
    public PagedQuery<T> next() {
        page = Page.of(page.number() + 1, page.size());
        return this;
    }

    /**
     * Internal error code.
     */
    public enum Errors {

        /**
         * Gets the page result error.
         */
        GET_PAGE_RESULT_ERROR;
    }

    /**
     * Create a row count CriteriaQuery from a CriteriaQuery
     *
     * @param em       entity manager
     * @param criteria source criteria
     * @param <T>      the entity type.
     * @return row count CriteriaQuery
     */
    public static <T> CriteriaQuery<Long> createCountCriteria(EntityManager em, CriteriaQuery<T> criteria) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = createCountCriteriaQuery(builder, criteria, false);
        Expression<Long> countExpression;
        if (criteria.isDistinct()) {
            countExpression = builder.countDistinct(findRoot(countCriteria, criteria.getResultType()));
        } else {
            countExpression = builder.count(findRoot(countCriteria, criteria.getResultType()));
        }
        return countCriteria.select(countExpression);
    }

    /**
     * Creates count criteria query base on the {@code from} criteria query..
     *
     * @param builder the criteria builder.
     * @param from    source Criteria.
     * @param fetches copy fetches queries.
     * @return count criteria query.
     */
    public static CriteriaQuery<Long> createCountCriteriaQuery(CriteriaBuilder builder, CriteriaQuery<?> from, boolean fetches) {
        CriteriaQuery<Long> result = builder.createQuery(Long.class);

        // copy the roots and they joins and fetches
        from.getRoots().forEach(root -> {
            Root<?> dest = result.from(root.getJavaType());
            dest.alias(createAlias(root));
            copyJoins(root, dest);
            if (fetches) {
                copyFetches(root, dest);
            }
        });

        // add the group by
        result.groupBy(from.getGroupList());

        // add the distinct
        result.distinct(from.isDistinct());

        // add the group restriction
        if (from.getGroupRestriction() != null) {
            result.having(from.getGroupRestriction());
        }

        // add the predicate
        Predicate predicate = from.getRestriction();
        if (predicate != null) {
            result.where(predicate);
        }
        return result;
    }

    /**
     * Find the Root with type class on {@link CriteriaQuery} Root Set for the {@code clazz}.
     *
     * @param query criteria query
     * @param clazz root type
     * @param <T> the type of the root class.
     * @return the root of the criteria query or {@code null} if none
     */
    public static <T> Root<T> findRoot(CriteriaQuery<?> query, Class<T> clazz) {
        for (Root<?> r : query.getRoots()) {
            if (clazz.equals(r.getJavaType())) {
                return (Root<T>) r.as(clazz);
            }
        }
        return null;
    }

    /**
     * Copy Joins
     *
     * @param from source Join
     * @param to   destination Join
     */
    public static void copyJoins(From<?, ?> from, From<?, ?> to) {
        from.getJoins().forEach(join -> {
            Join<?, ?> item = to.join(join.getAttribute().getName(), join.getJoinType());
            item.alias(createAlias(join));
            copyJoins(join, item);
        });
    }

    /**
     * Copy Fetches
     *
     * @param from source From
     * @param to   destination From
     */
    public static void copyFetches(From<?, ?> from, From<?, ?> to) {
        from.getFetches().forEach(fetch -> {
            Fetch<?, ?> item = to.fetch(fetch.getAttribute().getName(), fetch.getJoinType());
            copyFetches(fetch, item);
        });
    }

    /**
     * Copy Fetches
     *
     * @param from source From
     * @param to   destination From
     */
    public static void copyFetches(Fetch<?, ?> from, Fetch<?, ?> to) {
        from.getFetches().forEach(fetch -> {
            Fetch<?, ?> item = to.fetch(fetch.getAttribute().getName(), fetch.getJoinType());
            copyFetches(fetch, item);
        });
    }

    /**
     * Gets The result alias, if none set a default one and return it
     *
     * @param selection the selection
     * @return root alias or generated one
     */
    public static <T> String createAlias(Selection<T> selection) {
        String alias = selection.getAlias();
        if (alias == null) {
            alias = "Alias_" + 1;
            selection.alias(alias);
        }
        return alias;

    }

}
