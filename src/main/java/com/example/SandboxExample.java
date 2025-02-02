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

import com.example.db.entity.Customer;
import com.example.db.entity.Customer_;
import com.example.db.entity.SalesOrder;
import com.example.db.entity.SalesOrder_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Selection;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.TemporalUnit;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        example008();
        example009();
        example010();
        example011();
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

    private void example010() {
        logger.info("Example010 WITH RECURSIVE で連番生成 (Hibernate)");

        var cb = em.getCriteriaBuilder();
        var hcb = Optional.of(cb)
                .filter(HibernateCriteriaBuilder.class::isInstance).map(HibernateCriteriaBuilder.class::cast)
                .get();

        var query = hcb.createTupleQuery();

        var seqFrom = 1;
        var seqTo = 10;
        var seqStep = 1;
        var seqCol = "n";
        var seqCte = query.withRecursiveUnionAll(
                hcb.createTupleQuery().multiselect(
                        hcb.literal(seqFrom).alias(seqCol)
                ),
                (cteCriteria) -> {
                    var q = hcb.createTupleQuery();
                    var cteRoot = q.from(cteCriteria);
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

        var root = query.from(seqCte);
        var seqExpr = root.get(seqCol).asInteger();
        query.multiselect(seqExpr);

        try (var result = em.createQuery(query).getResultStream()) {
            result.forEach(tuple -> {
                logger.info("seq: {}", tuple.get(seqExpr));
            });
        }
    }

    private void example011() {
        logger.info("Example011 WITH RECURSIVE で連番生成して日付計算 (Hibernate)");

        var cb = em.getCriteriaBuilder();
        var hcb = Optional.of(cb)
                .filter(HibernateCriteriaBuilder.class::isInstance).map(HibernateCriteriaBuilder.class::cast)
                .get();

        var query = hcb.createTupleQuery();

        var seqFrom = -12;
        var seqTo = 12;
        var seqStep = 1;
        var seqCol = "n";
        var seqCte = query.withRecursiveUnionAll(
                hcb.createTupleQuery().multiselect(
                        hcb.literal(seqFrom).alias(seqCol)
                ),
                (cteCriteria) -> {
                    var q = hcb.createTupleQuery();
                    var cteRoot = q.from(cteCriteria);
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

        var root = query.from(seqCte);
        var seqExpr = root.get(seqCol).asInteger();
        var date = hcb.addDuration(
                hcb.currentDate().as(LocalDate.class),
                hcb.durationScaled(root.get(seqCol), hcb.duration(1L, TemporalUnit.MONTH))
        );
        query.multiselect(
                seqExpr,
                date
        );

        try (var result = em.createQuery(query).getResultStream()) {
            result.forEach(tuple -> {
                logger.info("seq: {} {}", tuple.get(seqExpr), tuple.get(date));
            });
        }
    }
}
