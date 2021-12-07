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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link DbCustomerDao}.
 */
class DbCustomerDaoTest {

    private final CustomerDto customer = new CustomerDto(1, "Freddy", "Krueger", 0);
    private final CustomerDto updatedCustomer = new CustomerDto(1, "Anne", "Krueger", 0);
    private DbCustomerDao dao;
    private Connection mockedConnection;
    private PreparedStatement mockedStatement;
    private ResultSet mockedResultSet;

    /**
     * setup a customer DAO.
     *
     * @throws SQLException if any error occurs.
     */
    @BeforeEach
    void setUp() throws SQLException {
        dao = new DbCustomerDao(mockedDatasource());
    }

    private DataSource mockedDatasource() throws SQLException {
        final DataSource mockedDataSource = mock(DataSource.class);
        mockedConnection = mock(Connection.class);
        mockedStatement = mock(PreparedStatement.class);
        mockedResultSet = mock(ResultSet.class);
        doReturn(mockedConnection).when(mockedDataSource).getConnection();
        return mockedDataSource;
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test getById returns the expected customer when the customer exists.
     */
    @Test
    void getById_whenCustomerExist_returnCustomer() throws SQLException {
        doReturn(mockedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        doReturn(mockedResultSet).when(mockedStatement).executeQuery();
        doReturn(true).when(mockedResultSet).next();
        doReturn(1).when(mockedResultSet).getInt("ID");
        doReturn("Freddy").when(mockedResultSet).getString("FNAME");
        doReturn("Krueger").when(mockedResultSet).getString("LNAME");
        doReturn(0).when(mockedResultSet).getInt("VERSION");

        var result = dao.getById(1);
        assertEquals(result.get(), customer);
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test getById returns empty result when the customer does not exist.
     */
    @Test
    void getById_whenCustomerNotExist_returnEmpty() throws SQLException {
        doReturn(mockedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        doReturn(mockedResultSet).when(mockedStatement).executeQuery();
        doReturn(false).when(mockedResultSet).next();

        var result = dao.getById(1);
        assertTrue(result.isEmpty());
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test getById throws CustomException when SQLException occurs.
     */
    @Test
    void getById_whenSQLException_returnCustomException() throws SQLException {
        var exception = new SQLException();
        doThrow(exception).when(mockedConnection).prepareStatement(Mockito.anyString());

        assertThrows(CustomException.class, () -> { dao.getById(1); });
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer creation executes successfully when no exception occurs.
     */
    @Test
    void create_whenNoException_executeWithoutException() throws SQLException {
        doReturn(mockedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());

        assertDoesNotThrow(() -> dao.create(customer));
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer creation throws CustomException when SQLException occurs.
     */
    @Test
    void create_whenSQLException_returnCustomException() throws SQLException {
        var exception = new SQLException();
        doThrow(exception).when(mockedConnection).prepareStatement(Mockito.anyString());

        assertThrows(CustomException.class, () -> { dao.create(customer);});
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer update executes successfully when no exception occurs.
     */
    @Test
    void update_whenUpdateExecute_executeWithoutException() throws SQLException {
        doReturn(mockedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        doReturn(1).when(mockedStatement).executeUpdate();

        assertDoesNotThrow(() -> dao.update(updatedCustomer));
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test CustomException is thrown when customer not updated.
     */
    @Test
    void update_whenUpdateNotExecute_returnCustomException() throws SQLException {
        doReturn(mockedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        doReturn(0).when(mockedStatement).executeUpdate();

        assertThrows(CustomException.class, () -> { dao.update(updatedCustomer); });
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer update throws CustomException when SQLException occurs.
     */
    @Test
    void update_whenSQLException_returnCustomException() throws SQLException {
        var exception = new SQLException();
        doThrow(exception).when(mockedConnection).prepareStatement(Mockito.anyString());

        assertThrows(CustomException.class, () -> { dao.update(updatedCustomer); });
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer deletion executes successfully when no exception occurs.
     */
    @Test
    void deleteById_whenNoException_executeWithoutException() throws SQLException {
        doReturn(mockedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());

        assertDoesNotThrow(() -> dao.deleteById(1));
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer deletion throws CustomException when SQLException occurs.
     */
    @Test
    void deleteById_whenSQLException_returnCustomException() throws SQLException {
        var exception = new SQLException();
        doThrow(exception).when(mockedConnection).prepareStatement(Mockito.anyString());

        assertThrows(CustomException.class, () -> { dao.deleteById(1); });
    }
}