package com.b9.json.jsonplatform.order.infrastructure.repository;

import com.b9.json.jsonplatform.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testFindByTitiperId() {
        Order order = new Order();
        UUID titiperId = UUID.randomUUID();
        order.setTitiperId(titiperId);
        order.setQuantity(2);
        orderRepository.save(order);

        List<Order> found = orderRepository.findByTitiperId(titiperId);

        assertFalse(found.isEmpty());
        assertEquals(titiperId, found.get(0).getTitiperId());
    }
}