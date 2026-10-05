package com.horizonbank.horizon_api.repository;

import com.horizonbank.horizon_api.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPan(String pan);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);
}