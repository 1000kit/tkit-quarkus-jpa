package org.tkit.quarkus.jpa.test;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.daos.PagedQuery;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@QuarkusTest
@DisplayName("User DAO tests")
public class UserDAOTest {

    private static Logger log = LoggerFactory.getLogger(UserDAOTest.class);

    @Inject
    UserDAO userDAO;

    @Inject
    AddressDAO addressDAO;

    @Test
    public void userPagingTest() {
        // create 150 users
        userDAO.create(Stream.generate(UserTestBuilder::createUser).limit(150));

        PagedQuery<User> query = userDAO.pageUsers(Page.of(0, 10));

        PageResult<User> page = query.getPageResult();
        log.info("{}", page);
        log.info("{}", page.getStream().map(TraceableEntity::getId).collect(Collectors.toList()));

        query.next().getPageResult();
        page = query.getPageResult();
        log.info("{}", page);
        log.info("{}", page.getStream().map(TraceableEntity::getId).collect(Collectors.toList()));
    }

    @Test
    public void userPagingCriteriaTest() {
        // create 100 users
        Address address = new Address();
        address.setCity("Bratislava");
        address = addressDAO.create(address);

        userDAO.create(UserTestBuilder.createIndexUsers(100, address));

        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setName("Name_1");
        criteria.setEmail("Email_");
        criteria.setCity("Bratislava");
        PagedQuery<User> pages = userDAO.pageUsers(criteria, Page.of(0, 10));

        PageResult<User> page = pages.getPageResult();
        log.info("{}", page);
        log.info("{}", page.getStream().map(User::getName).collect(Collectors.toList()));

        page = pages.next().getPageResult();
        log.info("{}", page);
        log.info("{}", page.getStream().map(User::getName).collect(Collectors.toList()));

        page = pages.next().getPageResult();
        log.info("{}", page);
        log.info("{}", page.getStream().map(User::getName).collect(Collectors.toList()));
    }

    @Test
    public void pageCountSizeTest() {
        // create 150 users
        userDAO.create(Stream.generate(UserTestBuilder::createUser).limit(10));

        PagedQuery<User> query = userDAO.pageUsers(Page.of(100, 100));

        PageResult<User> page = query.getPageResult();
        log.info("{}", page);
        log.info("{}", page.getStream().map(TraceableEntity::getId).collect(Collectors.toList()));
    }

    public static class UserTestBuilder {

        public static  User createUser() {
            User user = new User();
            user.setName("Name_" + UUID.randomUUID().toString());
            user.setEmail("Email_" + UUID.randomUUID().toString());
            return user;
        }

        public static  User createIndexUser(int index) {
            User user = new User();
            user.setName("Name_" + index + "_" + UUID.randomUUID().toString());
            user.setEmail("Email_" + UUID.randomUUID().toString());
            return user;
        }

        public static Stream<User> createIndexUsers(int count, Address address) {
            List<User> tmp = new ArrayList<>();
            for (int i=0; i<100; i++) {
                User u = createIndexUser(i);
                u.setAddress(address);
                tmp.add(u);
            }
            return tmp.stream();
        }
    }
}
