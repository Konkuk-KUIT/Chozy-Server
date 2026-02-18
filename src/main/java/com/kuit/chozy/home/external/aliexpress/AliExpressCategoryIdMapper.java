package com.kuit.chozy.home.external.aliexpress;

import com.kuit.chozy.home.entity.ProductCategory;

import java.util.List;

public final class AliExpressCategoryIdMapper {

    private AliExpressCategoryIdMapper() {}

    /**
     * ✅ leaf(소분류) category_id 위주로 반환
     * - leaf가 충분히 있는 카테고리: leaf ids 반환
     * - leaf가 부족한 카테고리: 루트 ids 반환(임시) + client에서 keywords를 항상 같이 넣어 보정
     */
    public static List<Long> toAliCategoryIds(ProductCategory c) {
        if (c == null) return List.of();

        return switch (c) {
            case TOYS -> List.of(
                    200001726L, // Games and Puzzles
                    200001725L, // Dolls & Stuffed Toys
                    200001387L, // Stuffed Animals & Plush
                    200001383L, // Building & Construction Toys
                    100001698L, // Baby & Toddler Toys
                    100001714L, // Learning & Education
                    100001716L, // Pretend Play
                    201292714L  // Action & Toy Figures
            );

            case HOBBY -> List.of(
                    100005529L, // Camping & Hiking
                    100005371L, // Fitness & Body Building
                    100005537L, // Fishing
                    200003500L, // Cycling
                    100005481L, // Musical Instruments
                    100005360L, // Golf
                    200003540L  // Racquet Sports
            );

            case HOME -> List.of(
                    125L,        // Garden Supplies
                    405L,        // Home Textile
                    1541L,       // Home Storage & Organization
                    3710L,       // Home Decor
                    200000920L,  // Kitchen,Dining & Bar
                    200033149L,  // Household Merchandises
                    100006664L,  // Pet Products (HOME 안에서도 잘 뜸)
                    100000041L,  // Kitchen Appliances (Home Appliances 트리)
                    100000038L   // Cleaning Appliances
            );

            case ELECTRONICS -> List.of(
                    702L,        // Laptops
                    701L,        // Desktops & AIO
                    200001081L,  // Computer Peripherals
                    200001076L,  // Computer Components
                    200001074L,  // Storage Device
                    200001077L,  // Networking
                    200001086L,  // Tablets
                    100000039L,  // Home Appliance Parts
                    200165142L   // Personal Care Appliances (가전/디바이스 느낌)
            );

            case PET -> List.of(
                    100006664L   // Pet Products
            );

            case FASHION -> List.of(
                    3L,           // Apparel & Accessories
                    200000345L,   // Women's Clothing
                    200000343L,   // Men's Clothing
                    322L,         // Shoes
                    200574005L,   // Underwear
                    36L           // Jewelry & Accessories
            );

            case BEAUTY -> List.of(
                    66L,          // Beauty & Health
                    200165144L    // Hair Extensions & Wigs
            );

            case AUTOMOTIVE -> List.of(
                    34L,          // Automobiles, Parts & Accessories
                    201355758L    // Motorcycle Equipments & Parts
            );
        };
    }

    /**
     * ✅ category 모드에서도 0개 방지용 keywords
     * (루트 기반 카테고리들도 여기로 강제 보정)
     */
    public static String fallbackKeyword(ProductCategory c) {
        if (c == null) return "hot";

        return switch (c) {
            case FASHION -> "women clothing dress hoodie";
            case BEAUTY -> "skincare makeup";
            case HOME -> "home kitchen";
            case ELECTRONICS -> "electronics gadget";
            case HOBBY -> "camping fitness";
            case TOYS -> "toy";
            case PET -> "pet dog cat";
            case AUTOMOTIVE -> "car accessory";
        };
    }
}
