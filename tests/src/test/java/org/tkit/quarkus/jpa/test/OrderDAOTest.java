package org.tkit.quarkus.jpa.test;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.daos.PagedQuery;
import org.tkit.quarkus.jpa.models.AbstractBusinessTraceableEntity;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@QuarkusTest
@DisplayName("Order DAO tests")
public class OrderDAOTest {

    private static Logger log = LoggerFactory.getLogger(OrderDAOTest.class);

    @Inject
    OrderDAO orderDAO;

    @Test
    public void findByBIDTest() {
        // create 150 users
        orderDAO.create(Stream.generate(OrderTestBuilder::createOrder).limit(1));

        Stream<Order> orders = orderDAO.findAll();
        Optional<Order> toSearch = orders.findFirst();

        Order found = orderDAO.findByBID(toSearch.get().getBid());

        log.info("{}", "order found BID: " + found.getBid() + "; Guid" + found.getId() + "; titel: " + found.getOrderTitle());
           
    }



    public static class OrderTestBuilder {

        public static  Order createOrder() {
            Order order = new Order();
            order.setOrderTitle("My New Order With BID");
            return order;
        }


    }
}
