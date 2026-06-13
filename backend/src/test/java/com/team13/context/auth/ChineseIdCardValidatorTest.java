package com.team13.context.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChineseIdCardValidatorTest {

    @Test
    void acceptsValidTestId() {
        assertTrue(ChineseIdCardValidator.isValid("110101199001010015"));
    }

    @Test
    void rejectsInvalidCheckDigit() {
        assertFalse(ChineseIdCardValidator.isValid("110101199001010014"));
    }
}
