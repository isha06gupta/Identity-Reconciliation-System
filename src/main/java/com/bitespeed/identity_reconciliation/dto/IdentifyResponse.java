package com.bitespeed.identity_reconciliation.dto;

public class IdentifyResponse {

    private ContactResponse contact;

    public IdentifyResponse() {
    }

    public IdentifyResponse(ContactResponse contact) {
        this.contact = contact;
    }

    public ContactResponse getContact() {
        return contact;
    }

    public void setContact(ContactResponse contact) {
        this.contact = contact;
    }
}
