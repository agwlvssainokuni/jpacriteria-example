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

package com.example.jpa;

import com.example.db.entity.SalesOrder;
import com.example.db.entity.SalesOrderItem;
import com.example.db.entity.SalesOrderItem_;
import com.example.db.entity.SalesOrder_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class JpaOtherUsageExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public JpaOtherUsageExample(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {
        logger.info("5. その他の書き方");
        example01();
        example02();
        example03();
        example04();
        example05();
    }

    private void example01() {
        logger.info("5.1 GROUP BY句(カラム)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrderItem> soi = query.from(SalesOrderItem.class);

        // GROUP BY句
        query.groupBy(soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id));

        Expression<BigDecimal> amount = cb.sum(
                cb.prod(
                        soi.get(SalesOrderItem_.unitPrice),
                        soi.get(SalesOrderItem_.quantity).as(BigDecimal.class)
                )
        );

        query.multiselect(
                soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id),
                amount
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {}, amount: {}",
                    r.get(soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id)),
                    r.get(amount)
            );
        }
    }

    private void example02() {
        logger.info("5.2 GROUP BY句(エンティティ)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrderItem> soi = query.from(SalesOrderItem.class);
        Join<SalesOrderItem, SalesOrder> so = soi.join(SalesOrderItem_.salesOrder);

        // GROUP BY句
        query.groupBy(so);

        Expression<BigDecimal> amount = cb.sum(
                cb.prod(
                        soi.get(SalesOrderItem_.unitPrice),
                        soi.get(SalesOrderItem_.quantity).as(BigDecimal.class)
                )
        );

        query.multiselect(
                soi.get(SalesOrderItem_.salesOrder),
                amount
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, amount: {}",
                    r.get(soi.get(SalesOrderItem_.salesOrder)).getId(),
                    r.get(soi.get(SalesOrderItem_.salesOrder)).getStatus(),
                    r.get(amount)
            );
        }
    }

    private void example03() {
        logger.info("5.3 HAVING句");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrderItem> soi = query.from(SalesOrderItem.class);
        Join<SalesOrderItem, SalesOrder> so = soi.join(SalesOrderItem_.salesOrder);

        // GROUP BY句
        query.groupBy(so);

        Expression<BigDecimal> amount = cb.sum(
                cb.prod(
                        soi.get(SalesOrderItem_.unitPrice),
                        soi.get(SalesOrderItem_.quantity).as(BigDecimal.class)
                )
        );

        // HAVING句
        query.having(
                cb.ge(
                        amount,
                        cb.literal(30_000).as(BigDecimal.class)
                )
        );

        query.multiselect(
                soi.get(SalesOrderItem_.salesOrder),
                amount
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, amount: {}",
                    r.get(soi.get(SalesOrderItem_.salesOrder)).getId(),
                    r.get(soi.get(SalesOrderItem_.salesOrder)).getStatus(),
                    r.get(amount)
            );
        }
    }

    private void example04() {
        logger.info("5.4 ORDER BY句");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrderItem> soi = query.from(SalesOrderItem.class);
        Join<SalesOrderItem, SalesOrder> so = soi.join(SalesOrderItem_.salesOrder);

        // GROUP BY句
        query.groupBy(so);

        Expression<BigDecimal> amount = cb.sum(
                cb.prod(
                        soi.get(SalesOrderItem_.unitPrice),
                        soi.get(SalesOrderItem_.quantity).as(BigDecimal.class)
                )
        );

        // ORDER BY句
        query.orderBy(
                cb.desc(amount),
                cb.desc(soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id))
        );

        query.multiselect(
                soi.get(SalesOrderItem_.salesOrder),
                amount
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, amount: {}",
                    r.get(soi.get(SalesOrderItem_.salesOrder)).getId(),
                    r.get(soi.get(SalesOrderItem_.salesOrder)).getStatus(),
                    r.get(amount)
            );
        }
    }

    private void example05() {
        logger.info("5.5 SELECT FOR UPDATE");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }
}
