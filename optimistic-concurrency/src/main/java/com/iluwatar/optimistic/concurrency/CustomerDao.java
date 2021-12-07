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

/**
 * The Data Access Object (DAO) that provides an interface to database.
 */
public interface CustomerDao {

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Get customer as Optional by id.
   *
   * @param id unique identifier of the customer.
   * @return an optional with customer if a customer exists, empty optional otherwise.
   */
  Optional<CustomerDto> getById(int id);

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Create a customer.
   *
   * @param customer the customer to be created.
   */
  void create(CustomerDto customer);

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Update a customer.
   *
   * @param customer the customer to be updated.
   */
  void update(CustomerDto customer);

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1280
  /**
   * Delete a customer.
   *
   * @param id identify the customer to be deleted.
   */
  void deleteById(int id);
}
