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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link CustomerDao} that persists customers in RDBMS.
 */
@Slf4j
@RequiredArgsConstructor
public class DbCustomerDao implements CustomerDao {

  private final DataSource dataSource;

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<CustomerDto> getById(int id) {
    ResultSet resultSet = null;

    try (var connection = getConnection();
         var statement = connection.prepareStatement("SELECT * FROM CUSTOMERS WHERE ID = ?")) {

      statement.setInt(1, id);
      resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return Optional.of(createCustomer(resultSet));
      } else {
        return Optional.empty();
      }
    } catch (final SQLException ex) {
      throw new CustomException(String.format("Failed to get customer with Id: %d", id), ex);
    } finally {
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (final SQLException ex) {
          throw new IllegalStateException(ex);
        }
      }
    }
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * {@inheritDoc}
   */
  @Override
  public void create(CustomerDto customer) {
    try (var connection = getConnection();
         var statement = connection.prepareStatement("INSERT INTO CUSTOMERS VALUES (?,?,?,?)")) {
      statement.setInt(1, customer.getId());
      statement.setString(2, customer.getFirstName());
      statement.setString(3, customer.getLastName());
      statement.setInt(4, customer.getVersion());
      statement.execute();
    } catch (final SQLException ex) {
      throw new CustomException(
        String.format("Failed to create customer with Id: %d", customer.getId()), ex);
    }
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * {@inheritDoc}
   * Only update a customer when the version is the same the version in the database.
   * Increase the version by 1 if the version is the same. Otherwise throws an exception.
   */
  @Override
  public void update(CustomerDto customer) {
    try (var connection = getConnection();
         var statement = connection.prepareStatement(
                 "UPDATE CUSTOMERS SET FNAME = ?, LNAME = ?, VERSION = ? "
                        + "WHERE ID = ? AND VERSION = ?")) {
      statement.setString(1, customer.getFirstName());
      statement.setString(2, customer.getLastName());
      statement.setInt(3, customer.getVersion() + 1);
      statement.setInt(4, customer.getId());
      statement.setInt(5, customer.getVersion());
      if (statement.executeUpdate() == 0) {
        throw new CustomException(
          String.format("Failed to update customer with Id: %d", customer.getId()));
      }
    } catch (final SQLException ex) {
      throw new CustomException(
        String.format("Failed to update customer with Id: %d", customer.getId()), ex);
    }
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * {@inheritDoc} to simplify no return
   */
  @Override
  public void deleteById(int id) {
    try (var connection = getConnection();
        var statement = connection.prepareStatement("DELETE FROM CUSTOMERS WHERE ID = ?")) {
      statement.setInt(1, id);
      statement.execute();
    } catch (final SQLException ex) {
      throw new CustomException(String.format("Failed to delete customer with Id: %d", id), ex);
    }
  }

  private Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  private CustomerDto createCustomer(ResultSet resultSet) throws SQLException {
    return new CustomerDto(resultSet.getInt("ID"),
      resultSet.getString("FNAME"),
      resultSet.getString("LNAME"),
      resultSet.getInt("VERSION"));
  }
}
