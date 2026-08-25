package com.nammametro.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records", indexes = {
    @Index(name = "idx_idem_key", columnList = "idempotency_key", unique = true)
})
public class IdempotencyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType; // e.g. PAYMENT, BOOKING

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Lob
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String resourceType, String requestHash, String responseBody, Integer statusCode) {
        this.idempotencyKey = idempotencyKey;
        this.resourceType = resourceType;
        this.requestHash = requestHash;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
