package com.berkay.customer.service.dataaccess.customer.adapter;

import com.berkay.customer.service.dataaccess.customer.mapper.CustomerDataAccessMapper;
import com.berkay.customer.service.dataaccess.customer.repository.CustomerJpaRepository;
import com.berkay.customer.service.domain.entity.Customer;
import com.berkay.customer.service.ports.output.repository.CustomerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;

    private final CustomerDataAccessMapper customerDataAccessMapper;

    public CustomerRepositoryImpl(CustomerJpaRepository customerJpaRepository,
                                  CustomerDataAccessMapper customerDataAccessMapper) {
        this.customerJpaRepository = customerJpaRepository;
        this.customerDataAccessMapper = customerDataAccessMapper;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        return customerDataAccessMapper.customerEntityToCustomer(
                customerJpaRepository.save(customerDataAccessMapper.customerToCustomerEntity(customer)));
    }

    @Override
    public Optional<Customer> findCustomerByEmail(String email) {
        return customerJpaRepository.findByEmail(email)
                .map(customerDataAccessMapper::customerEntityToCustomer);
    }
}
