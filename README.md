# BiteSpeed Identity Reconciliation API

A production-ready backend service built for the **BiteSpeed Backend Engineering Internship Assignment**.

This service reconciles customer identities across multiple purchases using different emails or phone numbers and returns a unified customer profile.

---

##  Live API

> After deployment, update this:

```
Base URL: https://identity-reconciliation-system.onrender.com/identify
```

Health Check:

```
GET /
```

Identify Endpoint:

```
POST /identify
```

---

##  Problem Statement

Customers may place orders using different contact details.

Example:

| Order | Email                           | Phone |
| ----- | ------------------------------- | ----- |
| 1     | [a@mail.com](mailto:a@mail.com) | 111   |
| 2     | [b@mail.com](mailto:b@mail.com) | 111   |

Both belong to **same customer**.

This service:

* links related contacts
* maintains primary identity
* returns consolidated customer data

---

## ⚙️ Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* PostgreSQL
* H2 (local testing)
* Maven
* Render (Deployment)

---

##  Project Structure

```
src/main/java/com/bitespeed/identity_reconciliation
│
├── controller
├── service
├── repository
├── entity
└── dto
```

---

## 📡 API Usage

### POST `/identify`

### Request

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
    "emails": ["doc@fluxkart.com","george@hillvalley.edu"],
    "phoneNumbers": ["999999","123456"],
    "secondaryContactIds": [2,3]
  }
}
```

---

##  Features

* Identity reconciliation logic
* Primary ↔ Secondary contact linking
* Automatic primary selection (oldest contact)
* Transactional consistency
* RESTful API design
* Production deployment ready

---

##  Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

API runs at:

```
http://localhost:8080
```

Health Check:

```
GET /
```

---

##  Deployment (Render)

1. Push repository to GitHub
2. Create PostgreSQL database on Render
3. Create Web Service (Java)
4. Configure:

Build Command:

```
./mvnw clean package
```

Start Command:

```
java -jar target/identity-reconciliation-0.0.1-SNAPSHOT.jar
```

Environment Variables:

```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
PORT
```

---

##  Engineering Decisions

* Oldest contact enforced as PRIMARY
* New information creates SECONDARY contact
* Graph traversal used to merge identities
* Database schema auto-managed via Hibernate

---

##  Author

Isha Gupta
BiteSpeed Backend Internship Submission
