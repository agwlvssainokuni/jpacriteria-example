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

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Selection;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.TemporalUnit;
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
        example008();
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
}
