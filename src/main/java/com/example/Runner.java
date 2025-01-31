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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Runner implements ApplicationRunner {

    private final PrepareExample prepareExample;
    private final BasicUsageExample basicUsageExample;
    private final SelectClauseExample selectClauseExample;
    private final FromClauseExample fromClauseExample;
    private final SandboxExample sandboxExample;

    public Runner(
            PrepareExample prepareExample,
            BasicUsageExample basicUsageExample,
            SelectClauseExample selectClauseExample,
            FromClauseExample fromClauseExample,
            SandboxExample sandboxExample
    ) {
        this.prepareExample = prepareExample;
        this.basicUsageExample = basicUsageExample;
        this.selectClauseExample = selectClauseExample;
        this.fromClauseExample = fromClauseExample;
        this.sandboxExample = sandboxExample;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        prepareExample.execute();
        basicUsageExample.execute();
        selectClauseExample.execute();
        fromClauseExample.execute();
        // sandboxExample.execute();
    }
}
