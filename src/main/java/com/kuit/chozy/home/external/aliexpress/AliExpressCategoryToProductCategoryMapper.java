package com.kuit.chozy.home.external.aliexpress;

import com.kuit.chozy.home.entity.ProductCategory;

import java.util.HashMap;
import java.util.Map;

public final class AliExpressCategoryToProductCategoryMapper {

    private AliExpressCategoryToProductCategoryMapper() {}

    private static final Map<Long, ProductCategory> SECOND_LEVEL_MAP = new HashMap<>();
    private static final Map<Long, ProductCategory> FIRST_LEVEL_MAP = new HashMap<>();

    static {
        // =========================
        // TOYS
        // =========================
        putSecond(200001726L, ProductCategory.TOYS);
        putSecond(200001725L, ProductCategory.TOYS);
        putSecond(200001387L, ProductCategory.TOYS);
        putSecond(200001383L, ProductCategory.TOYS);
        putSecond(100001698L, ProductCategory.TOYS);
        putSecond(100001714L, ProductCategory.TOYS);
        putSecond(100001716L, ProductCategory.TOYS);
        putSecond(201292714L, ProductCategory.TOYS);

        // =========================
        // HOBBY
        // =========================
        putSecond(100005529L, ProductCategory.HOBBY);
        putSecond(100005371L, ProductCategory.HOBBY);
        putSecond(100005537L, ProductCategory.HOBBY);
        putSecond(200003500L, ProductCategory.HOBBY);
        putSecond(100005481L, ProductCategory.HOBBY);
        putSecond(100005360L, ProductCategory.HOBBY);
        putSecond(200003540L, ProductCategory.HOBBY);

        // =========================
        // HOME
        // =========================
        putSecond(125L, ProductCategory.HOME);
        putSecond(405L, ProductCategory.HOME);
        putSecond(1541L, ProductCategory.HOME);
        putSecond(3710L, ProductCategory.HOME);
        putSecond(200000920L, ProductCategory.HOME);
        putSecond(200033149L, ProductCategory.HOME);

        // =========================
        // ELECTRONICS
        // =========================
        putSecond(702L, ProductCategory.ELECTRONICS);
        putSecond(701L, ProductCategory.ELECTRONICS);
        putSecond(200001081L, ProductCategory.ELECTRONICS);
        putSecond(200001076L, ProductCategory.ELECTRONICS);
        putSecond(200001074L, ProductCategory.ELECTRONICS);
        putSecond(200001077L, ProductCategory.ELECTRONICS);
        putSecond(200001086L, ProductCategory.ELECTRONICS);
        putSecond(100000039L, ProductCategory.ELECTRONICS);
        putSecond(200165142L, ProductCategory.ELECTRONICS);

        // =========================
        // PET
        // =========================
        putSecond(100006664L, ProductCategory.PET);

        // =========================
        // BEAUTY
        // =========================

        // Beauty & Health 하위 leaf
        putSecond(1513L, ProductCategory.BEAUTY);      // Sanitary Paper
        putSecond(3305L, ProductCategory.BEAUTY);      // Oral Hygiene
        putSecond(3306L, ProductCategory.BEAUTY);      // Skin Care
        putSecond(660103L, ProductCategory.BEAUTY);    // Makeup
        putSecond(660302L, ProductCategory.BEAUTY);    // Shaving & Hair Removal
        putSecond(201169002L, ProductCategory.BEAUTY); // Beauty Equipment
        putSecond(202188610L, ProductCategory.BEAUTY); // Rehabilitation Supplies
        putSecond(202219877L, ProductCategory.BEAUTY); // Medical Laboratory Equipment
        putSecond(200001976L, ProductCategory.BEAUTY); // Tattoo & Body Art
        putSecond(200001508L, ProductCategory.BEAUTY); // Sex Products
        putSecond(200001355L, ProductCategory.BEAUTY); // Health Care
        putSecond(200001288L, ProductCategory.BEAUTY); // Bath & Shower
        putSecond(200001221L, ProductCategory.BEAUTY); // Fragrances & Deodorants
        putSecond(200001187L, ProductCategory.BEAUTY); // Tools & Accessories
        putSecond(200001168L, ProductCategory.BEAUTY); // Hair Care & Styling
        putSecond(200001147L, ProductCategory.BEAUTY); // Nail Art & Tools
        putSecond(201248902L, ProductCategory.BEAUTY); // Dental Supplies
        putSecond(201217706L, ProductCategory.BEAUTY); // Massage & Relaxation
        putSecond(100000616L, ProductCategory.BEAUTY); // Skin Care Tool
        putSecond(202236816L, ProductCategory.BEAUTY); // Perfume

        // Hair Extensions & Wigs 관련
        putSecond(200165144L, ProductCategory.BEAUTY);
        putSecond(200317142L, ProductCategory.BEAUTY);
        putSecond(200319142L, ProductCategory.BEAUTY);
        putSecond(201303603L, ProductCategory.BEAUTY);
        putSecond(200166144L, ProductCategory.BEAUTY);
        putSecond(200167144L, ProductCategory.BEAUTY);
        putSecond(200396142L, ProductCategory.BEAUTY);
        putSecond(202170002L, ProductCategory.BEAUTY);
        putSecond(200168146L, ProductCategory.BEAUTY);
        putSecond(201222636L, ProductCategory.BEAUTY);
        putSecond(127886013L, ProductCategory.BEAUTY);

        // =========================
        // FASHION
        // =========================

        // 1) Apparel & Accessories
        putFirst(3L, ProductCategory.FASHION);
        putSecond(200003274L, ProductCategory.FASHION); // Sportswears

        // 2) Women's Clothing
        putFirst(200000345L, ProductCategory.FASHION);

        putSecond(349L, ProductCategory.FASHION);
        putSecond(201531101L, ProductCategory.FASHION);
        putSecond(202219298L, ProductCategory.FASHION);
        putSecond(201303001L, ProductCategory.FASHION);
        putSecond(202220408L, ProductCategory.FASHION);
        putSecond(200129142L, ProductCategory.FASHION);
        putSecond(201241002L, ProductCategory.FASHION);
        putSecond(201240602L, ProductCategory.FASHION);
        putSecond(200128142L, ProductCategory.FASHION);
        putSecond(200001918L, ProductCategory.FASHION);
        putSecond(200001908L, ProductCategory.FASHION);
        putSecond(200001912L, ProductCategory.FASHION);
        putSecond(200001911L, ProductCategory.FASHION);
        putSecond(200003494L, ProductCategory.FASHION);
        putSecond(201771210L, ProductCategory.FASHION);
        putSecond(200000367L, ProductCategory.FASHION);
        putSecond(200000366L, ProductCategory.FASHION);
        putSecond(200000373L, ProductCategory.FASHION);
        putSecond(200000348L, ProductCategory.FASHION);
        putSecond(200000347L, ProductCategory.FASHION);
        putSecond(200000346L, ProductCategory.FASHION);
        putSecond(200000865L, ProductCategory.FASHION);
        putSecond(200000778L, ProductCategory.FASHION);
        putSecond(200000796L, ProductCategory.FASHION);
        putSecond(201516501L, ProductCategory.FASHION);
        putSecond(201515701L, ProductCategory.FASHION);
        putSecond(202240602L, ProductCategory.FASHION);

        // 3) Men's Clothing
        putFirst(200000343L, ProductCategory.FASHION);

        putSecond(201236604L, ProductCategory.FASHION);
        putSecond(202220211L, ProductCategory.FASHION);
        putSecond(202220407L, ProductCategory.FASHION);
        putSecond(202225807L, ProductCategory.FASHION);
        putSecond(201240601L, ProductCategory.FASHION);
        putSecond(200128143L, ProductCategory.FASHION);
        putSecond(200001860L, ProductCategory.FASHION);
        putSecond(200001877L, ProductCategory.FASHION);
        putSecond(200001819L, ProductCategory.FASHION);
        putSecond(200000344L, ProductCategory.FASHION);
        putSecond(200000779L, ProductCategory.FASHION);
        putSecond(200000795L, ProductCategory.FASHION);
        putSecond(202235806L, ProductCategory.FASHION);
        putSecond(200005141L, ProductCategory.FASHION);
        putSecond(202236004L, ProductCategory.FASHION);
        putSecond(202236401L, ProductCategory.FASHION);
        putSecond(201515601L, ProductCategory.FASHION);

        // 4) Shoes
        putFirst(322L, ProductCategory.FASHION);

        putSecond(200133142L, ProductCategory.FASHION);
        putSecond(200131145L, ProductCategory.FASHION);
        putSecond(200001000L, ProductCategory.FASHION);
        putSecond(32210L, ProductCategory.FASHION);
        putSecond(32299L, ProductCategory.FASHION);

        // 5) Luggage & Bags
        putFirst(1524L, ProductCategory.FASHION);

        putSecond(201294604L, ProductCategory.FASHION);
        putSecond(3803L, ProductCategory.FASHION);
        putSecond(3806L, ProductCategory.FASHION);
        putSecond(201298604L, ProductCategory.FASHION);
        putSecond(201336907L, ProductCategory.FASHION);
        putSecond(201337808L, ProductCategory.FASHION);
        putSecond(380520L, ProductCategory.FASHION);
        putSecond(202236005L, ProductCategory.FASHION);
        putSecond(152409L, ProductCategory.FASHION);
        putSecond(152499L, ProductCategory.FASHION);

        // 6) Jewelry & Accessories
        putFirst(36L, ProductCategory.FASHION);

        putSecond(1509L, ProductCategory.FASHION);
        putSecond(201239108L, ProductCategory.FASHION);
        putSecond(201238105L, ProductCategory.FASHION);
        putSecond(200001680L, ProductCategory.FASHION);
        putSecond(200001479L, ProductCategory.FASHION);
        putSecond(200001478L, ProductCategory.FASHION);
        putSecond(200370154L, ProductCategory.FASHION);

        // 7) Apparel Accessories
        putFirst(200000297L, ProductCategory.FASHION);

        putSecond(200000395L, ProductCategory.FASHION);
        putSecond(200000402L, ProductCategory.FASHION);
        putSecond(200000399L, ProductCategory.FASHION);
        putSecond(200000394L, ProductCategory.FASHION);
        putSecond(200000298L, ProductCategory.FASHION);
        putSecond(200000305L, ProductCategory.FASHION);
        putSecond(200000440L, ProductCategory.FASHION);
        putSecond(202235409L, ProductCategory.FASHION);
        putSecond(201159809L, ProductCategory.FASHION);

        // 8) Underwear
        putFirst(200574005L, ProductCategory.FASHION);

        putSecond(202219099L, ProductCategory.FASHION);
        putSecond(200001894L, ProductCategory.FASHION);
        putSecond(200001866L, ProductCategory.FASHION);
        putSecond(200001865L, ProductCategory.FASHION);
        putSecond(200000349L, ProductCategory.FASHION);
        putSecond(200000854L, ProductCategory.FASHION);

        // 9) Novelty & Special Use
        putFirst(200000532L, ProductCategory.FASHION);

        putSecond(201302006L, ProductCategory.FASHION);
        putSecond(202226810L, ProductCategory.FASHION);
        putSecond(200000740L, ProductCategory.FASHION);
        putSecond(200000668L, ProductCategory.FASHION);
        putSecond(100005769L, ProductCategory.FASHION);


        // =========================
        // FIRST LEVEL fallback
        // =========================
        putFirst(26L, ProductCategory.TOYS);
        putFirst(18L, ProductCategory.HOBBY);
        putFirst(15L, ProductCategory.HOME);
        putFirst(7L, ProductCategory.ELECTRONICS);
        putFirst(6L, ProductCategory.ELECTRONICS);
        putFirst(3L, ProductCategory.FASHION);
        putFirst(66L, ProductCategory.BEAUTY);
        putFirst(34L, ProductCategory.AUTOMOTIVE);
    }

    private static void putSecond(Long id, ProductCategory c) {
        SECOND_LEVEL_MAP.put(id, c);
    }

    private static void putFirst(Long id, ProductCategory c) {
        FIRST_LEVEL_MAP.put(id, c);
    }

    public static ProductCategory toProductCategory(Long secondLevelId, Long firstLevelId) {
        if (secondLevelId != null) {
            ProductCategory c = SECOND_LEVEL_MAP.get(secondLevelId);
            if (c != null) return c;
        }
        if (firstLevelId != null) {
            ProductCategory c = FIRST_LEVEL_MAP.get(firstLevelId);
            if (c != null) return c;
        }
        return ProductCategory.HOME;
    }
}
