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
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class BasicUsageExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public BasicUsageExample(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {
        logger.info("1. 基本的なAPIの呼出し方");
        example01();
        example02();
        example03();
        example04();
        example05();
        example06();
        example07();
    }

    private void example01() {
        logger.info("1.1 単一のBeanとして取り出す");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Customer> query = cb.createQuery(Customer.class);
        Root<Customer> cu = query.from(Customer.class);

        // 取得するBeanを指定する。
        query.select(cu);

        // クエリを発行する。
        List<Customer> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("Customer: {} {} {}",
                    r.getId(),
                    r.getFirstName(),
                    r.getLastName()
            );
        }
    }

    private void example02() {
        logger.info("1.2 複数のカラムをTupleとして取り出す");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Customer> cu = query.from(Customer.class);

        // 取得するカラムを指定する。
        query.multiselect(
                cu.get(Customer_.id),
                cu.get(Customer_.firstName),
                cu.get(Customer_.lastName)
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("Customer: {} {} {}",
                    r.get(cu.get(Customer_.id)),
                    r.get(cu.get(Customer_.firstName)),
                    r.get(cu.get(Customer_.lastName))
            );
        }
    }

    private void example03() {
        logger.info("1.3 (fetch)1:Nのレコードを一回のクエリで取得する(親駆動)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);
        so.fetch(SalesOrder_.item, JoinType.LEFT);

        // 取得するBeanを指定する。
        query.select(so);

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
            r.getItem().forEach(i -> {
                logger.info("    SalesOrderItem: {} {} {}",
                        i.getId(), i.getUnitPrice(), i.getQuantity()
                );
            });
        }
    }

    private void example04() {
        logger.info("1.4 (fetch)1:Nのレコードを一回のクエリで取得する(子駆動)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrderItem> query = cb.createQuery(SalesOrderItem.class);
        Root<SalesOrderItem> soi = query.from(SalesOrderItem.class);
        soi.fetch(SalesOrderItem_.salesOrder, JoinType.LEFT);

        // 取得するBeanを指定する。
        query.select(soi);

        // クエリを発行する。
        List<SalesOrderItem> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrderItem: {} {} {}, SalesOrder: {} {}",
                    r.getId(), r.getUnitPrice(), r.getQuantity(),
                    r.getSalesOrder().getId(), r.getSalesOrder().getStatus()
            );
        }
    }

    private void example05() {
        logger.info("1.5 (join)複数のBeanをTupleとして取り出す(親駆動)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);
        ListJoin<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item, JoinType.LEFT);

        // 取得するBeanを指定する。
        query.multiselect(
                so,
                soi
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            SalesOrder o = r.get(so);
            Optional<SalesOrderItem> i = Optional.ofNullable(r.get(soi));
            logger.info("SalesOrder: {} {}, SalesOrderItem: {} {} {}",
                    o.getId(), o.getStatus(),
                    i.map(SalesOrderItem::getId).orElse(null),
                    i.map(SalesOrderItem::getUnitPrice).orElse(null),
                    i.map(SalesOrderItem::getQuantity).orElse(null)
            );
        }
    }

    private void example06() {
        logger.info("1.6 (join)複数のBeanをTupleとして取り出す(子駆動)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrderItem> soi = query.from(SalesOrderItem.class);
        Join<SalesOrderItem, SalesOrder> so = soi.join(SalesOrderItem_.salesOrder, JoinType.LEFT);

        // 取得するBeanを指定する。
        query.multiselect(
                soi,
                so
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            SalesOrderItem i = r.get(soi);
            SalesOrder o = i.getSalesOrder();
            logger.info("SalesOrderItem: {} {} {}, SalesOrder: {} {}",
                    i.getId(), i.getUnitPrice(), i.getQuantity(),
                    o.getId(), o.getStatus()
            );
        }
    }

    private void example07() {
        logger.info("1.7 1レコードずつ取得する(カーソル処理)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Customer> query = cb.createQuery(Customer.class);
        Root<Customer> cu = query.from(Customer.class);

        // 取得するBeanを指定する。
        query.select(cu);

        // クエリを発行する。
        try (Stream<Customer> result = em.createQuery(query).getResultStream()) {
            // 最後まで読んだらカーソルをcloseするため try-with-resources で呼出し。
            result.forEach(r -> {
                logger.info("Customer: {} {} {}",
                        r.getId(),
                        r.getFirstName(),
                        r.getLastName()
                );
            });
        }
    }
}
