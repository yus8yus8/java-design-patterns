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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests {@link CustomerService}.
 */
class CustomerServiceTest {

    private CustomerService customerService;
    private DbCustomerDao mockedDao = mock(DbCustomerDao.class);
    private final CustomerDto customerDto = new CustomerDto(1, "Freddy", "Krueger", 0);
    private final Customer customer = new Customer(1, "Freddy", "Krueger");
    private final Customer updatedCustomer = new Customer(1, "Han", "Krueger");

    /**
     * setup a CustomerService.
     */
    @BeforeEach
    void setUp(){
        customerService = new CustomerService(mockedDao);
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test getCustomerById returns empty result when the customer does not exist.
     */
    @Test
    void getCustomerById_whenCustomerNotExist_returnEmpty() {
        doReturn(Optional.empty()).when(mockedDao).getById(1);
        var result = customerService.getCustomerById(1);
        assertTrue(result.isEmpty());
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test getCustomerById returns the expected cutomer when the customer exists.
     */
    @Test
    void getCustomerById_whenCustomerExist_returnCustomer() {
        doReturn(Optional.of(customerDto)).when(mockedDao).getById(1);
        var result = customerService.getCustomerById(1);
        assertEquals(result.get(), customer);
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer is created with version zero when no exception occurs.
     */
    @Test
    void create_whenNoException_createCustomerWithVersionZero() {
        customerService.create(customer);
        final var dtoCaptor = ArgumentCaptor.forClass(CustomerDto.class);
        verify(mockedDao).create(dtoCaptor.capture());

        final var customerDto = dtoCaptor.getValue();
        assertNotNull(customerDto);
        assertEquals(1, customerDto.getId());
        assertEquals("Freddy", customerDto.getFirstName());
        assertEquals("Krueger", customerDto.getLastName());
        assertEquals(0, customerDto.getVersion());
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test exception is thrown when updating a non-exist customer.
     */
    @Test
    void update_whenCustomerNotExist_throwsException() {
        doReturn(Optional.empty()).when(mockedDao).getById(1);
        assertThrows(CustomException.class, () -> { customerService.update(customer); });
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer is updated with current version plus one when no exception occurs.
     */
    @Test
    void update_whenNoException_updateCustomerWithNewVerision() {
        doReturn(Optional.of(customerDto)).when(mockedDao).getById(1);
        customerService.update(updatedCustomer);
        final var dtoCaptor = ArgumentCaptor.forClass(CustomerDto.class);
        verify(mockedDao).update(dtoCaptor.capture());

        final var customerDto = dtoCaptor.getValue();
        assertNotNull(customerDto);
        assertEquals(1, customerDto.getId());
        assertEquals("Han", customerDto.getFirstName());
        assertEquals("Krueger", customerDto.getLastName());
        assertEquals(0, customerDto.getVersion());
    }

    // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
    /**
     * Test customer is deleted when no exception occurs.
     */
    @Test
    void deleteCustomerById_whenNoException_deleteCustomer() {
        customerService.deleteCustomerById(1);
        verify(mockedDao, times(1)).deleteById(1);
    }
}