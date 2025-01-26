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

    private final EntityManager entityManager;

    public Runner(
            EntityManager entityManager
    ) {
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {

        var c = new Customer();
        c.setFirstName("FirstName");
        c.setLastName("LastName");
        entityManager.persist(c);

        var s1 = new SalesOrder();
        s1.setCustomer(c);
        s1.setStatus(SalesOrderStatus.NEW);
        entityManager.persist(s1);

        var s2 = new SalesOrder();
        s2.setCustomer(c);
        s2.setStatus(SalesOrderStatus.APPROVED);
        entityManager.persist(s2);

        entityManager.flush();
        entityManager.clear();

        var builder = entityManager.getCriteriaBuilder();
        var criteria1 = builder.createQuery(Customer.class);

        var root1 = criteria1.from(Customer.class);
        root1.fetch(Customer_.salesOrder, JoinType.LEFT);
        criteria1.select(root1);

        try (var result1 = entityManager.createQuery(criteria1).getResultStream()) {
            result1.forEach(cust -> {
                logger.info("Customer: %d %s %s".formatted(cust.getId(), cust.getFirstName(), cust.getLastName()));
                cust.getSalesOrder().forEach(order -> {
                    logger.info("    SalesOrder: %d %s".formatted(order.getId(), order.getStatus()));
                });
            });
        }

        var criteria2 = builder.createQuery(SalesOrder.class);
        var root2 = criteria2.from(SalesOrder.class);
        root2.fetch(SalesOrder_.customer);

        criteria2.select(root2);
        criteria2.where(
                builder.equal(
                        root2.get(SalesOrder_.status),
                        SalesOrderStatus.APPROVED
                ).not()
        );

        try (var result2 = entityManager.createQuery(criteria2).getResultStream()) {
            result2.forEach(so -> {
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        so.getCustomer().getId(), so.getCustomer().getFirstName(), so.getCustomer().getLastName()
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
