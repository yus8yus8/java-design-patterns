/*
 * The MIT License
 * Copyright © 2014-2021 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.iluwatar.optimistic.concurrency;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests {@link DbCustomerDao}.
 */
class DbCustomerDaoIntegrationTest {

    private static final String DB_URL = "jdbc:h2:~/dao";
    private DbCustomerDao dao;
    private final CustomerDto customer = new CustomerDto(1, "Freddy", "Krueger", 0);
    private final CustomerDto customerToUpdate = new CustomerDto(1, "Anne", "Krueger", 0);
    private final CustomerDto updatedCustomer = new CustomerDto(1, "Anne", "Krueger", 1);
    private final CustomerDto oldVersionCustomer = new CustomerDto(1, "Anne", "Krueger", 0);

    /**
     * Creates customers schema.
     *
     * @throws SQLException if there is any error while creating schema.
     */
    @BeforeEach
    void setUp() throws SQLException {
        var dataSource = new JdbcDataSource();
        dataSource.setURL(DB_URL);
        dao = new DbCustomerDao(dataSource);
        try (var connection = DriverManager.getConnection(DB_URL);
             var statement = connection.createStatement()) {
            statement.execute(CustomerSchemaSqlUtils.CREATE_SCHEMA_SQL);
        }
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test getById returns the expected customer when the customer exists.
     */
    @Test
    void getById_whenCustomerExist_returnCustomer() {
        dao.create(customer);
        var result = dao.getById(1);
        assertEquals(result.get(), customer);
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test getById returns empty result when the customer does not exist.
     */
    @Test
    void getById_whenCustomerNotExist_returnEmpty() {
        var result = dao.getById(1);
        assertTrue(result.isEmpty());
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer is created when no exception occurs.
     */
    @Test
    void create_whenNoException_customerCreated() {
        dao.create(customer);
        var result = dao.getById(1);
        assertEquals(result.get(), customer);
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer is updated with current version plus one when no exception occurs.
     */
    @Test
    void update_whenNoException_customerUpdated() {
        dao.create(customer);
        dao.update(customerToUpdate);
        var result = dao.getById(1);
        assertEquals(result.get(), updatedCustomer);
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test CustomException is thrown when updating customer with older version.
     */
    @Test
    void update_whenUpdateOldVersionCustomer_throwsCustomException() {
        dao.create(customer);
        dao.update(customerToUpdate);
        assertThrows(CustomException.class, () -> { dao.update(oldVersionCustomer); });
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer is deleted when no exception occurs.
     */
    @Test
    void deleteById_whenNoException_customerDeleted() {
        dao.create(customer);
        dao.deleteById(1);
        var result = dao.getById(1);
        assertTrue(result.isEmpty());
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Delete customer schema for fresh setup per test.
     *
     * @throws SQLException if any error occurs.
     */
    @AfterEach
    void deleteSchema() throws SQLException {
        try (var connection = DriverManager.getConnection(DB_URL);
             var statement = connection.createStatement()) {
            statement.execute(CustomerSchemaSqlUtils.DELETE_SCHEMA_SQL);
        }
    }
}
