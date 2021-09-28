package org.tkit.quarkus.jpa.test;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.daos.PagedQuery;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import javax.inject.Inject;
import javax.transaction.Transactional;
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

    private final String userIdForDeleting = "123456789";

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

    @Test
    @Transactional
    public void deleteAllUsersTest(){
        User user = UserTestBuilder.createUser();
        userDAO.create(user);
        User foundUser1 = userDAO.findById(user.getId());
        Assertions.assertNotNull(foundUser1);
        userDAO.deleteAll();
        User foundUser2 = userDAO.findById(user.getId());
        Assertions.assertNull(foundUser2);
    }

    @Test
    public void deleteByIdTest(){
        User user = UserTestBuilder.createUser();
        user.setId(userIdForDeleting);
        userDAO.create(user);
        User foundUser1 = userDAO.findById(user.getId());
        Assertions.assertNotNull(foundUser1);
        boolean deleted = userDAO.deleteQueryById(foundUser1.getId());
        Assertions.assertTrue(deleted);
    }


    @Test
    @Transactional
    public void deleteEntityTest(){
        User user = UserTestBuilder.createUser();
        userDAO.create(user);
        User foundUser1 = userDAO.findById(user.getId());
        Assertions.assertNotNull(foundUser1);
        userDAO.delete(foundUser1);
        User foundUser2 = userDAO.findById(user.getId());
        Assertions.assertNull(foundUser2);
    }

    @Test
    public void updateEntitiesTest(){
        User user1 = UserTestBuilder.createUser();
        User user2 = UserTestBuilder.createUser();
        userDAO.create(user1);
        userDAO.create(user2);
        user1.setEmail("email1@test.com");
        user2.setEmail("email2@test.com");
        List<User> users = List.of(user1, user2);
        userDAO.update(users);
        User foundUser1 = userDAO.findById(user1.getId());
        User foundUser2 = userDAO.findById(user2.getId());
        Assertions.assertEquals(foundUser1.getEmail(), user1.getEmail());
        Assertions.assertEquals(foundUser2.getEmail(), user2.getEmail());
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
