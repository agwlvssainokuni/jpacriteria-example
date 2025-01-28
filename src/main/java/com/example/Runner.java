package com.example;

import com.example.db.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Selection;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.TemporalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.LongStream;

@Component
public class Runner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public Runner(
            EntityManager em
    ) {
        this.em = em;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        prepare();
        example001();
        example002();
        example003();
        example004();
        example005();
        example006();
        example007();
        example008();
    }

    private void prepare() {

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

        var salesOrder4 = new SalesOrder();
        salesOrder4.setCustomer(customer);
        salesOrder4.setStatus(SalesOrderStatus.NEW);
        em.persist(salesOrder4);
        em.persist(SalesOrderHistory.from(salesOrder4));
        var salesOrderItem41 = new SalesOrderItem();
        salesOrderItem41.setSalesOrder(salesOrder4);
        salesOrderItem41.setUnitPrice(BigDecimal.valueOf(98765));
        salesOrderItem41.setQuantity(9L);
        em.persist(salesOrderItem41);
        var salesOrderItem42 = new SalesOrderItem();
        salesOrderItem42.setSalesOrder(salesOrder4);
        salesOrderItem42.setUnitPrice(BigDecimal.valueOf(43210));
        salesOrderItem42.setQuantity(23L);
        em.persist(salesOrderItem42);

        em.flush();
        em.clear();
    }

    private void example001() {
        logger.info("Example001 単純な全件検索");

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
    }

    private void example002() {
        logger.info("Example002 条件を指定して検索");

        var cb = em.getCriteriaBuilder();

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
    }

    private void example003() {
        logger.info("Example003 JOINしてEntityをmultiselect");

        var cb = em.getCriteriaBuilder();

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
    }

    private void example004() {
        logger.info("Example004 JOINして各カラムをmultiselect");

        var cb = em.getCriteriaBuilder();

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
    }

    private void example005() {
        logger.info("Example005 履歴テーブルから各組の最新を取得(NOT EXISTS)");

        var cb = em.getCriteriaBuilder();

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
    }

    private void example006() {
        logger.info("Example006 GROUP BYして集約関数(SUM)");

        var cb = em.getCriteriaBuilder();

        var query6 = cb.createTupleQuery();
        var so6 = query6.from(SalesOrder.class);
        var soi6 = so6.join(SalesOrder_.item, JoinType.LEFT);
        query6.groupBy(so6);

        var amountExpr = cb.sum(
                cb.prod(soi6.get(SalesOrderItem_.unitPrice), soi6.get(SalesOrderItem_.quantity))
        );
        query6.multiselect(so6, amountExpr);

        try (var result = em.createQuery(query6).getResultStream()) {
            result.forEach(tuple -> {
                var so = tuple.get(so6);
                var amount = tuple.get(amountExpr);
                logger.info("SalesOrder: %d %s, amount: %s".formatted(
                        so.getId(), so.getStatus(),
                        amount
                ));
            });
        }
    }

    public void example007() {
        logger.info("Example007 SQL関数");

        var cb = em.getCriteriaBuilder();

        var query7 = cb.createTupleQuery();
        var max = 120L;
        var selection = LongStream.range(0, max + 1L).mapToObj(rad ->
                cb.function("cos", Number.class,
                        cb.prod(
                                cb.quot(
                                        cb.literal((double) rad),
                                        cb.literal((double) max)
                                ),
                                cb.prod(
                                        cb.function("pi", Number.class),
                                        cb.literal(2)
                                )
                        )
                )
        ).toList();
        query7.multiselect(selection.toArray(Selection[]::new));

        var result = em.createQuery(query7).getSingleResult();
        selection.stream()
                .map(result::get).map(Number::toString)
                .forEach(logger::info);
    }

    public void example008() {
        logger.info("Example008 日時関数");

        var cb = em.getCriteriaBuilder();
        var hcb = Optional.of(cb)
                .filter(HibernateCriteriaBuilder.class::isInstance).map(HibernateCriteriaBuilder.class::cast)
                .get();

        var query8 = cb.createTupleQuery();
        var selection = LongStream.range(-12, 13).mapToObj(duration ->
                hcb.addDuration(
                        hcb.currentTimestamp().as(LocalDateTime.class),
                        hcb.duration(duration, TemporalUnit.MONTH)
                )
        ).toList();
        query8.multiselect(selection.toArray(Selection[]::new));

        var result = em.createQuery(query8).getSingleResult();
        selection.stream()
                .map(result::get).map(LocalDateTime::toString)
                .forEach(logger::info);
    }
}
