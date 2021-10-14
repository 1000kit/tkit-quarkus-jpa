# tkit-quarkus-jpa

1000kit Quarkus JPA extension

[![License](https://img.shields.io/badge/license-Apache--2.0-green?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.tkit.quarkus/tkit-quarkus-jpa?logo=java&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/org.tkit.quarkus/tkit-quarkus-jpa)

> The version 1.0.0+ contains new model and DAO. For old version please use
> branch [0.7](https://gitlab.com/1000kit/libs/quarkus/tkit-quarkus-jpa/-/tree/0.7)

## Documentation

Example project with this extension is in the [1000kit JPA guides](https://1000kit.gitlab.io/guides/docs/quarkus/quarkus-jpa-project/)

This extension contains abstract classes for the `JPA Entity` and for `DAO`.
The main class of this extension is `AbstractDAO<T>` class which implements basic `CRUD` operation.

## Entity

We have these abstract classes for `Entity`
* `org.tkit.quarkus.jpa.models.TraceableEntity` - base `Entity` abstract class which implements 
traceable fields `creationUser`,`creationDate`,`modificationDate` and `modificationUser`. The type of the `ID` field is `String`.
The `ID` is generated when you create java instance with `UUID.randomUUID().toString()`
* `org.tkit.quarkus.jpa.models.BusinessTraceableEntity` - this business entity class implements all traceable fields and the type
of the `ID` is `Long`. For this entity we muss define the ID generator
```java
@Table(name = "TABLE_NAME", indexes = @Index(name = "TABLE_NAME_GUID_IDX", columnList = "GUID", unique = true))
@SequenceGenerator(name = "GEN_TABLE_NAME", sequenceName = "SEQ_TABLE_NAME_BID", allocationSize = 1, initialValue = 1)
```  

In the project you need to extend `Entities` from one of these abstract classes.

## DAO

The `AbstractDAO<T>` represent `DAO pattern` which implements `CRUD` operation.
```java
@ApplicationScoped
public class UserDAO extends AbstractDAO<User> {
    
}
```
The operation `create`,`delete`,`update` and `findById` are implemented in the abstract class.
In your `DAO` class you need to implement only the business logic.

## Exception

All method of the `AbstractDAO<T>` class throws `DAOException` which is `RuntimeException` and has enumerated `ErrorCode`.
These errors are defined in the `AbstractDAO` class. 

The `ConstraintException` extends from the `DAOException` and is use for database constraints.
For example the `create` or `update` operation can throw this exception.

## PageQuery

The `AbstractDAO` class implements the `PageQuery`. With the method `PagedQuery<T> createPageQuery(CriteriaQuery<T> query, Page page)`
could you create a `PageQuery` for you entity. The method `getPageResult` of the `PageQuery` return `PageResult` which contains:
* stream - stream of entities.
* totalElements - total elements in the database for your criteria.
* number - the page number
* size - size of the page
* totalPages - total pages

Examaple method:
```java
public PageResult<User> searchByCriteria(UserSearchCriteria criteria) {
    if (criteria == null) {
        return null;
    }
    CriteriaQuery<User> cq = criteriaQuery();
    Root<User> root = cq.from(User.class);
    List<Predicate> predicates = new ArrayList<>();
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

    if (criteria.getName() != null && !criteria.getName().isEmpty()) {
        predicates.add(cb.like(root.get(User_.USERNAME), wildcard(criteria.getName())));
    }
    if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
        predicates.add(cb.like(root.get(User_.EMAIL), wildcard(criteria.getEmail())));
    }
    if (!predicates.isEmpty()) {
        cq.where(predicates.toArray(new Predicate[0]));
    }

    return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
}
```
In version 2.8.0 default sorting by id attribute was added to avoid a problem with unpredictable data order for paging. 
There could be a situation where some rows are selected from DB more than once, and some rows were skipped. 

## Release

### Create a release

```bash
mvn semver-release:release-create
```

### Create a patch branch
```bash
mvn semver-release:patch-create -DpatchVersion=x.x.0
```
