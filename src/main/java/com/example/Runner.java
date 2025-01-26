package com.example;

import com.example.db.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Runner implements ApplicationRunner, ExitCodeGenerator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicInteger exitCode = new AtomicInteger(0);

    private final EntityManager em;

    public Runner(
            EntityManager em
    ) {
        this.em = em;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {

        var customer = new Customer();
        customer.setFirstName("FirstName");
        customer.setLastName("LastName");
        em.persist(customer);

        var salesOrder1 = new SalesOrder();
        salesOrder1.setCustomer(customer);
        salesOrder1.setStatus(SalesOrderStatus.NEW);
        em.persist(salesOrder1);
        em.persist(SalesOrderHistory.from(salesOrder1));

        var salesOrder2 = new SalesOrder();
        salesOrder2.setCustomer(customer);
        salesOrder2.setStatus(SalesOrderStatus.NEW);
        em.persist(salesOrder2);
        em.persist(SalesOrderHistory.from(salesOrder2));
        salesOrder2.setStatus(SalesOrderStatus.APPROVED);
        em.persist(salesOrder2);
        em.persist(SalesOrderHistory.from(salesOrder2));

        var salesOrder3 = new SalesOrder();
        salesOrder3.setCustomer(customer);
        salesOrder3.setStatus(SalesOrderStatus.NEW);
        em.persist(salesOrder3);
        em.persist(SalesOrderHistory.from(salesOrder3));
        salesOrder3.setStatus(SalesOrderStatus.APPROVED);
        em.persist(salesOrder3);
        em.persist(SalesOrderHistory.from(salesOrder3));
        salesOrder3.setStatus(SalesOrderStatus.PREPARING);
        em.persist(salesOrder3);
        em.persist(SalesOrderHistory.from(salesOrder3));
        salesOrder3.setStatus(SalesOrderStatus.SHIPPED);
        em.persist(salesOrder3);
        em.persist(SalesOrderHistory.from(salesOrder3));

        em.flush();
        em.clear();

        var cb = em.getCriteriaBuilder();

        var query1 = cb.createQuery(SalesOrder.class);
        var so1 = query1.from(SalesOrder.class);
        so1.fetch(SalesOrder_.customer);

        query1.select(so1);

        try (var result = em.createQuery(query1).getResultStream()) {
            result.forEach(so -> {
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        so.getCustomer().getId(), so.getCustomer().getFirstName(), so.getCustomer().getLastName()
                ));
            });
        }

        var query2 = cb.createQuery(SalesOrder.class);
        var so2 = query2.from(SalesOrder.class);
        so2.fetch(SalesOrder_.customer);

        query2.where(
                cb.not(cb.equal(
                        so2.get(SalesOrder_.status),
                        SalesOrderStatus.NEW
                ))
        );

        query2.select(so2);

        try (var result = em.createQuery(query2).getResultStream()) {
            result.forEach(so -> {
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        so.getCustomer().getId(), so.getCustomer().getFirstName(), so.getCustomer().getLastName()
                ));
            });
        }

        var query3 = cb.createTupleQuery();
        var so3 = query3.from(SalesOrder.class);
        var cu3 = so3.join(SalesOrder_.customer, JoinType.LEFT);

        query3.where(
                cb.equal(
                        so3.get(SalesOrder_.status),
                        SalesOrderStatus.NEW
                )
        );

        query3.multiselect(so3, cu3);

        try (var result = em.createQuery(query3).getResultStream()) {
            result.forEach(tuple -> {
                var so = tuple.get(so3);
                var cust = tuple.get(cu3);
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        cust.getId(), cust.getFirstName(), cust.getLastName()
                ));
            });
        }

        var query4 = cb.createTupleQuery();
        var so4 = query4.from(SalesOrder.class);
        var cu4 = so4.join(SalesOrder_.customer, JoinType.LEFT);

        query4.where(
                cb.equal(
                        so4.get(SalesOrder_.status),
                        SalesOrderStatus.NEW
                )
        );

        query4.multiselect(
                so4.get(SalesOrder_.id),
                so4.get(SalesOrder_.status),
                cu4.get(Customer_.id),
                cu4.get(Customer_.firstName),
                cu4.get(Customer_.lastName)
        );

        try (var result = em.createQuery(query4).getResultStream()) {
            result.forEach(tuple -> {
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        tuple.get(so4.get(SalesOrder_.id)),
                        tuple.get(so4.get(SalesOrder_.status)),
                        tuple.get(cu4.get(Customer_.id)),
                        tuple.get(cu4.get(Customer_.firstName)),
                        tuple.get(cu4.get(Customer_.lastName))
                ));
            });
        }

        var query5 = cb.createQuery(SalesOrderHistory.class);
        var soh5 = query5.from(SalesOrderHistory.class);
        soh5.fetch(SalesOrderHistory_.customer, JoinType.LEFT);

        var subquery5 = query5.subquery(SalesOrderHistory.class);
        var soh5a = subquery5.from(SalesOrderHistory.class);
        query5.where(
                cb.not(cb.exists(subquery5.where(
                        cb.equal(soh5a.get(SalesOrderHistory_.id), soh5.get(SalesOrderHistory_.id)),
                        cb.greaterThan(soh5a.get(SalesOrderHistory_.seq), soh5.get(SalesOrderHistory_.seq))
                )))
        );

        try (var result = em.createQuery(query5).getResultStream()) {
            result.forEach(soh -> {
                logger.info("SalesOrderHistory: %d %d %s %s, Customer: %d %s %s".formatted(
                        soh.getSeq(), soh.getId(), soh.getStatus(), soh.getCreatedAt(),
                        soh.getCustomer().getId(), soh.getCustomer().getFirstName(), soh.getCustomer().getLastName()
                ));
            });
        }

        setExitCode(0);
    }

    private void setExitCode(int code) {
        exitCode.set(code);
        latch.countDown();
    }

    @Override
    public int getExitCode() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            // 何もしない
        }
        return exitCode.get();
    }
}
