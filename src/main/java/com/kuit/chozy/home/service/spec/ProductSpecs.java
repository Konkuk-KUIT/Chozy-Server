package com.kuit.chozy.home.service.spec;

import com.kuit.chozy.home.entity.Product;
import com.kuit.chozy.home.entity.ProductCategory;
import com.kuit.chozy.home.entity.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecs {
    public static Specification<Product> isActive() {
        return (r,q,cb) -> cb.equal(r.get("status"), ProductStatus.ACTIVE);
    }

    public static Specification<Product> categoryEquals(ProductCategory c) {
        return (r,q,cb) -> cb.equal(r.get("category"), c);
    }

    public static Specification<Product> nameContains(String kw) {
        return (r,q,cb) -> cb.like(r.get("name"), "%" + kw + "%");
    }

    public static Specification<Product> priceGte(Integer v) {
        return (r,q,cb) -> cb.greaterThanOrEqualTo(r.get("listPrice"), v);
    }

    public static Specification<Product> priceLte(Integer v) {
        return (r,q,cb) -> cb.lessThanOrEqualTo(r.get("listPrice"), v);
    }

    public static Specification<Product> ratingGte(Double v) {
        return (r,q,cb) -> cb.greaterThanOrEqualTo(r.get("ratingAvg"), v);
    }

    public static Specification<Product> ratingLte(Double v) {
        return (r,q,cb) -> cb.lessThanOrEqualTo(r.get("ratingAvg"), v);
    }
}
