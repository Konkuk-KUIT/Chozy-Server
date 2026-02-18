package com.kuit.chozy.home.external.coupang;

import com.kuit.chozy.home.entity.ProductCategory;

import java.util.List;

public final class CoupangCategoryIdMapper {
    private CoupangCategoryIdMapper(){}

    public static List<Integer> toCoupangCategoryIds(ProductCategory c) {
        return switch (c) {
            case FASHION -> List.of(1001, 1002, 1030);              // 여성/남성/유아동패션
            case BEAUTY -> List.of(1010);                           // 뷰티
            case HOME -> List.of(1013, 1014, 1015);                 // 주방/생활/홈인테리어
            case ELECTRONICS -> List.of(1016);                      // 가전디지털
            case HOBBY -> List.of(1017, 1019, 1021);                // 스포츠/도서음반/DVD/문구오피스
            case TOYS -> List.of(1011, 1020);                       // 출산유아동/완구취미
            case PET -> List.of(1029);                              // 반려동물
            case AUTOMOTIVE -> List.of(1018);                       // 자동차용품
        };
    }
}
