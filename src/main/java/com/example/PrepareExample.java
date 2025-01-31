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

import com.example.db.entity.*;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PrepareExample {

    private final EntityManager em;

    public PrepareExample(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void execute() {

        var customer01 = createCustomer("FIRST_01", "LAST_01");
        var customer02 = createCustomer("FIRST_02", "LAST_02");

        var product01 = createProduct("PROD_01", BigDecimal.valueOf(100));
        var product02 = createProduct("PROD_02", BigDecimal.valueOf(1000));
        var product03 = createProduct("PROD_03", BigDecimal.valueOf(10000));

        createSalesOrder(
                customer01,
                List.of(SalesOrderStatus.NEW),
                Map.of()
        );

        createSalesOrder(
                customer02,
                List.of(
                        SalesOrderStatus.NEW,
                        SalesOrderStatus.APPROVED
                ),
                Map.of()
        );

        createSalesOrder(
                customer01,
                List.of(
                        SalesOrderStatus.NEW,
                        SalesOrderStatus.APPROVED,
                        SalesOrderStatus.PREPARING,
                        SalesOrderStatus.SHIPPED
                ),
                Map.of(
                        product01, 10L,
                        product02, 20L
                )
        );

        createSalesOrder(
                customer02,
                List.of(
                        SalesOrderStatus.NEW,
                        SalesOrderStatus.APPROVED,
                        SalesOrderStatus.PREPARING
                ),
                Map.of(
                        product01, 1L,
                        product02, 2L,
                        product03, 3L
                )
        );

        em.flush();
        em.clear();
    }

    private Customer createCustomer(String firstName, String lastName) {
        var customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        em.persist(customer);
        return customer;
    }

    private Product createProduct(String productName, BigDecimal unitPrice) {
        var product = new Product();
        product.setName(productName);
        product.setUnitPrice(unitPrice);
        em.persist(product);
        return product;
    }

    private void createSalesOrder(
            Customer customer,
            List<SalesOrderStatus> salesOrderStatus,
            Map<Product, Long> itemList
    ) {

        var salesOrder = new SalesOrder();
        salesOrder.setCustomer(customer);
        salesOrderStatus.forEach(status -> {
            salesOrder.setStatus(status);
            em.persist(salesOrder);
            em.persist(SalesOrderHistory.from(salesOrder));
        });

        itemList.forEach((key, value) -> {
            var item = new SalesOrderItem();
            item.setSalesOrder(salesOrder);
            item.setUnitPrice(key.getUnitPrice());
            item.setQuantity(value);
            item.setProduct(key);
            em.persist(item);
        });
    }
}
