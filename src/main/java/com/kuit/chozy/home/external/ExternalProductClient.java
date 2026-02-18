package com.kuit.chozy.home.external;

import com.kuit.chozy.home.dto.request.HomeProductsRequest;
import com.kuit.chozy.home.entity.Vendor;

import java.util.List;

public interface ExternalProductClient {
    Vendor supports();
    List<ProductSnapshot> fetch(HomeProductsRequest r);
}
