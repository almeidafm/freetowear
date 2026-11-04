package com.freetowear.repository;

import com.freetowear.entity.Address;
import com.freetowear.entity.Customer;
import com.freetowear.enums.UF;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddressRepositoryTest extends RepositoryTestBase {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnEmptyWhenCustomerHasNoAddresses() {
        Customer customer = persistCustomer("customer@example.com");
        flushAndClear();

        List<Address> result = addressRepository.findAllByCustomerId(customer.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnOnlyAddressesBelongingToRequestedCustomer() {
        Customer customer = persistCustomer("customer@example.com");
        Customer otherCustomer = persistCustomer("other@example.com");
        Address expected = persistAddress(customer, "01001000", false);
        persistAddress(otherCustomer, "20040002", true);
        flushAndClear();

        List<Address> result = addressRepository.findAllByCustomerId(customer.getId());

        assertThat(result).extracting(Address::getId).containsExactly(expected.getId());
    }

    @Test
    void shouldReturnMultipleAddressesForCustomer() {
        Customer customer = persistCustomer("customer@example.com");
        Address first = persistAddress(customer, "01001000", true);
        Address second = persistAddress(customer, "20040002", false);
        flushAndClear();

        List<Address> result = addressRepository.findAllByCustomerId(customer.getId());

        assertThat(result).extracting(Address::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(result).extracting(Address::getDefaultAddress)
                .containsExactlyInAnyOrder(true, false);
    }

    @Test
    void shouldReturnEmptyWhenCustomerIdDoesNotExist() {
        persistAddress(persistCustomer("customer@example.com"), "01001000", false);
        flushAndClear();

        assertThat(addressRepository.findAllByCustomerId("01J00000000000000000000000")).isEmpty();
    }

    private Customer persistCustomer(String email) {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail(email);
        customer.setPassword("password");
        customer.setTermsAndConditionsAcceptedAt(LocalDateTime.now());
        entityManager.persist(customer);
        return customer;
    }

    private Address persistAddress(Customer customer, String cep, boolean defaultAddress) {
        Address address = new Address();
        address.setCustomer(customer);
        address.setCep(cep);
        address.setStreet("Test Street");
        address.setNumber("123");
        address.setNeighborhood("Test Neighborhood");
        address.setCity("São Paulo");
        address.setState(UF.SP);
        address.setDefaultAddress(defaultAddress);
        entityManager.persist(address);
        return address;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}