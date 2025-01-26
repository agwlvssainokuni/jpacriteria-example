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

        var salesOrder2 = new SalesOrder();
        salesOrder2.setCustomer(customer);
        salesOrder2.setStatus(SalesOrderStatus.APPROVED);
        em.persist(salesOrder2);

        em.flush();
        em.clear();

        var cb = em.getCriteriaBuilder();

        var criteria1 = cb.createQuery(SalesOrder.class);
        var root1 = criteria1.from(SalesOrder.class);
        root1.fetch(SalesOrder_.customer);

        criteria1.select(root1);

        try (var result = em.createQuery(criteria1).getResultStream()) {
            result.forEach(so -> {
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        so.getCustomer().getId(), so.getCustomer().getFirstName(), so.getCustomer().getLastName()
                ));
            });
        }

        var criteria2 = cb.createQuery(SalesOrder.class);
        var root2 = criteria2.from(SalesOrder.class);
        root2.fetch(SalesOrder_.customer);

        criteria2.where(
                cb.not(cb.equal(
                        root2.get(SalesOrder_.status),
                        SalesOrderStatus.NEW
                ))
        );

        criteria2.select(root2);

        try (var result = em.createQuery(criteria2).getResultStream()) {
            result.forEach(so -> {
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        so.getCustomer().getId(), so.getCustomer().getFirstName(), so.getCustomer().getLastName()
                ));
            });
        }

        var criteria3 = cb.createTupleQuery();
        var root3 = criteria3.from(SalesOrder.class);
        var join3 = root3.join(SalesOrder_.customer, JoinType.LEFT);

        criteria3.where(
                cb.equal(
                        root3.get(SalesOrder_.status),
                        SalesOrderStatus.NEW
                )
        );

        criteria3.multiselect(root3, join3);

        try (var result = em.createQuery(criteria3).getResultStream()) {
            result.forEach(tuple -> {
                var so = tuple.get(root3);
                var cust = tuple.get(join3);
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        cust.getId(), cust.getFirstName(), cust.getLastName()
                ));
            });
        }

        var criteria4 = cb.createTupleQuery();
        var root4 = criteria4.from(SalesOrder.class);
        var join4 = root4.join(SalesOrder_.customer, JoinType.LEFT);

        criteria4.where(
                cb.equal(
                        root4.get(SalesOrder_.status),
                        SalesOrderStatus.NEW
                )
        );

        criteria4.multiselect(
                root4.get(SalesOrder_.id),
                root4.get(SalesOrder_.status),
                join4.get(Customer_.id),
                join4.get(Customer_.firstName),
                join4.get(Customer_.lastName)
        );

        try (var result = em.createQuery(criteria4).getResultStream()) {
            result.forEach(tuple -> {
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        tuple.get(root4.get(SalesOrder_.id)),
                        tuple.get(root4.get(SalesOrder_.status)),
                        tuple.get(join4.get(Customer_.id)),
                        tuple.get(join4.get(Customer_.firstName)),
                        tuple.get(join4.get(Customer_.lastName))
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
