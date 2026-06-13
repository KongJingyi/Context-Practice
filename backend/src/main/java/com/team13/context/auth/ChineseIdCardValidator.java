package com.team13.context.auth;

/**
 * 18 位中国大陆居民身份证格式 + 校验位（与前端 idCard.ts 一致）。
 */
public final class ChineseIdCardValidator {

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final String CHECK_CODES = "10X98765432";

    private ChineseIdCardValidator() {
    }

    public static boolean isValid(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            return false;
        }
        String normalized = idCard.trim().toUpperCase();
        if (!normalized.matches("\\d{17}[\\dX]")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.getNumericValue(normalized.charAt(i)) * WEIGHTS[i];
        }
        return CHECK_CODES.charAt(sum % 11) == normalized.charAt(17);
    }
}
