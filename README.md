# BiteSpeed Identity Reconciliation API

Backend service built for the BiteSpeed Backend Engineering Internship assignment.

The API reconciles customer identities created using different emails or phone numbers and returns a unified customer profile.

---

## Live API

Base URL

https://identity-reconciliation-system.onrender.com

Health Check

GET /

Identify Endpoint

POST /identify

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven
* Render

---

## API Usage

### Request

POST /identify

```json
{
  "email": "doc@fluxkart.com",
  "phoneNumber": "999999"
}
```

### Response

```json
{
  "contact": {
    "primaryContactId": 1,
    "emails": ["doc@fluxkart.com"],
    "phoneNumbers": ["999999"],
    "secondaryContactIds": []
  }
}
```

---

## Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

Runs at:

http://localhost:8080

---

## Deployment

Hosted on Render using PostgreSQL database.

Environment Variables:

SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
PORT

---

## Author

Isha Gupta
BiteSpeed Backend Internship Submission
