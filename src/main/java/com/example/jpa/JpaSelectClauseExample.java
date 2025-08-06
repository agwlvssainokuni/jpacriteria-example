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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static jakarta.persistence.criteria.CriteriaBuilder.Trimspec;

/**
 * JPA Criteria APIのSELECT句に関する様々な表現方法を示すクラス。
 * <p>
 * このクラスでは以下のSELECT句の書き方を実装しています：
 * <ul>
 * <li>カラム指定、定数値、計算式</li>
 * <li>数値関数、文字列関数、日時関数</li>
 * <li>単純CASE式、検索CASE式</li>
 * <li>集約関数、COALESCE関数</li>
 * <li>スカラサブクエリ、SQL関数</li>
 * </ul>
 */
@Service
public class JpaSelectClauseExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public JpaSelectClauseExample(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {
        logger.info("2. SELECT句の書き方");
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
    }

    private void example01() {
        logger.info("2.1 照会するカラムを指定する");
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

    private void example02() {
        logger.info("2.2 定数値を指定する");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        // 取得するカラムを指定する。
        var numInt = cb.literal(Integer.MAX_VALUE);
        var numLong = cb.literal(Long.MAX_VALUE);
        var numFloat = cb.literal(Float.MAX_VALUE * 0.999_999_97f);
        var numDouble = cb.literal(Double.MAX_VALUE);
        var numBigInteger = cb.literal(
                BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(Long.MAX_VALUE))
        );
        var numBigDecimal = cb.literal(
                BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.valueOf(Long.MAX_VALUE))
                        .divide(BigDecimal.TEN.pow(20))
        );
        query.multiselect(
                numInt,
                numLong,
                numFloat,
                numDouble,
                numBigInteger,
                numBigDecimal
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("literal: {} {} {} {} {} {}",
                    r.get(numInt),
                    r.get(numLong),
                    r.get(numFloat),
                    r.get(numDouble),
                    r.get(numBigInteger),
                    r.get(numBigDecimal)
            );
        }
    }

    private void example03() {
        logger.info("2.3 数値の加減乗除");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        // 取得するカラムを指定する。
        var numSum = cb.sum(
                cb.literal(1_000_001),
                cb.literal(1_000)
        );
        var numDiff = cb.diff(
                cb.literal(1_000_001),
                cb.literal(1_000)
        );
        var numProd = cb.prod(
                cb.literal(1_000_001),
                cb.literal(1_000)
        );
        var numQuot = cb.quot(
                cb.literal(1_000_001),
                cb.literal(1_000)
        );
        var numMod = cb.mod(
                cb.literal(1_000_001),
                cb.literal(1_000)
        );
        query.multiselect(
                numSum,
                numDiff,
                numProd,
                numQuot,
                numMod
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("calc: {} {} {} {} {}",
                    r.get(numSum),
                    r.get(numDiff),
                    r.get(numProd),
                    r.get(numQuot),
                    r.get(numMod)
            );
        }
    }

    private void example04() {
        logger.info("2.4 数値関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        // 取得するカラムを指定する。
        var signPos = cb.sign(cb.literal(Integer.MAX_VALUE));
        var signNeg = cb.sign(cb.sum(cb.literal(Integer.MIN_VALUE), cb.literal(1)));
        var negPos = cb.neg(cb.literal(Integer.MAX_VALUE));
        var negNeg = cb.neg(cb.sum(cb.literal(Integer.MIN_VALUE), cb.literal(1)));
        var absPos = cb.abs(cb.literal(Integer.MAX_VALUE));
        var absNeg = cb.abs(cb.sum(cb.literal(Integer.MIN_VALUE), cb.literal(1)));
        var ceilingPos = cb.ceiling(cb.literal(1234.5678));
        var ceilingNeg = cb.ceiling(cb.literal(-1234.5678));
        var floorPos = cb.floor(cb.literal(1234.5678));
        var floorNeg = cb.floor(cb.literal(-1234.5678));
        var sqrt = cb.sqrt(cb.literal(2.0));
        var exp = cb.exp(cb.literal(2.0));
        var ln = cb.ln(cb.literal(2.0));
        var power = cb.power(cb.literal(2), cb.literal(32));
        var roundPos = cb.round(cb.literal(1234.5678), 2);
        var roundNeg = cb.round(cb.literal(1234.5678), -2);
        query.multiselect(
                signPos, signNeg,
                negPos, negNeg,
                absPos, absNeg,
                ceilingPos, ceilingNeg,
                floorPos, floorNeg,
                sqrt,
                exp, ln,
                power,
                roundPos, roundNeg
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("numerical function: {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {}",
                    r.get(signPos), r.get(signNeg),
                    r.get(negPos), r.get(negNeg),
                    r.get(absPos), r.get(absNeg),
                    r.get(ceilingPos), r.get(ceilingNeg),
                    r.get(floorPos), r.get(floorNeg),
                    r.get(sqrt),
                    r.get(exp), r.get(ln),
                    r.get(power),
                    r.get(roundPos), r.get(roundNeg)
            );
        }
    }

    private void example05() {
        logger.info("2.5 文字列関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        // 取得するカラムを指定する。
        var concat = cb.concat(cb.literal("ABCDE"), cb.literal("FGHIJ"));
        var substring = cb.substring(cb.literal("ABCDEFGHIJ"), cb.literal(5));
        var trim = cb.trim(cb.literal("  ABCDE  "));
        var trimLeading = cb.trim(Trimspec.LEADING, cb.literal("  ABCDE  "));
        var trimBoth = cb.trim(Trimspec.BOTH, cb.literal("  ABCDE  "));
        var trimTrailing = cb.trim(Trimspec.TRAILING, cb.literal("  ABCDE  "));
        var lower = cb.lower(cb.literal("ABCDEFGHIJ"));
        var upper = cb.upper(cb.literal("abcdefghij"));
        var length = cb.length(cb.literal("ABCDEFGHIJ"));
        var locate = cb.locate(cb.literal("ABCDEFGHIJ"), cb.literal("DEF"));
        query.multiselect(
                concat,
                substring,
                trim, trimLeading, trimBoth, trimTrailing,
                lower, upper,
                length,
                locate
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("string function: {} {} {} {} {} {} {} {} {} {}",
                    r.get(concat),
                    r.get(substring),
                    r.get(trim), r.get(trimLeading), r.get(trimBoth), r.get(trimTrailing),
                    r.get(lower), r.get(upper),
                    r.get(length),
                    r.get(locate)
            );
        }
    }

    private void example06() {
        logger.info("2.6 日時関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        // 取得するカラムを指定する。
        Expression<Timestamp> currentTimestamp = cb.currentTimestamp();
        Expression<Date> currentDate = cb.currentDate();
        Expression<Time> currentTime = cb.currentTime();
        Expression<LocalDateTime> localDatetime = cb.localDateTime();
        Expression<LocalDate> localDate = cb.localDate();
        Expression<LocalTime> localTime = cb.localTime();
        query.multiselect(
                currentTimestamp, currentDate, currentTime,
                localDatetime, localDate, localTime
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("datetime function: {} {} {} {} {} {}",
                    r.get(currentTimestamp).toLocalDateTime(),
                    r.get(currentDate).toLocalDate(),
                    r.get(currentTime).toLocalTime(),
                    r.get(localDatetime), r.get(localDate), r.get(localTime)
            );
        }
    }

    private void example07() {
        logger.info("2.7 単純CASE式");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 単純CASE式を組み立てる。
        var status = cb.selectCase(so.get(SalesOrder_.status))
                .when(cb.literal(SalesOrderStatus.NEW), cb.literal("NEW"))
                .when(cb.literal(SalesOrderStatus.APPROVED), cb.literal("APPROVED"))
                .when(cb.literal(SalesOrderStatus.PREPARING), cb.literal("PREPARING"))
                .when(cb.literal(SalesOrderStatus.SHIPPED), cb.literal("SHIPPED"))
                .otherwise(cb.literal("UNKNOWN"));

        // 結果として抽出する項目を指定する。
        query.multiselect(
                so,
                status
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, status {}",
                    r.get(so).getId(), r.get(so).getStatus(),
                    r.get(status)
            );
        }
    }

    private void example08() {
        logger.info("2.8 検索CASE式");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // 検索CASE式を組み立てる。
        var status = cb.selectCase()
                .when(
                        cb.equal(so.get(SalesOrder_.status), cb.literal(SalesOrderStatus.NEW)),
                        cb.literal("NEW")
                )
                .when(
                        cb.equal(so.get(SalesOrder_.status), cb.literal(SalesOrderStatus.APPROVED)),
                        cb.literal("APPROVED")
                )
                .when(
                        cb.equal(so.get(SalesOrder_.status), cb.literal(SalesOrderStatus.PREPARING)),
                        cb.literal("PREPARING")
                )
                .when(
                        cb.equal(so.get(SalesOrder_.status), cb.literal(SalesOrderStatus.SHIPPED)),
                        cb.literal("SHIPPED")
                )
                .otherwise(cb.literal("UNKNOWN"));

        // 結果として抽出する項目を指定する。
        query.multiselect(
                so,
                status
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, status {}",
                    r.get(so).getId(), r.get(so).getStatus(),
                    r.get(status)
            );
        }
    }

    private void example09() {
        logger.info("2.9 集約関数(SUM)");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);
        ListJoin<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item, JoinType.LEFT);

        // 集約単位を指定する。
        query.groupBy(so);

        // 集約関数。
        var amount = cb.sum(
                cb.prod(
                        soi.get(SalesOrderItem_.unitPrice),
                        soi.get(SalesOrderItem_.quantity)
                )
        );

        // 結果として抽出する項目を指定する。
        query.multiselect(
                so,
                amount
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, amount {}",
                    r.get(so).getId(), r.get(so).getStatus(),
                    r.get(amount)
            );
        }
    }

    private void example10() {
        logger.info("2.10 COALESCE関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);
        ListJoin<SalesOrder, SalesOrderItem> soi = so.join(SalesOrder_.item, JoinType.LEFT);

        // 集約単位を指定する。
        query.groupBy(so);

        // COALESCE関数 (集約結果に対して適用)。
        var amount = cb.coalesce()
                .value(cb.sum(
                        cb.prod(
                                soi.get(SalesOrderItem_.unitPrice),
                                soi.get(SalesOrderItem_.quantity)
                        )
                ))
                .value(cb.literal(BigDecimal.ZERO));

        // 結果として抽出する項目を指定する。
        query.multiselect(
                so,
                amount
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, amount {}",
                    r.get(so).getId(), r.get(so).getStatus(),
                    r.get(amount)
            );
        }
    }

    private void example11() {
        logger.info("2.11 スカラサブクエリ");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalesOrder> so = query.from(SalesOrder.class);

        // スカラサブクエリを組み立てる。
        Subquery<BigDecimal> amount = query.subquery(BigDecimal.class);
        Root<SalesOrderItem> soi = amount.from(SalesOrderItem.class);
        amount.where(
                cb.equal(
                        soi.get(SalesOrderItem_.salesOrder).get(SalesOrder_.id),
                        so.get(SalesOrder_.id)
                )
        );
        amount.select(
                cb.sum(
                        cb.prod(
                                soi.get(SalesOrderItem_.unitPrice),
                                soi.get(SalesOrderItem_.quantity).as(BigDecimal.class)
                        )
                )
        );

        // 結果として抽出する項目を指定する。
        query.multiselect(
                so,
                amount
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("SalesOrder: {} {}, amount {}",
                    r.get(so).getId(), r.get(so).getStatus(),
                    r.get(amount)
            );
        }
    }

    private void example12() {
        logger.info("2.12 SQL関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 抽出条件を組み立てる。
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        // SQL関数呼び出し。
        Expression<Number> cos00deg = cb.function("cos", Number.class,
                cb.prod(
                        cb.function("pi", Number.class),
                        cb.quot(cb.literal(0.0), cb.literal(180.0))
                )
        );
        Expression<Number> cos30deg = cb.function("cos", Number.class,
                cb.prod(
                        cb.function("pi", Number.class),
                        cb.quot(cb.literal(30.0), cb.literal(180.0))
                )
        );
        Expression<Number> cos45deg = cb.function("cos", Number.class,
                cb.prod(
                        cb.function("pi", Number.class),
                        cb.quot(cb.literal(45.0), cb.literal(180.0))
                )
        );
        Expression<Number> cos60deg = cb.function("cos", Number.class,
                cb.prod(
                        cb.function("pi", Number.class),
                        cb.quot(cb.literal(60.0), cb.literal(180.0))
                )
        );
        Expression<Number> cos90deg = cb.function("cos", Number.class,
                cb.prod(
                        cb.function("pi", Number.class),
                        cb.quot(cb.literal(90.0), cb.literal(180.0))
                )
        );

        // 結果として抽出する項目を指定する。
        query.multiselect(
                cos00deg, cos30deg, cos45deg, cos60deg, cos90deg
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("cos: {} {} {} {} {}",
                    r.get(cos00deg), r.get(cos30deg), r.get(cos45deg), r.get(cos60deg), r.get(cos90deg)
            );
        }
    }
}
