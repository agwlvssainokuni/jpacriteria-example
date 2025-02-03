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

import com.example.hibernate.HibernateFromClauseExample;
import com.example.hibernate.HibernateWithClauseExample;
import com.example.jpa.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Runner implements ApplicationRunner {

    private final PrepareExample prepareExample;
    private final JpaBasicUsageExample jpaBasicUsageExample;
    private final JpaSelectClauseExample jpaSelectClauseExample;
    private final JpaFromClauseExample jpaFromClauseExample;
    private final JpaWhereClauseExample jpaWhereClauseExample;
    private final JpaOtherUsageExample jpaOtherUsageExample;
    private final HibernateWithClauseExample hibernateWithClauseExample;
    private final HibernateFromClauseExample hibernateFromClauseExample;
    private final SandboxExample sandboxExample;

    public Runner(
            PrepareExample prepareExample,
            JpaBasicUsageExample jpaBasicUsageExample,
            JpaSelectClauseExample jpaSelectClauseExample,
            JpaFromClauseExample jpaFromClauseExample,
            JpaWhereClauseExample jpaWhereClauseExample,
            JpaOtherUsageExample jpaOtherUsageExample,
            HibernateWithClauseExample hibernateWithClauseExample,
            HibernateFromClauseExample hibernateFromClauseExample,
            SandboxExample sandboxExample
    ) {
        this.prepareExample = prepareExample;
        this.jpaBasicUsageExample = jpaBasicUsageExample;
        this.jpaSelectClauseExample = jpaSelectClauseExample;
        this.jpaFromClauseExample = jpaFromClauseExample;
        this.jpaWhereClauseExample = jpaWhereClauseExample;
        this.jpaOtherUsageExample = jpaOtherUsageExample;
        this.hibernateWithClauseExample = hibernateWithClauseExample;
        this.hibernateFromClauseExample = hibernateFromClauseExample;
        this.sandboxExample = sandboxExample;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        prepareExample.execute();

        jpaBasicUsageExample.execute();
        jpaSelectClauseExample.execute();
        jpaFromClauseExample.execute();
        jpaWhereClauseExample.execute();
        jpaOtherUsageExample.execute();

        hibernateWithClauseExample.execute();
        hibernateFromClauseExample.execute();
        // sandboxExample.execute();
    }
}
