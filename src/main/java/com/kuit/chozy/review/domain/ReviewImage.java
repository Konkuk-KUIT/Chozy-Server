package com.kuit.chozy.review.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "review_images")
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Integer sortOrder;

    protected ReviewImage() {
    }

    public ReviewImage(Review review, String fileName, String contentType, Integer sortOrder) {
        this.review = review;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Review getReview() {
        return review;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}