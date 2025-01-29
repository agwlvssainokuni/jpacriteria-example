/*
 * Copyright 2025 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.example.db.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Selection;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.TemporalUnit;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.LongStream;

@Service
public class SandboxExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public SandboxExample(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {
        example001();
        example002();
        example003();
        example004();
        example005();
        example006();
        example007();
        example008();
        example009();
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
        logger.info("Example008 日時関数 (Hibernate)");

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

    private void example009() {
        logger.info("Example009 プロパティを使わずにJOIN (Hibernate)");

        var cb = em.getCriteriaBuilder();
        var hcb = Optional.of(cb)
                .filter(HibernateCriteriaBuilder.class::isInstance).map(HibernateCriteriaBuilder.class::cast)
                .get();

        var query9 = hcb.createTupleQuery();
        var so9 = query9.from(SalesOrder.class);
        var cu9 = so9.join(Customer.class, SqmJoinType.LEFT);
//        cu9.on(cb.equal(
//                cu9,
//                so9.get(SalesOrder_.customer)
//        ));
        cu9.on(cb.equal(
                cu9.get(Customer_.id),
                so9.get(SalesOrder_.customer).get(Customer_.id)
        ));

        query9.multiselect(so9, cu9);

        try (var result = em.createQuery(query9).getResultStream()) {
            result.forEach(tuple -> {
                var so = tuple.get(so9);
                var cust = tuple.get(cu9);
                logger.info("SalesOrder: %d %s, Customer: %d %s %s".formatted(
                        so.getId(), so.getStatus(),
                        cust.getId(), cust.getFirstName(), cust.getLastName()
                ));
            });
        }
    }
}
