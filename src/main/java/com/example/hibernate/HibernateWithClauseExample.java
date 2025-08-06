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

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.query.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Hibernate固有のWITH句（CTE: Common Table Expression）機能の使用例を示すクラス。
 * <p>
 * このクラスでは以下のHibernate拡張機能を実装しています：
 * <ul>
 * <li>WITH RECURSIVEによる再帰的共通テーブル式</li>
 * <li>連番生成パターン（0-9、0-99、0-999、0-9999）</li>
 * <li>複数のCTEを組み合わせた複雑な計算</li>
 * <li>再帰回数制限を超える大量データ生成</li>
 * </ul>
 */
@Service
public class HibernateWithClauseExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public HibernateWithClauseExample(EntityManager em) {
        this.em = em;
    }

    /**
     * Hibernate固有のWITH句（CTE: Common Table Expression）機能の使用例を実行します。
     * <p>
     * 以下の例を順次実行します：
     * <ul>
     * <li>WITH RECURSIVEによる連番生成（0-9）</li>
     * <li>CTEを複数回参照した連番生成（0-99）</li>
     * <li>さらに複雑な連番生成（0-999）</li>
     * <li>再帰制限を超える大量連番生成（0-9999）</li>
     * </ul>
     */
    @Transactional
    public void execute() {
        logger.info("6. (Hibernate拡張) WITH句");
        example01();
        example02();
        example03();
        example04();
    }

    /**
     * WITH RECURSIVEを使用した基本的な連番生成の例（0-9）。
     * <p>
     * 共通テーブル式（CTE）の再帰機能を使用して0から9までの連番を生成します。
     * withRecursiveUnionAllメソッドを使用してベースケースと再帰ケースを定義します。
     */
    private void example01() {
        logger.info("6.1 WITH RECURSIVEで連番生成 (0-9)");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // WITH句を組み立てる。
        int seqFrom = 0;
        int seqTo = 9;
        int seqStep = 1;
        String seqCol = "seq";
        JpaCteCriteria<Tuple> seqCte = query.withRecursiveUnionAll(
                // SELECT {seqFrom}
                hcb.createTupleQuery().multiselect(
                        hcb.literal(seqFrom).alias(seqCol)
                ),
                // SELECT {seqFrom} + {seqStep} FROM {seqCte} WHERE ({seqFrom} + {seqStep}) BETWEEN {seqFrom} {seqTo}
                (cteCriteria) -> {
                    JpaCriteriaQuery<Tuple> q = hcb.createTupleQuery();
                    JpaRoot<Tuple> cteRoot = q.from(cteCriteria);
                    q.where(hcb.between(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep)),
                            hcb.literal(Math.min(seqFrom, seqTo)),
                            hcb.literal(Math.max(seqFrom, seqTo))
                    ));
                    q.multiselect(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep))
                    );
                    return q;
                }
        );

        // 連番を算出する。
        JpaRoot<Tuple> seqRoot = query.from(seqCte);
        JpaExpression<Integer> seqExpr = seqRoot.get(seqCol).asInteger();

        query.multiselect(seqExpr);
        query.orderBy(hcb.asc(seqExpr));

        // クエリを発行する。
        try (Stream<Tuple> result = em.createQuery(query).getResultStream()) {
            logger.info("連番: {}",
                    result.map(r -> r.get(seqExpr))
                            .map(String::valueOf)
                            .collect(Collectors.joining(" "))
            );
        }
    }

    private void example02() {
        logger.info("6.2 WITH RECURSIVEで連番生成 (0-99)");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // WITH句を組み立てる。
        int seqFrom = 0;
        int seqTo = 9;
        int seqStep = 1;
        String seqCol = "seq";
        JpaCteCriteria<Tuple> seqCte = query.withRecursiveUnionAll(
                // SELECT {seqFrom}
                hcb.createTupleQuery().multiselect(
                        hcb.literal(seqFrom).alias(seqCol)
                ),
                // SELECT {seqFrom} + {seqStep} FROM {seqCte} WHERE ({seqFrom} + {seqStep}) BETWEEN {seqFrom} {seqTo}
                (cteCriteria) -> {
                    JpaCriteriaQuery<Tuple> q = hcb.createTupleQuery();
                    JpaRoot<Tuple> cteRoot = q.from(cteCriteria);
                    q.where(hcb.between(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep)),
                            hcb.literal(Math.min(seqFrom, seqTo)),
                            hcb.literal(Math.max(seqFrom, seqTo))
                    ));
                    q.multiselect(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep))
                    );
                    return q;
                }
        );

        // 連番を算出する。
        JpaRoot<Tuple> seqRoot = query.from(seqCte);
        JpaRoot<Tuple> seqRoot10 = query.from(seqCte);
        JpaExpression<Integer> seqExpr = hcb.sum(
                seqRoot.get(seqCol),
                hcb.prod(
                        seqRoot10.get(seqCol),
                        hcb.literal(10)
                )
        ).asInteger();

        query.multiselect(seqExpr);
        query.orderBy(hcb.asc(seqExpr));

        // クエリを発行する。
        try (Stream<Tuple> result = em.createQuery(query).getResultStream()) {
            logger.info("連番: {}",
                    result.map(r -> r.get(seqExpr))
                            .map(String::valueOf)
                            .collect(Collectors.joining(" "))
            );
        }
    }

    private void example03() {
        logger.info("6.3 WITH RECURSIVEで連番生成 (0-999)");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // WITH句を組み立てる。
        int seqFrom = 0;
        int seqTo = 9;
        int seqStep = 1;
        String seqCol = "seq";
        JpaCteCriteria<Tuple> seqCte = query.withRecursiveUnionAll(
                // SELECT {seqFrom}
                hcb.createTupleQuery().multiselect(
                        hcb.literal(seqFrom).alias(seqCol)
                ),
                // SELECT {seqFrom} + {seqStep} FROM {seqCte} WHERE ({seqFrom} + {seqStep}) BETWEEN {seqFrom} {seqTo}
                (cteCriteria) -> {
                    JpaCriteriaQuery<Tuple> q = hcb.createTupleQuery();
                    JpaRoot<Tuple> cteRoot = q.from(cteCriteria);
                    q.where(hcb.between(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep)),
                            hcb.literal(Math.min(seqFrom, seqTo)),
                            hcb.literal(Math.max(seqFrom, seqTo))
                    ));
                    q.multiselect(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep))
                    );
                    return q;
                }
        );

        // 連番を算出する。
        JpaRoot<Tuple> seqRoot = query.from(seqCte);
        JpaRoot<Tuple> seqRoot10 = query.from(seqCte);
        JpaRoot<Tuple> seqRoot100 = query.from(seqCte);
        JpaExpression<Integer> seqExpr = hcb.sum(
                hcb.sum(
                        seqRoot.get(seqCol),
                        hcb.prod(
                                seqRoot10.get(seqCol),
                                hcb.literal(10)
                        )
                ),
                hcb.prod(
                        seqRoot100.get(seqCol),
                        hcb.literal(100)
                )
        ).asInteger();

        query.multiselect(seqExpr);
        query.orderBy(hcb.asc(seqExpr));

        // クエリを発行する。
        try (Stream<Tuple> result = em.createQuery(query).getResultStream()) {
            logger.info("連番: {}",
                    result.map(r -> r.get(seqExpr))
                            .map(String::valueOf)
                            .collect(Collectors.joining(" "))
            );
        }
    }

    /**
     * WITH RECURSIVEを複数回参照して大量連番生成する例（0-9999）。
     * <p>
     * 同じCTEを複数回参照し、桁を組み合わせることで再帰制限を超える
     * 大量の連番を効率的に生成する高度なパターンです。
     */
    private void example04() {
        // WITH RECURSIVE の再帰回数の上限以上の連番を生成できる。
        logger.info("6.4 WITH RECURSIVEで連番生成 (0-9999)");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // WITH句を組み立てる。
        int seqFrom = 0;
        int seqTo = 9;
        int seqStep = 1;
        String seqCol = "seq";
        JpaCteCriteria<Tuple> seqCte = query.withRecursiveUnionAll(
                // SELECT {seqFrom}
                hcb.createTupleQuery().multiselect(
                        hcb.literal(seqFrom).alias(seqCol)
                ),
                // SELECT {seqFrom} + {seqStep} FROM {seqCte} WHERE ({seqFrom} + {seqStep}) BETWEEN {seqFrom} {seqTo}
                (cteCriteria) -> {
                    JpaCriteriaQuery<Tuple> q = hcb.createTupleQuery();
                    JpaRoot<Tuple> cteRoot = q.from(cteCriteria);
                    q.where(hcb.between(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep)),
                            hcb.literal(Math.min(seqFrom, seqTo)),
                            hcb.literal(Math.max(seqFrom, seqTo))
                    ));
                    q.multiselect(
                            hcb.sum(cteRoot.get(seqCol), hcb.literal(seqStep))
                    );
                    return q;
                }
        );

        // 連番を算出する。
        JpaRoot<Tuple> seqRoot = query.from(seqCte);
        JpaRoot<Tuple> seqRoot10 = query.from(seqCte);
        JpaRoot<Tuple> seqRoot100 = query.from(seqCte);
        JpaRoot<Tuple> seqRoot1000 = query.from(seqCte);
        JpaExpression<Integer> seqExpr = hcb.sum(
                hcb.sum(
                        hcb.sum(
                                seqRoot.get(seqCol),
                                hcb.prod(
                                        seqRoot10.get(seqCol),
                                        hcb.literal(10)
                                )
                        ),
                        hcb.prod(
                                seqRoot100.get(seqCol),
                                hcb.literal(100)
                        )
                ),
                hcb.prod(
                        seqRoot1000.get(seqCol),
                        hcb.literal(1000)
                )
        ).asInteger();

        query.multiselect(seqExpr);
        query.orderBy(hcb.asc(seqExpr));

        // クエリを発行する。
        try (Stream<Tuple> result = em.createQuery(query).getResultStream()) {
            logger.info("連番: {}",
                    result.map(r -> r.get(seqExpr))
                            .map(String::valueOf)
                            .collect(Collectors.joining(" "))
            );
        }
    }
}
