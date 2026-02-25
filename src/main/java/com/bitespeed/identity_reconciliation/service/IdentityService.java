package com.bitespeed.identity_reconciliation.service;

import com.bitespeed.identity_reconciliation.dto.ContactResponse;
import com.bitespeed.identity_reconciliation.dto.IdentifyRequest;
import com.bitespeed.identity_reconciliation.dto.IdentifyResponse;
import com.bitespeed.identity_reconciliation.entity.Contact;
import com.bitespeed.identity_reconciliation.entity.LinkPrecedence;
import com.bitespeed.identity_reconciliation.repository.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IdentityService {

    private final ContactRepository contactRepository;

    public IdentityService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional
    public IdentifyResponse identify(IdentifyRequest request) {
        String email = normalizeEmail(request.getEmail());
        String phoneNumber = normalizePhone(request.getPhoneNumber());

        if (email == null && phoneNumber == null) {
            throw new IllegalArgumentException("Either email or phoneNumber must be provided.");
        }

        List<Contact> directMatches = contactRepository.findDirectMatches(email, phoneNumber);

        if (directMatches.isEmpty()) {
            Contact primary = newContact(email, phoneNumber, null, LinkPrecedence.PRIMARY);
            primary = contactRepository.save(primary);
            return new IdentifyResponse(buildContactResponse(primary, List.of(primary)));
        }

        List<Contact> allLinkedContacts = findAllLinkedContacts(directMatches);

        Contact primary = allLinkedContacts.stream()
                .min(Comparator.comparing(Contact::getCreatedAt).thenComparing(Contact::getId))
                .orElseThrow();

        if (primary.getLinkPrecedence() != LinkPrecedence.PRIMARY || primary.getLinkedId() != null) {
            primary.setLinkPrecedence(LinkPrecedence.PRIMARY);
            primary.setLinkedId(null);
            contactRepository.save(primary);
        }

        for (Contact contact : allLinkedContacts) {
            if (Objects.equals(contact.getId(), primary.getId())) {
                continue;
            }
            boolean changed = false;
            if (contact.getLinkPrecedence() != LinkPrecedence.SECONDARY) {
                contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                changed = true;
            }
            if (!Objects.equals(contact.getLinkedId(), primary.getId())) {
                contact.setLinkedId(primary.getId());
                changed = true;
            }
            if (changed) {
                contactRepository.save(contact);
            }
        }

        Set<String> existingEmails = allLinkedContacts.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> existingPhoneNumbers = allLinkedContacts.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        boolean hasNewEmail = email != null && !existingEmails.contains(email);
        boolean hasNewPhone = phoneNumber != null && !existingPhoneNumbers.contains(phoneNumber);

        if (hasNewEmail || hasNewPhone) {
            Contact secondary = newContact(email, phoneNumber, primary.getId(), LinkPrecedence.SECONDARY);
            contactRepository.save(secondary);
            allLinkedContacts.add(secondary);
        }

        return new IdentifyResponse(buildContactResponse(primary, allLinkedContacts));
    }

    private List<Contact> findAllLinkedContacts(List<Contact> seedContacts) {
        Map<Long, Contact> visited = new HashMap<>();
        Deque<Long> queue = new ArrayDeque<>();

        for (Contact seed : seedContacts) {
            if (seed.getId() != null) {
                queue.add(seed.getId());
            }
            if (seed.getLinkedId() != null) {
                queue.add(seed.getLinkedId());
            }
        }

        while (!queue.isEmpty()) {
            Set<Long> batch = new LinkedHashSet<>();
            while (!queue.isEmpty()) {
                Long id = queue.poll();
                if (id != null && !visited.containsKey(id)) {
                    batch.add(id);
                }
            }

            if (batch.isEmpty()) {
                break;
            }

            List<Contact> contacts = contactRepository.findByIdInOrLinkedIdIn(batch);
            for (Contact contact : contacts) {
                if (visited.putIfAbsent(contact.getId(), contact) == null) {
                    if (contact.getLinkedId() != null && !visited.containsKey(contact.getLinkedId())) {
                        queue.add(contact.getLinkedId());
                    }
                    if (contact.getId() != null && !visited.containsKey(contact.getId())) {
                        queue.add(contact.getId());
                    }
                }
            }
        }

        List<Contact> result = new ArrayList<>(visited.values());
        result.sort(Comparator.comparing(Contact::getCreatedAt).thenComparing(Contact::getId));
        return result;
    }

    private ContactResponse buildContactResponse(Contact primary, Collection<Contact> contacts) {
        List<Contact> orderedContacts = contacts.stream()
                .sorted(Comparator.comparing(Contact::getCreatedAt).thenComparing(Contact::getId))
                .toList();

        LinkedHashSet<String> emails = new LinkedHashSet<>();
        LinkedHashSet<String> phoneNumbers = new LinkedHashSet<>();
        List<Long> secondaryContactIds = new ArrayList<>();

        if (primary.getEmail() != null) {
            emails.add(primary.getEmail());
        }
        if (primary.getPhoneNumber() != null) {
            phoneNumbers.add(primary.getPhoneNumber());
        }

        for (Contact contact : orderedContacts) {
            if (Objects.equals(contact.getId(), primary.getId())) {
                continue;
            }
            if (contact.getEmail() != null) {
                emails.add(contact.getEmail());
            }
            if (contact.getPhoneNumber() != null) {
                phoneNumbers.add(contact.getPhoneNumber());
            }
            secondaryContactIds.add(contact.getId());
        }

        ContactResponse response = new ContactResponse();
        response.setPrimaryContactId(primary.getId());
        response.setEmails(new ArrayList<>(emails));
        response.setPhoneNumbers(new ArrayList<>(phoneNumbers));
        response.setSecondaryContactIds(secondaryContactIds);
        return response;
    }

    private Contact newContact(String email, String phoneNumber, Long linkedId, LinkPrecedence precedence) {
        Contact contact = new Contact();
        contact.setEmail(email);
        contact.setPhoneNumber(phoneNumber);
        contact.setLinkedId(linkedId);
        contact.setLinkPrecedence(precedence);
        return contact;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return phoneNumber.trim();
    }
}
