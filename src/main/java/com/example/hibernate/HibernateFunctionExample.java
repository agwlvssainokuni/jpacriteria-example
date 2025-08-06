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
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.sqm.TemporalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Hibernate固有のSQL関数の使用例を示すクラス。
 * <p>
 * このクラスでは以下のHibernate拡張関数を実装しています：
 * <ul>
 * <li>拡張数値関数（三角関数、対数関数、双曲線関数など）</li>
 * <li>拡張文字列関数（overlay、pad、repeat、left、rightなど）</li>
 * <li>拡張日時関数（format、truncate、duration操作など）</li>
 * <li>Instant型とLocalDateTime型間の変換操作</li>
 * </ul>
 */
@Service
public class HibernateFunctionExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityManager em;

    public HibernateFunctionExample(EntityManager em) {
        this.em = em;
    }

    /**
     * Hibernate固有のSQL関数の使用例を実行します。
     * <p>
     * 以下の例を順次実行します：
     * <ul>
     * <li>拡張数値関数（三角関数、対数関数、双曲線関数等）</li>
     * <li>拡張文字列関数（overlay、pad、repeat、left、right等）</li>
     * <li>拡張日時関数（format、truncate、duration操作等）</li>
     * </ul>
     */
    @Transactional
    public void execute() {
        logger.info("8. (Hibernate拡張) SQL関数");
        example01();
        example02();
        example03();
    }

    /**
     * Hibernate拡張の数値関数群の使用例。
     * <p>
     * 標準JPA以外の数値関数（三角関数、対数関数、双曲線関数など）を使用します。
     * HibernateCriteriaBuilderにキャストすることで拡張関数が利用可能になります。
     */
    private void example01() {
        logger.info("8.1 数値関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // 数値関数を適用する。
        JpaExpression<Double> truncatePos = hcb.truncate(hcb.literal(1234.5678), 2);
        JpaExpression<Double> truncateNeg = hcb.truncate(hcb.literal(1234.5678), -2);
        JpaExpression<Double> log10 = hcb.log10(hcb.literal(100.0));
        JpaExpression<Double> log = hcb.log(hcb.literal(2.0), hcb.literal(8.0));
        JpaExpression<Double> pi = hcb.pi();
        JpaExpression<Double> sin = hcb.sin(hcb.quot(hcb.pi(), hcb.literal(6.0)));
        JpaExpression<Double> cos = hcb.cos(hcb.quot(hcb.pi(), hcb.literal(3.0)));
        JpaExpression<Double> tan = hcb.tan(hcb.quot(hcb.pi(), hcb.literal(4.0)));
        JpaExpression<Double> asin = hcb.asin(hcb.literal(0.5));
        JpaExpression<Double> acos = hcb.acos(hcb.literal(0.5));
        JpaExpression<Double> atan = hcb.atan(hcb.literal(1.0));
        JpaExpression<Double> atan2 = hcb.atan2(hcb.literal(1.0), hcb.literal(1.0));
        JpaExpression<Double> sinh = hcb.sinh(hcb.ln(hcb.literal(2.0)));
        JpaExpression<Double> cosh = hcb.cosh(hcb.ln(hcb.literal(2.0)));
        JpaExpression<Double> tanh = hcb.tanh(hcb.ln(hcb.literal(2.0)));
        JpaExpression<Double> deg = hcb.degrees(hcb.literal(180.0));
        JpaExpression<Double> rad = hcb.radians(hcb.pi());

        query.multiselect(
                truncatePos, truncateNeg,
                log10, log,
                pi, sin, cos, tan,
                asin, acos, atan, atan2,
                sinh, cosh, tanh,
                deg, rad
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("numerical function: {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {}",
                    r.get(truncatePos), r.get(truncateNeg),
                    r.get(log10), r.get(log),
                    r.get(pi), r.get(sin), r.get(cos), r.get(tan),
                    r.get(asin), r.get(acos), r.get(atan), r.get(atan2),
                    r.get(sinh), r.get(cosh), r.get(tanh),
                    r.get(deg), r.get(rad)
            );
        }
    }

    private void example02() {
        logger.info("8.2 文字列関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // 文字列関数を適用する。
        JpaExpression<String> overlay = hcb.overlay(
                hcb.literal("ABCDEFGHIJ"), hcb.literal("def"),
                hcb.literal(4), hcb.literal(4)
        );
        JpaExpression<String> pad = hcb.pad(hcb.literal("ABCDE"), hcb.literal(8));
        JpaExpression<String> padLeading = hcb.pad(CriteriaBuilder.Trimspec.LEADING,
                hcb.literal("ABCDE"), hcb.literal(8), hcb.literal('x'));
        JpaExpression<String> padBoth = hcb.pad(CriteriaBuilder.Trimspec.BOTH,
                hcb.literal("ABCDE"), hcb.literal(8), hcb.literal('x'));
        JpaExpression<String> padTrailing = hcb.pad(CriteriaBuilder.Trimspec.TRAILING,
                hcb.literal("ABCDE"), hcb.literal(8), hcb.literal('x'));
        JpaExpression<String> repeat = hcb.repeat(hcb.literal("ABCDE"), hcb.literal(3));
        JpaExpression<String> left = hcb.left(hcb.literal("ABCDEFGHIJ"), hcb.literal(5));
        JpaExpression<String> right = hcb.right(hcb.literal("ABCDEFGHIJ"), hcb.literal(5));
        JpaExpression<String> replace = hcb.replace(hcb.literal("ABCDEFGHIJ"),
                hcb.literal("DEF"), hcb.literal("def"));
        // MySQLのみ
        //JpaExpression<Boolean> collate = hcb.equal(
        //        hcb.collate(hcb.literal("ABCDEFGHIJ"), "utf8mb4_bin"),
        //        "abcdefghij"
        //);

        query.multiselect(
                overlay,
                pad, padLeading, padBoth, padTrailing,
                repeat, left, right,
                replace // , collate
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("string function: {} {} {} {} {} {} {} {} {}",
                    r.get(overlay),
                    r.get(pad), r.get(padLeading), r.get(padBoth), r.get(padTrailing),
                    r.get(repeat), r.get(left), r.get(right),
                    r.get(replace)  // , r.get(collate)
            );
        }
    }

    /**
     * Hibernate拡張の日時関数群の使用例。
     * <p>
     * 標準JPA以外の日時関数（format、truncate、duration操作など）を使用します。
     * 特にDuration型を使用した期間計算やInstant型の操作が特徴的です。
     */
    private void example03() {
        logger.info("8.3 日時関数");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;

        // クエリを組み立てる。
        JpaCriteriaQuery<Tuple> query = hcb.createTupleQuery();

        // 日時関数を適用する。
        JpaExpression<LocalDateTime> instant = hcb.cast(hcb.currentInstant(), LocalDateTime.class);
        JpaExpression<String> format = hcb.format(hcb.currentInstant(), "yyyy'年'MM'月'dd'日'HH'時'mm'分'ss'秒'SSSSSS'ナノ秒'");
        JpaExpression<Integer> year = hcb.year(hcb.currentInstant());
        JpaExpression<Integer> month = hcb.month(hcb.currentInstant());
        JpaExpression<Integer> day = hcb.day(hcb.currentInstant());
        JpaExpression<Integer> hour = hcb.hour(hcb.currentInstant());
        JpaExpression<Integer> minute = hcb.minute(hcb.currentInstant());
        JpaExpression<Float> second = hcb.second(hcb.currentInstant());
        JpaExpression<LocalDateTime> truncateMonth = hcb.truncate(
                hcb.cast(hcb.currentInstant(), LocalDateTime.class),
                TemporalUnit.MONTH
        );
        JpaExpression<LocalDateTime> truncateHour = hcb.truncate(
                hcb.cast(hcb.currentInstant(), LocalDateTime.class),
                TemporalUnit.HOUR
        );
        JpaExpression<Duration> duration = hcb.duration(1L, TemporalUnit.DAY);
        JpaExpression<Duration> durationSum = hcb.durationSum(
                hcb.duration(1L, TemporalUnit.DAY),
                hcb.duration(1L, TemporalUnit.HOUR)
        );
        JpaExpression<Duration> durationDiff = hcb.durationDiff(
                hcb.duration(1L, TemporalUnit.DAY),
                hcb.duration(1L, TemporalUnit.HOUR)
        );
        JpaExpression<Duration> durationScaled = hcb.durationScaled(
                hcb.literal(0.5),
                hcb.duration(1L, TemporalUnit.DAY)
        );
        JpaExpression<Long> durationByUnit = hcb.durationByUnit(
                TemporalUnit.SECOND,
                hcb.duration(1L, TemporalUnit.DAY)
        );
        JpaExpression<Duration> durationBetween = hcb.durationBetween(
                hcb.literal(LocalDate.now()),
                hcb.literal(LocalDate.now().plusMonths(1L))
        );
        JpaExpression<LocalDateTime> addDuration = hcb.addDuration(
                hcb.cast(hcb.currentInstant(), LocalDateTime.class),
                hcb.duration(1L, TemporalUnit.MONTH)
        );
        JpaExpression<LocalDateTime> subtractDuration = hcb.subtractDuration(
                hcb.cast(hcb.currentInstant(), LocalDateTime.class),
                hcb.duration(1L, TemporalUnit.MONTH)
        );

        query.multiselect(
                instant, format,
                year, month, day,
                hour, minute, second,
                truncateMonth, truncateHour,
                duration,
                durationSum, durationDiff,
                durationScaled, durationByUnit,
                durationBetween,
                addDuration, subtractDuration
        );

        // クエリを発行する。
        List<Tuple> result = em.createQuery(query).getResultList();
        for (var r : result) {
            logger.info("datetime function: {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {} {}",
                    r.get(instant), r.get(format),
                    r.get(year), r.get(month), r.get(day),
                    r.get(hour), r.get(minute), r.get(second),
                    r.get(truncateMonth), r.get(truncateHour),
                    r.get(duration),
                    r.get(durationSum), r.get(durationDiff),
                    r.get(durationScaled), r.get(durationByUnit),
                    r.get(durationBetween),
                    r.get(addDuration), r.get(subtractDuration)
            );
        }
    }
}
