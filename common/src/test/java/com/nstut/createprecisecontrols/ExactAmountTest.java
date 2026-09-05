package com.nstut.createprecisecontrols;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExactAmountTest {
    @Test void parsesExactValue() { assertEquals(1440, ExactAmount.parseAndClamp("1440", 1, 90000)); }
    @Test void clampsLow() { assertEquals(1, ExactAmount.parseAndClamp("0", 1, 64)); }
    @Test void clampsHigh() { assertEquals(64, ExactAmount.parseAndClamp("999", 1, 64)); }
    @Test void rejectsEmpty() { assertThrows(NumberFormatException.class, () -> ExactAmount.parseAndClamp("", 1, 64)); }
    @Test void exactParserAcceptsBoundary() { assertEquals(64, ExactAmount.parseInRange("64", 1, 64)); }
    @Test void exactParserRejectsBelowRange() { assertThrows(NumberFormatException.class, () -> ExactAmount.parseInRange("0", 1, 64)); }
    @Test void exactParserRejectsAboveRange() { assertThrows(NumberFormatException.class, () -> ExactAmount.parseInRange("65", 1, 64)); }
    @Test void exactParserAcceptsNegativeSentinel() { assertEquals(-1, ExactAmount.parseInRange("-1", -1, 99)); }
}
