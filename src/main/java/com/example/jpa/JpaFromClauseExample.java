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

@Service
public class JpaFromClauseExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public JpaFromClauseExample(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {
        logger.info("3. FROM句の書き方");
        example01();
        example02();
        example03();
        example04();
        example05();
        example06();
        example07();
        example08();
        example09();
    }

    private void example01() {
        logger.info("3.1 単一表を指定する");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 取得するBeanを指定。(この場合は省略可能)
        query.select(so);

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    private void example02() {
        logger.info("3.2 (fetch)1:1のエンティティを 1 クエリで取得する");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 1:1のエンティティをfetch指定。
        Fetch<SalesOrder, Customer> cu = so.fetch(SalesOrder_.customer);

        // 取得するBeanを指定。(この場合は省略可能)
        query.select(so);

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, Customer: {} {} {}",
                    r.getId(), r.getStatus(),
                    r.getCustomer().getId(), r.getCustomer().getFirstName(), r.getCustomer().getLastName()
            );
        }
    }

    private void example03() {
        logger.info("3.3 (fetch)1:Nのエンティティを 1 クエリで取得する(内部結合)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 1:Nのエンティティをfetch指定。
        Fetch<SalesOrder, SalesOrderItem> soi = so.fetch(SalesOrder_.item);

        // 取得するBeanを指定。(この場合は省略可能)
        query.select(so);

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
            for (var i : r.getItem()) {
                logger.info("    SalesOrderItem: {} {} {}",
                        i.getId(), i.getUnitPrice(), i.getQuantity()
                );
            }
        }
    }

    private void example04() {
        logger.info("3.4 (fetch)1:Nのエンティティを 1 クエリで取得する(外部結合)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 1:Nのエンティティをfetch指定。
        Fetch<SalesOrder, SalesOrderItem> soi = so.fetch(SalesOrder_.item, JoinType.LEFT);

        // 取得するBeanを指定。(この場合は省略可能)
        query.select(so);

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
            for (var i : r.getItem()) {
                logger.info("    SalesOrderItem: {} {} {}",
                        i.getId(), i.getUnitPrice(), i.getQuantity()
                );
            }
        }
    }

    private void example05() {
        logger.info("3.5 (fetch)1:N-1:1を 1 クエリで取得する(内部結合)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 1:Nのエンティティをfetch指定。
        Fetch<SalesOrder, SalesOrderItem> soi = so.fetch(SalesOrder_.item);
        // 1:1のエンティティをfetch指定。
        Fetch<SalesOrderItem, Product> prd = soi.fetch(SalesOrderItem_.product);

        // 取得するBeanを指定。(この場合は省略可能)
        query.select(so);

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
            for (var i : r.getItem()) {
                logger.info("    SalesOrderItem: {} {} {}, Product: {} {} {}",
                        i.getId(), i.getUnitPrice(), i.getQuantity(),
                        i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getUnitPrice()
                );
            }
        }
    }

    private void example06() {
        logger.info("3.6 (join)1:Nのエンティティを 1 クエリで取得する(内部結合)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 1:Nのエンティティをjoin指定。
        Join<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item);

        // 取得するBeanを指定。
        query.multiselect(
                so,
                soi
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            var o = r.get(so);
            var i = r.get(soi);
            logger.info("SalesOrder: {} {}, SalesOrderItem: {} {} {}",
                    o.getId(), o.getStatus(),
                    i.getId(), i.getUnitPrice(), i.getQuantity()
            );
        }
    }

    private void example07() {
        logger.info("3.7 (join)1:Nのエンティティを 1 クエリで取得する(外部結合)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 1:Nのエンティティをjoin指定。
        Join<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item, JoinType.LEFT);

        // 取得するBeanを指定。
        query.multiselect(
                so,
                soi
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            var o = r.get(so);
            var i = r.get(soi);
            logger.info("SalesOrder: {} {}, SalesOrderItem: {} {} {}",
                    o.getId(), o.getStatus(),
                    Optional.ofNullable(i).map(SalesOrderItem::getId),
                    Optional.ofNullable(i).map(SalesOrderItem::getUnitPrice),
                    Optional.ofNullable(i).map(SalesOrderItem::getQuantity)
            );
        }
    }

    private void example08() {
        logger.info("3.8 (join)1:N-1:1を 1 クエリで取得する(内部結合)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 1:Nのエンティティをjoin指定。
        Join<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item);

        // 1:1のエンティティをjoin指定。
        Join<SalesOrderItem, Product> prd = soi.join(SalesOrderItem_.product);

        // 取得するBeanを指定。
        query.multiselect(
                so,
                soi,
                prd
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            var o = r.get(so);
            var i = r.get(soi);
            var p = r.get(prd);
            logger.info("SalesOrder: {} {}, SalesOrderItem: {} {} {}, Product: {} {} {}",
                    o.getId(), o.getStatus(),
                    i.getId(), i.getUnitPrice(), i.getQuantity(),
                    p.getId(), p.getName(), p.getUnitPrice()
            );
        }
    }

    private void example09() {
        logger.info("3.9 FROM句に複数表を並べてWHERE句で結合条件指定(内部結合)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);
        Root<SalesOrderItem> soi = query.from(SalesOrderItem.class);
        Root<Product> prd = query.from(Product.class);

        // 結合条件を指定する。
        query.where(
                cb.equal(
                        so.get(SalesOrder_.id),
                        soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id)
                ),
                cb.equal(
                        soi.get(SalesOrderItem_.product).get(Product_.id),
                        prd.get(Product_.id)
                )
        );

        // 取得するBeanを指定。
        query.multiselect(
                so,
                soi,
                prd
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            var o = r.get(so);
            var i = r.get(soi);
            var p = r.get(prd);
            logger.info("SalesOrder: {} {}, SalesOrderItem: {} {} {}, Product: {} {} {}",
                    o.getId(), o.getStatus(),
                    i.getId(), i.getUnitPrice(), i.getQuantity(),
                    p.getId(), p.getName(), p.getUnitPrice()
            );
        }
    }
}
