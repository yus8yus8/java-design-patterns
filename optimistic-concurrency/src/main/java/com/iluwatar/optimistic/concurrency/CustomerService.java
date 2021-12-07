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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A domain service which manages customer and hide details like optimistic concurrency
 * control in the infrastructure layer.
 */
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

  private final DbCustomerDao customerDao;

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Get customer as Optional by id.
   *
   * @param id unique identifier of the customer.
   * @return an optional with customer if a customer exists, empty optional otherwise.
   */
  public Optional<Customer> getCustomerById(int id) {
    return customerDao.getById(id)
            .map(customerDto -> Customer.builder()
                    .id(customerDto.getId())
                    .firstName(customerDto.getFirstName())
                    .lastName(customerDto.getLastName())
                    .build());
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Create a customer.
   *
   * @param customer the customer to be created.
   */
  public void create(Customer customer) {
    customerDao.create(CustomerDto.builder()
        .id(customer.getId())
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .version(0)
        .build());
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Update a customer.
   *
   * @param customer the customer to be updated.
   */
  public void update(Customer customer) {
    final CustomerDto customerDto = customerDao.getById(customer.getId())
            .orElseThrow(() -> new CustomException(
                    String.format("No customer with Id: %d exists", customer.getId())));
    customerDao.update(CustomerDto.builder()
        .id(customer.getId())
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .version(customerDto.getVersion())
        .build());
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Delete a customer.
   *
   * @param id identify the customer to be deleted.
   */
  public void deleteCustomerById(int id) {
    customerDao.deleteById(id);
  }
}
