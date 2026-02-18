package com.kuit.chozy.home.external.coupang;

import com.kuit.chozy.home.entity.ProductCategory;

import java.util.List;
import java.util.Locale;

public final class CoupangKeywordCategoryMapper {

    private CoupangKeywordCategoryMapper() {}

    public static ProductCategory guess(String keyword, String productName, ProductCategory fallback) {
        String k = normalize(keyword);
        String n = normalize(productName);
        String text = (k + " " + n).trim();

        if (containsAny(text, List.of(
                "니트", "코트", "패딩", "후드", "맨투맨", "셔츠", "블라우스", "원피스", "스커트",
                "바지", "청바지", "데님", "슬랙스", "자켓", "가디건", "운동복", "레깅스",
                "브라", "팬티", "속옷", "언더웨어", "양말", "스타킹",
                "가방", "백팩", "지갑", "벨트", "모자", "캡", "비니",
                "신발", "운동화", "스니커즈", "부츠", "로퍼", "샌들", "슬리퍼"
        ))) return ProductCategory.FASHION;

        if (containsAny(text, List.of(
                "립", "립스틱", "틴트", "파운데이션", "쿠션", "컨실러", "아이섀도", "섀도", "마스카라",
                "스킨", "토너", "로션", "에센스", "세럼", "크림", "클렌징", "선크림", "선블록",
                "향수", "바디워시", "샴푸", "트리트먼트", "헤어", "네일"
        ))) return ProductCategory.BEAUTY;

        if (containsAny(text, List.of(
                "노트북", "맥북", "그램", "데스크탑", "pc", "모니터", "키보드", "마우스",
                "이어폰", "헤드폰", "스피커", "아이폰", "갤럭시", "태블릿", "아이패드",
                "충전기", "보조배터리", "ssd", "hdd", "그래픽카드", "공유기", "와이파이",
                "카메라", "액션캠", "드론"
        ))) return ProductCategory.ELECTRONICS;

        if (containsAny(text, List.of(
                "침대", "매트리스", "이불", "베개", "커튼", "러그", "쿠션",
                "수납", "정리함", "선반", "행거",
                "냄비", "프라이팬", "주방", "그릇", "접시", "텀블러", "컵",
                "청소기", "물티슈", "휴지", "세제", "섬유유연제", "살균", "탈취",
                "조명", "스탠드", "가습기", "제습기"
        ))) return ProductCategory.HOME;

        if (containsAny(text, List.of(
                "강아지", "고양이", "사료", "간식", "캣타워", "모래", "하네스", "리드줄",
                "배변", "패드", "장난감", "펫", "미용", "브러쉬"
        ))) return ProductCategory.PET;

        if (containsAny(text, List.of(
                "피규어", "프라모델", "레고", "보드게임", "퍼즐", "취미", "캠핑", "등산",
                "낚시", "골프", "자전거", "헬스", "요가"
        ))) return ProductCategory.HOBBY;

        if (containsAny(text, List.of(
                "장난감", "인형", "키즈", "유아", "어린이", "블록", "로봇", "교육완구"
        ))) return ProductCategory.TOYS;

        if (containsAny(text, List.of(
                "차량", "자동차", "카시트", "블랙박스", "타이어", "세차", "엔진오일", "와이퍼"
        ))) return ProductCategory.AUTOMOTIVE;

        return fallback != null ? fallback : ProductCategory.HOME; // 마지막 기본값
    }

    private static boolean containsAny(String text, List<String> keywords) {
        for (String kw : keywords) {
            if (!kw.isBlank() && text.contains(normalize(kw))) return true;
        }
        return false;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }
}
