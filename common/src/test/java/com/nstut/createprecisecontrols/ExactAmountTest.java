package com.nstut.createprecisecontrols;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExactAmountTest {
    @Test void parsesExactValue() { assertEquals(1440, ExactAmount.parseAndClamp("1440", 1, 90000)); }
    @Test void clampsLow() { assertEquals(1, ExactAmount.parseAndClamp("0", 1, 64)); }
    @Test void clampsHigh() { assertEquals(64, ExactAmount.parseAndClamp("999", 1, 64)); }
    @Test void rejectsEmpty() { assertThrows(NumberFormatException.class, () -> ExactAmount.parseAndClamp("", 1, 64)); }
}
