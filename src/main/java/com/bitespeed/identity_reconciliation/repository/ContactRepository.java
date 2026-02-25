package com.bitespeed.identity_reconciliation.repository;

import com.bitespeed.identity_reconciliation.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("""
            select c from Contact c
            where (:email is not null and c.email = :email)
               or (:phoneNumber is not null and c.phoneNumber = :phoneNumber)
            """)
    List<Contact> findDirectMatches(@Param("email") String email, @Param("phoneNumber") String phoneNumber);

    @Query("""
            select c from Contact c
            where c.id in :ids
               or (c.linkedId is not null and c.linkedId in :ids)
            """)
    List<Contact> findByIdInOrLinkedIdIn(@Param("ids") Collection<Long> ids);
}
