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

import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;

/**
 * Optimistic concurrency protect data integrity by checking data conflict before commit.
 * It allows parallel data process before commit to increase performance.
 *
 * <p>In an optimistic concurrency model, a violation is considered to have occurred if,
 * after a user receives a value from the database, another user modifies the value before
 * the first user has attempted to modify it.
 *
 * <p>The simplest strategy for optimistic concurrency is to implement a versioning scheme.
 * Each entity under concurrency control is given a version identifier, which is changed
 * every time the data is altered. When modifying an object, you note the version number,
 * make your changes, and commit those changes if someone else hasn't changed the version number
 * of the underlying object since you started making your changes.
 *
 * <p>In the following example, customer is created with version 0. When the customer is updated,
 * the version is updated to 1.
 */
@Slf4j
public class App {
  private static final String DB_URL = "jdbc:h2:~/optimistic-concurrency";

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Program entry point.
   *
   * @param args command line args.
   */
  public static void main(final String[] args) {

    final var dataSource = createDataSource();

    try {
      createSchema(dataSource);
      final var dbDao = new DbCustomerDao(dataSource);
      final var customerService = new CustomerService((dbDao));

      final var customerToCreate =
              Customer.builder().id(1).firstName("Dan").lastName("Danson").build();
      customerService.create(customerToCreate);
      LOGGER.info("Create " + customerToCreate);
      LOGGER.info("Customer data entry created with version 0: "
              + dbDao.getById(1).get().toString());

      final var customerToUpdate =
              Customer.builder().id(1).firstName("Dan").lastName("Danson").build();
      customerService.update(Customer.builder().id(1).firstName("Han").lastName("Danson").build());
      LOGGER.info("Update " + customerToUpdate);
      LOGGER.info("Customer data entry updated with version 1: "
              + dbDao.getById(1).get().toString());

      deleteSchema(dataSource);
    } catch (final SQLException ex) {
      LOGGER.error("Application fail: " + ex.getMessage());
    }
  }

  private static void deleteSchema(DataSource dataSource) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.execute(CustomerSchemaSqlUtils.DELETE_SCHEMA_SQL);
    }
  }

  private static void createSchema(DataSource dataSource) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.execute(CustomerSchemaSqlUtils.CREATE_SCHEMA_SQL);
    }
  }

  private static DataSource createDataSource() {
    var dataSource = new JdbcDataSource();
    dataSource.setURL(DB_URL);
    return dataSource;
  }
}
