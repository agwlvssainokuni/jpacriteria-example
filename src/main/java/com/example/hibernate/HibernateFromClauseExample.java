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

package com.example.hibernate;

import com.example.db.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.query.criteria.*;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Hibernate固有のFROM句拡張機能の使用例を示すクラス。
 * <p>
 * このクラスでは以下のHibernate拡張機能を実装しています：
 * <ul>
 * <li>関連が定義されていないエンティティ間のJOIN</li>
 * <li>サブクエリをFROM句に指定する（派生テーブル）</li>
 * <li>条件付きJOIN（ON句の使用）</li>
 * <li>複雑な結合条件の組み立て</li>
 * </ul>
 */
@Service
public class HibernateFromClauseExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public HibernateFromClauseExample(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {
        logger.info("7. (Hibernate拡張) FROM句");
        example01();
        example02();
    }

    private void example01() {
        logger.info("7.1 関連が定義されていないエンティティのJOIN");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // 左外部結合
        JpaRoot<SalesOrder> so = query.from(SalesOrder.class);
        JpaEntityJoin<Product> prd = so.join(Product.class, SqmJoinType.LEFT);
        // 結合条件
        JpaSubQuery<SalesOrderItem> subquery = query.subquery(SalesOrderItem.class);
        JpaRoot<SalesOrderItem> soi = subquery.from(SalesOrderItem.class);
        prd.on(
                hcb.exists(
                        subquery.where(
                                hcb.equal(soi.get(SalesOrderItem_.salesOrder), so),
                                hcb.equal(soi.get(SalesOrderItem_.product), prd)
                        )
                )
        );

        query.multiselect(
                so,
                prd
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            var o = r.get(so);
            var p = r.get(prd);
            logger.info("SalesOrder: {} {}, Product: {}",
                    o.getId(), o.getStatus(),
                    p
            );
        }
    }

    private void example02() {
        logger.info("7.2 サブクエリをFROM句に指定する");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // FROM句に入れるサブクエリを組み立てる。
        // Tupleで取得するカラムを個別に指定 (エンティティとしての取得はできない)。
        JpaSubQuery<Tuple> subquery1 = query.subquery(Tuple.class);
        JpaRoot<SalesOrderHistory> soh1 = subquery1.from(SalesOrderHistory.class);
        JpaSubQuery<SalesOrderHistory> subquery2 = subquery1.subquery(SalesOrderHistory.class);
        JpaRoot<SalesOrderHistory> soh2 = subquery2.from(SalesOrderHistory.class);
        subquery1.where(
                hcb.not(hcb.exists(
                        subquery2.where(
                                hcb.equal(soh2.get(SalesOrderHistory_.id), soh1.get(SalesOrderHistory_.id)),
                                hcb.gt(soh2.get(SalesOrderHistory_.seq), soh1.get(SalesOrderHistory_.seq))
                        )
                ))
        );
        // サブクエリから取得するカラムを指定する。(alias必須)
        subquery1.multiselect(
                soh1.get(SalesOrderHistory_.seq).alias("soh_seq"),
                soh1.get(SalesOrderHistory_.id).alias("soh_id"),
                soh1.get(SalesOrderHistory_.status).alias("soh_status"),
                soh1.get(SalesOrderHistory_.createdAt).alias("soh_created_at")
        );

        // FROM句を組み立てる。
        JpaRoot<SalesOrder> so = query.from(SalesOrder.class);
        JpaDerivedRoot<Tuple> soh = query.from(subquery1);
        // WHERE句(結合条件)を組み立てる。
        query.where(
                hcb.equal(so.get(SalesOrder_.id), soh.get("soh_id"))
        );
        // SELECT句を組み立てる。
        query.multiselect(
                so,
                // サブクエリで付与したalias名で参照。
                soh.get("soh_seq"),
                soh.get("soh_id"),
                soh.get("soh_status"),
                soh.get("soh_created_at")
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, SalesOrderHistory: {} {} {} {}",
                    r.get(so).getId(), r.get(so).getStatus(),
                    r.get(soh.get("soh_seq")),
                    r.get(soh.get("soh_id")),
                    r.get(soh.get("soh_status")),
                    r.get(soh.get("soh_created_at"))
            );
        }
    }
}
