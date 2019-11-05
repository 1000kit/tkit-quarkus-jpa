/*
 * Copyright 2019 1000kit.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tkit.quarkus.jpa.daos;

import org.tkit.quarkus.jpa.exception.ConstraintException;
import org.tkit.quarkus.jpa.exception.ServiceException;
import org.tkit.quarkus.jpa.model.AbstractPersistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.quarkus.jpa.model.AbstractPersistent_;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;

/**
 * The abstract EAO service class using an entity type.
 *
 * @param <T> the entity class.
 */
@Transactional(value = Transactional.TxType.NOT_SUPPORTED, rollbackOn = ServiceException.class)
public abstract class AbstractEntityService<T extends AbstractPersistent> implements EntityService {

    /**
     * The logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractEntityService.class);

    /**
     * The property hint is javax.persistence.fetchgraph.
     * <p>
     * This hint will treat all the specified attributes in the Entity Graph as
     * FetchType.EAGER. Attributes that are not specified are treated as
     * FetchType.LAZY.
     */
    private static final String HINT_LOAD_GRAPH = "javax.persistence.loadgraph";

    /**
     * The entity manager.
     */
    @Inject
    public EntityManager em;

    /**
     * The entity class.
     */
    protected Class<T> entityClass;

    /**
     * The entity name.
     */
    protected String entityName;

    /**
     * The load entity graph.
     */
    private EntityGraph<? super T> loadEntityGraph;

    /**
     * The load all entity graph.
     */
    private EntityGraph<? super T> loadAllEntityGraph;

    /**
     * The criteria builder.
     */
    protected CriteriaBuilder cb;

    /**
     * Initialize the entity service bean.
     */
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        String serviceClass = getClass().getName();
        entityClass = getEntityClass();
        entityName = getEntityName();
        log.info("Initialize the entity service {} for entity {}/{}", serviceClass, entityClass, entityName);

        try {
            loadEntityGraph = (EntityGraph<? super T>) getEntityManager().getEntityGraph(entityName + AbstractPersistent.ENTITY_GRAPH_LOAD_BY_GUID);
        } catch (IllegalArgumentException ex) {
            log.warn("The entity graph '{}{}' is not defined for the entity {}/{}", entityName, AbstractPersistent.ENTITY_GRAPH_LOAD_BY_GUID, entityName, entityClass);
        }
        try {
            loadAllEntityGraph = (EntityGraph<? super T>) getEntityManager().getEntityGraph(entityName + AbstractPersistent.ENTITY_GRAPH_LOAD_ALL);
        } catch (IllegalArgumentException ex) {
            log.warn("The entity graph '{}{}' is not defined for the entity {}/{}", entityName, AbstractPersistent.ENTITY_GRAPH_LOAD_ALL, entityName, entityClass);
        }
        cb = getEntityManager().getCriteriaBuilder();
    }

    /**
     * Gets the entity manager.
     * 
     * @return the entity manager.
     */
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Gets all entities.
     *
     * @return the list of all entities.
     * @throws ServiceException if the method fails.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findAll() throws ServiceException {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            cq.from(entityClass);
            TypedQuery<T> query = getEntityManager().createQuery(cq);
            return query.getResultList();
        } catch (Exception e) {
            throw new ServiceException(Errors.FIND_ALL_ENTITIES_FAILED, e);
        }
    }

    /**
     * Gets the entity by id.
     *
     * @param guid the entity GUID.
     * @return the entity corresponding to the GUID.
     * @throws ServiceException if the method fails.
     */
    @SuppressWarnings("unchecked")
    @Transactional(value = Transactional.TxType.SUPPORTS, rollbackOn = ServiceException.class)
    public T findByGuid(final String guid) throws ServiceException {
        try {
            return getEntityManager().find(entityClass, guid);
        } catch (Exception e) {
            throw new ServiceException(Errors.FIND_ENTITY_BY_ID_FAILED, e);
        }
    }

    /**
     * Updates the entity.
     *
     * @param entity the entity.
     * @return the updated entity.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public T update(T entity) throws ServiceException {
        try {
            T result = getEntityManager().merge(entity);
            getEntityManager().flush();
            return result;
        } catch (Exception e) {
            throw handleConstraint(e, Errors.MERGE_ENTITY_FAILED);
        }
    }

    /**
     * Updates the entities.
     *
     * @param entities the list of entities.
     * @return the list of updated entities.
     * @throws ServiceException if the method fails.
     */
    @SuppressWarnings("unchecked")
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public List<T> update(List<T> entities) throws ServiceException {
        if (entities != null) {
            try {
                final List<T> result = new ArrayList<>(entities.size());
                entities.forEach(e -> result.add(getEntityManager().merge(e)));
                getEntityManager().flush();
                return result;
            } catch (Exception e) {
                throw handleConstraint(e, Errors.MERGE_ENTITY_FAILED);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Creates the entity.
     *
     * @param entity the entity.
     * @return the created entity.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public T create(T entity) throws ServiceException {
        try {
            getEntityManager().persist(entity);
            getEntityManager().flush();
        } catch (Exception e) {
            throw handleConstraint(e, Errors.PERSIST_ENTITY_FAILED);
        }
        return entity;
    }

    /**
     * Creates the entities.
     *
     * @param entities the list of enties.
     * @return list of created entities.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public List<T> create(List<T> entities) throws ServiceException {
        if (entities != null) {
            try {
                entities.forEach(em::persist);
                getEntityManager().flush();
            } catch (Exception e) {
                throw handleConstraint(e, Errors.PERSIST_ENTITY_FAILED);
            }
        }
        return entities;
    }

    /**
     * Performs persist followed by flush.
     *
     * @param entity the entity.
     */
    protected void refresh(T entity) {
        getEntityManager().refresh(entity);
    }

    /**
     * Deletes the entity.
     *
     * @param entity the entity.
     * @return <code>true</code> if the entity was correctly deleted.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public boolean delete(T entity) throws ServiceException {
        try {
            if (entity != null) {
                getEntityManager().remove(entity);
                getEntityManager().flush();
                return true;
            }
            return false;
        } catch (Exception e) {
            throw handleConstraint(e, Errors.DELETE_ENTITY_FAILED);
        }
    }

    /**
     * Performs delete operation on a list of entities. false is returned if one
     * object fails to be deleted.
     *
     * @param entities the list of entities.
     * @return the delete flag.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public boolean deleteAll(List<T> entities) throws ServiceException {
        try {
            if (entities != null && !entities.isEmpty()) {
                entities.forEach(e -> getEntityManager().remove(e));
                getEntityManager().flush();
                return true;
            }
            return false;
        } catch (Exception e) {
            throw handleConstraint(e, Errors.DELETE_ENTITIES_FAILED);
        }
    }

    /**
     * Creates the named query.
     *
     * @param namedQuery the named query.
     * @param parameters the map of parameters.
     * @return the query.
     */
    protected Query createNamedQuery(String namedQuery, Map<String, Object> parameters) {
        Query query = getEntityManager().createNamedQuery(namedQuery);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach(query::setParameter);
        }
        return query;
    }

    /**
     * Lock Entity in EntityManager.
     *
     * @param entity   the entity
     * @param lockMode the lock mode
     */
    protected void lock(T entity, LockModeType lockMode) {
        getEntityManager().lock(entity, lockMode);
    }

    /**
     * Finds the list of object by GUIDs.
     *
     * @param guids the set of GUIDs.
     * @return the corresponding list of entities.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public List<T> findByGuid(List<String> guids) throws ServiceException {
        List<T> result = null;
        if (guids != null && !guids.isEmpty()) {
            try {
                CriteriaQuery<T> cq = criteriaQuery();
                cq.where(cq.from(entityClass).get(AbstractPersistent_.GUID).in(guids));
                result = getEntityManager().createQuery(cq).getResultList();
            } catch (Exception e) {
                throw new ServiceException(Errors.FAILED_TO_GET_ENTITY_BY_GUIDS, e, entityName);
            }
        }
        return result;
    }

    /**
     * Performs delete operation on a list of entities. false is returned if one
     * object fails to be deleted.
     *
     * @param entities the list of entities.
     * @return {@code true} if all entities removed.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public int delete(List<T> entities) throws ServiceException {
        int result = 0;
        try {
            if (entities != null && !entities.isEmpty()) {
                for (int i = 0; i < entities.size(); i++) {
                    if (this.delete(entities.get(i))) {
                        result = result + 1;
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException(Errors.FAILED_TO_DELETE_ENTITY, e, entityName);
        }
        return result;
    }

    /**
     * Performs delete operation on a list of entities. false is returned if one
     * object fails to be deleted.
     *
     * @return {@code true} if all entities removed.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public int deleteAll() throws ServiceException {
        int result = 0;
        try {
            List<T> tmp = findAll();
            if (tmp != null && !tmp.isEmpty()) {
                result = delete(tmp);
            }
        } catch (Exception e) {
            throw new ServiceException(Errors.FAILED_TO_DELETE_ALL, e, entityName);
        }
        return result;
    }

    /**
     * Finds all entities in the corresponding interval.
     *
     * @param from  the from index.
     * @param count the count index.
     * @return the corresponding list of the entities.
     * @throws ServiceException if the method fails.
     */
    public List<T> find(Integer from, Integer count) throws ServiceException {
        try {
            CriteriaQuery<T> cq = criteriaQuery();
            cq.from(entityClass);
            TypedQuery<T> query = getEntityManager().createQuery(cq);
            if (from != null) {
                query.setFirstResult(from);
            }
            if (count != null) {
                if (from != null) {
                    query.setMaxResults(from + count);
                } else {
                    query.setMaxResults(count);
                }
            }
            return query.getResultList();
        } catch (Exception e) {
            throw new ServiceException(Errors.FAILED_TO_GET_ALL_ENTITIES, e, entityName, from, count);
        }
    }

    /**
     * Removes all entities. Check on existence is made.
     *
     * @return the number of deleted entities.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public int deleteQueryAll() throws ServiceException {
        try {
            CriteriaQuery<T> cq = criteriaQuery();
            cq.from(entityClass);
            int result = getEntityManager().createQuery(cq).executeUpdate();
            getEntityManager().flush();
            return result;
        } catch (Exception e) {
            throw handleConstraint(e, Errors.FAILED_TO_DELETE_ALL_QUERY);
        }
    }

    /**
     * Removes an entity by GUID. Check on existence is made.
     *
     * @param guid the GUID of the entity
     * @return true if removed.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public boolean deleteByGuid(String guid) throws ServiceException {
        if (guid != null) {
            try {
                CriteriaDelete<T> cq = deleteQuery();
                cq.where(
                        getEntityManager().getCriteriaBuilder()
                                .equal(cq.from(entityClass).get(AbstractPersistent_.GUID), guid)
                );
                int count = getEntityManager().createQuery(cq).executeUpdate();
                getEntityManager().flush();
                return count == 1;
            } catch (Exception e) {
                throw handleConstraint(e, Errors.FAILED_TO_DELETE_BY_GUID_QUERY);
            }
        }
        return false;
    }

    /**
     * Removes entities by GUIDs. Check on existence is made.
     *
     * @param guids the set of GUIDs.
     * @return the number of deleted entities.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public int deleteByGuid(List<String> guids) throws ServiceException {
        try {
            if (guids != null && !guids.isEmpty()) {
                CriteriaDelete<T> cq = deleteQuery();
                cq.where(cq.from(entityClass).get(AbstractPersistent_.GUID).in(guids));
                int result = getEntityManager().createQuery(cq).executeUpdate();
                getEntityManager().flush();
                return result;
            }
        } catch (Exception e) {
            throw handleConstraint(e, Errors.FAILED_TO_DELETE_ALL_BY_GUIDS_QUERY);
        }
        return 0;
    }

    /**
     * Loads all entities.
     *
     * @return the list loaded entities.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public List<T> loadAll() throws ServiceException {
        return loadAll(loadAllEntityGraph);
    }

    /**
     * Loads all entities.
     *
     * @param entityGraph the entity graph.
     * @return the list loaded entities.
     * @throws ServiceException if the method fails.
     */
    protected List<T> loadAll(EntityGraph<?> entityGraph) throws ServiceException {
        try {
            CriteriaQuery<T> cq = criteriaQuery();
            cq.from(entityClass);
            cq.distinct(true);
            TypedQuery<T> query = getEntityManager().createQuery(cq);
            if (entityGraph != null) {
                query.setHint(HINT_LOAD_GRAPH, entityGraph);
            }
            return query.getResultList();
        } catch (Exception e) {
            throw new ServiceException(Errors.FAILED_TO_LOAD_ALL_ENTITIES, e, entityName, entityGraph == null ? null : entityGraph.getName());
        }
    }

    /**
     * Loads all entities.
     *
     * @param guids the set of GUIDs.
     * @return the list loaded entities.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public List<T> loadByGuid(List<String> guids) throws ServiceException {
        return loadByGuid(guids, loadEntityGraph);
    }

    /**
     * Loads all entities.
     *
     * @param guids       the set of GUIDs.
     * @param entityGraph the entity graph.
     * @return the list loaded entities.
     * @throws ServiceException if the method fails.
     */
    protected List<T> loadByGuid(List<String> guids, EntityGraph<?> entityGraph) throws ServiceException {
        List<T> result = null;
        try {
            if (guids != null && !guids.isEmpty()) {
                CriteriaQuery<T> cq = criteriaQuery();
                cq.where(cq.from(entityClass).get(AbstractPersistent_.GUID).in(guids));
                TypedQuery<T> query = getEntityManager().createQuery(cq);
                if (entityGraph != null) {
                    query.setHint(HINT_LOAD_GRAPH, entityGraph);
                }
                result = query.getResultList();
            }
        } catch (Exception e) {
            throw new ServiceException(Errors.FAILED_TO_LOAD_GUIDS_ENTITIES, e, entityName, entityGraph == null ? null : entityGraph.getName());
        }
        return result;
    }

    /**
     * Loads the entity by GUID.
     *
     * @param guid the GUID.
     * @return the entity.
     * @throws ServiceException if the method fails.
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = ServiceException.class)
    public T loadByGuid(String guid) throws ServiceException {
        return loadByGuid(guid, loadEntityGraph);
    }

    /**
     * Loads the entity by GUID and entity graph name.
     *
     * @param guid        the GUID.
     * @param entityGraph the entity graph.
     * @return the entity.
     * @throws ServiceException if the method fails.
     */
    protected T loadByGuid(String guid, EntityGraph<?> entityGraph) throws ServiceException {
        if (guid != null) {
            try {
                Map<String, Object> properties = new HashMap<>();
                if (entityGraph != null) {
                    properties.put(HINT_LOAD_GRAPH, entityGraph);
                }
                return getEntityManager().find(entityClass, guid, properties);
            } catch (Exception e) {
                throw new ServiceException(Errors.FAILED_TO_LOAD_ENTITY_BY_GUID, e, entityName, guid, entityGraph == null ? null : entityGraph.getName());
            }
        }
        return null;
    }

    /**
     * Handle the JPA constraint exception.
     *
     * @param ex  the exception.
     * @param key the error key.
     * @return the corresponding service exception.
     */
    @SuppressWarnings("squid:S1872")
    protected ServiceException handleConstraint(Exception ex, Enum<?> key) {
        if (ex instanceof ConstraintException) {
            return (ConstraintException) ex;
        }
        if (ex instanceof PersistenceException) {
            PersistenceException e = (PersistenceException) ex;
            if (e.getCause() != null) {

                Throwable providerException = e.getCause();
                // Hibernate constraint violation exception
                if ("org.hibernate.exception.ConstraintViolationException".equals(providerException.getClass().getName())) {

                    // for the org.postgresql.util.PSQLException get the constraints message.
                    String msg = providerException.getMessage();
                    if (providerException.getCause() != null) {
                        msg = providerException.getCause().getMessage();
                        if (msg != null) {
                            msg = msg.replaceAll("\n", "");
                        }
                    }
                    // throw own constraints exception.
                    return new ConstraintException(msg, key, e, entityName);
                }
            }
        }
        return new ServiceException(key, ex, entityName);
    }

    /**
     * Creates the create criteria query.
     * @return the criteria query.
     */
    protected CriteriaQuery<T> criteriaQuery() {
        return this.getEntityManager().getCriteriaBuilder().createQuery(this.entityClass);
    }

    /**
     * Creates the create delete query.
     * @return the delete query.
     */
    protected CriteriaDelete<T> deleteQuery() {
        return getEntityManager().getCriteriaBuilder().createCriteriaDelete(entityClass);
    }

    /**
     * Creates the create update query.
     * @return the update query.
     */
    protected CriteriaUpdate<T> updateQuery() {
        return getEntityManager().getCriteriaBuilder().createCriteriaUpdate(entityClass);
    }


    /**
     * Wildcard the search string with case insensitive {@code true}.
     *
     * @param searchString the search string.
     * @return the corresponding search string.
     * @see #wildcard(java.lang.String, boolean)
     */
    public static String wildcard(final String searchString) {
        return wildcard(searchString, true);
    }

    /**
     * Wildcard the search string. Replace * to % and ? to _
     *
     * @param searchString the search string.
     * @param caseInsensitive the case insensitive flag.
     * @return the corresponding search string.
     */
    public static String wildcard(final String searchString, final boolean caseInsensitive) {
        String result = searchString;
        if (caseInsensitive) {
            result = result.toLowerCase();
        }
        if (searchString.indexOf('*') != -1) {
            result = result.replace('*', '%');
        }
        if (searchString.indexOf('?') != -1) {
            result = result.replace('?', '_');
        }
        return result;
    }

    /**
     * Create an IN clause. If the size of the collection exceeds 1000 items, multiple predicates are created and combined with
     * OR.
     *
     * @param path the path of the parameter
     * @param values the values for the IN clause
     * @param cb the criteria builder for the OR sub-query
     * @return the predicate with the IN clause
     */
    public static Predicate inClause(Expression<?> path, Collection<?> values, CriteriaBuilder cb) {
        Predicate result = path.in(values);
        if (values.size() > 1000) {
            List<Predicate> predicates = new ArrayList<>();
            List<?> valuesList = new ArrayList<>(values);
            while (valuesList.size() > 1000) {
                List<?> subList = valuesList.subList(0, 1000);
                predicates.add(path.in(subList));
                subList.clear();
            }
            predicates.add(path.in(valuesList));
            result = cb.or(predicates.toArray(new Predicate[0]));
        }
        return result;
    }

    /**
     * Create a NOT IN clause. If the size of the collection exceeds 1000 items, multiple predicates are created and combined
     * with AND.
     *
     * @param path the path of the parameter
     * @param values the values for the NOT IN clause
     * @param cb the criteria builder for the AND sub-query
     * @return the predicate with the NOT IN clause
     */
    public static Predicate notInClause(Expression<?> path, Collection<?> values, CriteriaBuilder cb) {
        Predicate result = cb.not(path.in(values));
        if (values.size() > 1000) {
            List<Predicate> predicates = new ArrayList<>();
            List<?> valuesList = new ArrayList<>(values);
            while (valuesList.size() > 1000) {
                List<?> subList = valuesList.subList(0, 1000);
                predicates.add(cb.not(path.in(subList)));
                subList.clear();
            }
            predicates.add(cb.not(path.in(valuesList)));
            result = cb.and(predicates.toArray(new Predicate[0]));
        }
        return result;
    }

    /**
     * Create an IN clause in JPQL. If the size of the collection exceeds 1000 items, multiple queries are created and combined
     * with OR.
     *
     * @param attribute the JPQL attribute
     * @param attributeName the attribute name for the parameter replacement
     * @param values the values for the IN clause
     * @param parameters the parameters to be added from the IN clause
     * @return the query string with the IN clause
     */
    public static String inClause(String attribute, String attributeName, Collection<?> values, Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(attribute).append(" IN (:").append(attributeName).append(")");
        List<?> valuesList = new ArrayList<>(values);
        if (values.size() > 1000) {
            int i = 0;
            while (valuesList.size() > 1000) {
                List<?> subList = valuesList.subList(0, 1000);
                sb.append(" OR ").append(attribute).append(" IN (:").append(attributeName).append(i).append(")");
                parameters.put(attributeName + i, new ArrayList<>(subList));
                subList.clear();
                i++;
            }
        }
        sb.append(")");
        parameters.put(attributeName, valuesList);
        return sb.toString();
    }

    /**
     * Create a NOT IN clause in JPQL. If the size of the collection exceeds 1000 items, multiple queries are created and
     * combined with AND.
     *
     * @param attribute the JPQL attribute
     * @param attributeName the attribute name for the parameter replacement
     * @param values the values for the NOT IN clause
     * @param parameters the parameters to be added from the NOT IN clause
     * @return the query string with the NOT IN clause
     */
    public static String notInClause(String attribute, String attributeName, Collection<?> values, Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(attribute).append(" NOT IN (:").append(attributeName).append(")");
        List<?> valuesList = new ArrayList<>(values);
        if (values.size() > 1000) {
            int i = 0;
            while (valuesList.size() > 1000) {
                List<?> subList = valuesList.subList(0, 1000);
                sb.append(" AND ").append(attribute).append(" NOT IN (:").append(attributeName).append(i).append(")");
                parameters.put(attributeName + i, new ArrayList<>(subList));
                subList.clear();
                i++;
            }
        }
        sb.append(")");
        parameters.put(attributeName, valuesList);
        return sb.toString();
    }

    /**
    /**
     * The error keys.
     */
    private enum Errors {
        FAILED_TO_GET_ALL_ENTITIES,
        FAILED_TO_GET_ENTITY_BY_GUIDS,
        FAILED_TO_DELETE_ALL,
        FAILED_TO_DELETE_ENTITY,
        FAILED_TO_DELETE_ALL_QUERY,
        FAILED_TO_DELETE_BY_GUID_QUERY,
        FAILED_TO_DELETE_ALL_BY_GUIDS_QUERY,
        FAILED_TO_LOAD_ALL_ENTITIES,
        FAILED_TO_LOAD_GUIDS_ENTITIES,
        FAILED_TO_LOAD_ENTITY_BY_GUID,
        PERSIST_ENTITY_FAILED,
        MERGE_ENTITY_FAILED,
        DELETE_ENTITY_FAILED,
        DELETE_ENTITIES_FAILED,
        FIND_ENTITY_BY_ID_FAILED,
        FIND_ALL_ENTITIES_FAILED,
        ;
    }
}
