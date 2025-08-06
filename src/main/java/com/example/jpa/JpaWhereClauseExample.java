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

/**
 * JPA Criteria APIのWHERE句に関する様々な表現方法を示すクラス。
 * <p>
 * このクラスでは以下のWHERE句の書き方を実装しています：
 * <ul>
 * <li>単一条件と複合条件（AND/OR）</li>
 * <li>LIKE、IN、IS NULL、BETWEEN述語</li>
 * <li>EXISTS/NOT EXISTS述語とサブクエリ</li>
 * <li>履歴テーブルからの最新レコード抽出パターン</li>
 * </ul>
 */
@Service
public class JpaWhereClauseExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public JpaWhereClauseExample(EntityManager em) {
        this.em = em;
    }

    /**
     * JPA Criteria APIのWHERE句に関する様々な表現方法の例を実行します。
     * <p>
     * 以下の例を順次実行します：
     * <ul>
     * <li>単一条件と複合条件（AND/OR組み合わせ）</li>
     * <li>LIKE/NOT LIKE述語によるパターンマッチング</li>
     * <li>IN/NOT IN述語による複数値条件</li>
     * <li>IS NULL/IS NOT NULL述語によるNULL判定</li>
     * <li>BETWEEN述語による範囲条件</li>
     * <li>EXISTS/NOT EXISTS述語とサブクエリ</li>
     * <li>履歴テーブルからの最新レコード抽出パターン</li>
     * </ul>
     */
    @Transactional
    public void execute() {
        logger.info("4. WHERE句の書き方");
        example01();
        example02();
        example03();
        example04();
        example05();
        example06();
        example07();
        example08();
        example09();
        example10();
        example11();
        example12();
        example13();
        example14();
        example15();
    }

    private void example01() {
        logger.info("4.1 単一条件");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 単一条件
        query.where(
                cb.equal(
                        so.get(SalesOrder_.status),
                        cb.literal(SalesOrderStatus.NEW)
                )
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    private void example02() {
        logger.info("4.2 複合条件");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 複合条件
        query.where(
                cb.notEqual(
                        so.get(SalesOrder_.status),
                        cb.literal(SalesOrderStatus.NEW)
                ),
                cb.notEqual(
                        so.get(SalesOrder_.status),
                        cb.literal(SalesOrderStatus.SHIPPED)
                )
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    private void example03() {
        logger.info("4.3 複合条件(AND指定)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 複合条件(AND指定)
        query.where(
                cb.and(
                        cb.notEqual(
                                so.get(SalesOrder_.status),
                                cb.literal(SalesOrderStatus.NEW)
                        ),
                        cb.notEqual(
                                so.get(SalesOrder_.status),
                                cb.literal(SalesOrderStatus.SHIPPED)
                        )
                )
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    private void example04() {
        logger.info("4.4 複合条件(OR指定)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 複合条件(OR指定)
        query.where(
                cb.or(
                        cb.equal(
                                so.get(SalesOrder_.status),
                                cb.literal(SalesOrderStatus.NEW)
                        ),
                        cb.equal(
                                so.get(SalesOrder_.status),
                                cb.literal(SalesOrderStatus.SHIPPED)
                        )
                )
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    private void example05() {
        logger.info("4.5 複合条件(ANDとORの組合せ)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 複合条件(ANDとORの組合せ)
        query.where(
                cb.or(
                        cb.and(
                                cb.notEqual(
                                        so.get(SalesOrder_.status),
                                        cb.literal(SalesOrderStatus.APPROVED)
                                ),
                                cb.notEqual(
                                        so.get(SalesOrder_.status),
                                        cb.literal(SalesOrderStatus.PREPARING)
                                )
                        ),
                        cb.and(
                                cb.notEqual(
                                        so.get(SalesOrder_.status),
                                        cb.literal(SalesOrderStatus.NEW)
                                ),
                                cb.notEqual(
                                        so.get(SalesOrder_.status),
                                        cb.literal(SalesOrderStatus.SHIPPED)
                                )
                        )
                )
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    private void example06() {
        logger.info("4.6 LIKE述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> prd = query.from(Product.class);

        // LIKE述語
        query.where(
                cb.like(
                        prd.get(Product_.name),
                        "%OD_01"
                )
        );

        // クエリを発行する。
        List<Product> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("Product: {} {} {}",
                    r.getId(), r.getName(), r.getUnitPrice()
            );
        }
    }

    private void example07() {
        logger.info("4.7 NOT LIKE述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> prd = query.from(Product.class);

        // NOT LIKE述語
        query.where(
                cb.notLike(
                        prd.get(Product_.name),
                        "%OD_01"
                )
        );

        // クエリを発行する。
        List<Product> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("Product: {} {} {}",
                    r.getId(), r.getName(), r.getUnitPrice()
            );
        }
    }

    private void example08() {
        logger.info("4.8 IN述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> prd = query.from(Product.class);

        // IN述語
        query.where(
                prd.get(Product_.name).in(
                        "PROD_01", "PROD_02"
                )
        );

        // クエリを発行する。
        List<Product> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("Product: {} {} {}",
                    r.getId(), r.getName(), r.getUnitPrice()
            );
        }
    }

    private void example09() {
        logger.info("4.9 NOT IN述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> prd = query.from(Product.class);

        // NOT IN述語
        query.where(
                prd.get(Product_.name).in(
                        "PROD_01", "PROD_02"
                ).not()
        );

        // クエリを発行する。
        List<Product> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("Product: {} {} {}",
                    r.getId(), r.getName(), r.getUnitPrice()
            );
        }
    }

    private void example10() {
        logger.info("4.10 IS NULL述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);
        ListJoin<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item, JoinType.LEFT);

        // IS NULLN述語
        query.where(
                soi.isNull()
        );

        query.multiselect(so, soi);

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            var o = r.get(so);
            var i = r.get(soi);
            logger.info("SalesOrder: {} {}, SalesOrderItem: {}",
                    o.getId(), o.getStatus(),
                    i
            );
        }
    }

    private void example11() {
        logger.info("4.11 IS NOT NULL述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);
        ListJoin<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item, JoinType.LEFT);

        // IS NOT NULLN述語
        query.where(
                soi.isNotNull()
        );

        query.multiselect(so, soi);

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

    private void example12() {
        logger.info("4.12 BETWEEN述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // BETWEEN述語
        query.where(
                cb.between(
                        so.get(SalesOrder_.status),
                        cb.literal(SalesOrderStatus.APPROVED),
                        cb.literal(SalesOrderStatus.PREPARING)
                )
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    /**
     * EXISTS述語を使用したサブクエリ存在チェックの例。
     * <p>
     * 関連レコードが存在するかどうかをチェックするためにEXISTS述語を使用します。
     * サブクエリと相関させることで、効率的な存在チェックが可能です。
     */
    private void example13() {
        logger.info("4.13 EXISTS述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // EXISTS述語
        Subquery<SalesOrderItem> subquery = query.subquery(SalesOrderItem.class);
        Root<SalesOrderItem> soi = subquery.from(SalesOrderItem.class);

        query.where(
                cb.exists(
                        subquery.where(
                                cb.equal(
                                        soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id),
                                        so.get(SalesOrder_.id)
                                )
                        )
                )
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    private void example14() {
        logger.info("4.14 NOT EXISTS述語");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrder> query = cb.createQuery(SalesOrder.class);
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // NOT EXISTS述語
        Subquery<SalesOrderItem> subquery = query.subquery(SalesOrderItem.class);
        Root<SalesOrderItem> soi = subquery.from(SalesOrderItem.class);

        query.where(
                cb.exists(
                        subquery.where(
                                cb.equal(
                                        soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id),
                                        so.get(SalesOrder_.id)
                                )
                        )
                ).not()
        );

        // クエリを発行する。
        List<SalesOrder> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}",
                    r.getId(), r.getStatus()
            );
        }
    }

    /**
     * NOT EXISTS述語を使用した履歴テーブルから最新レコード抽出の例。
     * <p>
     * 履歴テーブルから各IDの最新レコード（最大SEQ値）のみを抽出する実用的なパターンです。
     * 「自分より新しいレコードが存在しない」という条件でNOT EXISTSを使用します。
     */
    private void example15() {
        logger.info("4.15 NOT EXISTS用例 - 履歴の各組の中で最新を抽出");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<SalesOrderHistory> query = cb.createQuery(SalesOrderHistory.class);
        Root<SalesOrderHistory> soh = query.from(SalesOrderHistory.class);

        Subquery<SalesOrderHistory> subquery = query.subquery(SalesOrderHistory.class);
        Root<SalesOrderHistory> soh2 = subquery.from(SalesOrderHistory.class);

        query.where(
                cb.exists(
                        subquery.where(
                                cb.equal(
                                        soh2.get(SalesOrderHistory_.id),
                                        soh.get(SalesOrderHistory_.id)
                                ),  // 同じidを持つグループの中で
                                cb.gt(
                                        soh2.get(SalesOrderHistory_.seq),
                                        soh.get(SalesOrderHistory_.seq)
                                )   // 自分よりも後に履歴追加されたものが
                        )
                ).not() // 存在しない
        );

        // クエリを発行する。
        List<SalesOrderHistory> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrderHistory: {} {} {}",
                    r.getSeq(), r.getId(), r.getStatus()
            );
        }
    }
}
