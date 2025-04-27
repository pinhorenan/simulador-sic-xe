package sicxesimulator.utils;

import org.junit.jupiter.api.Test;
import sicxesimulator.common.utils.Checker;

import static org.junit.jupiter.api.Assertions.*;

class CheckerTest {

    @Test
    void testIsMnemonicValid() {
        // Verifica mnemônicos válidos (caso insensível a maiúsculas/minúsculas)
        assertTrue(Checker.isMnemonic("ADD"));
        assertTrue(Checker.isMnemonic("lda"));
    }

    @Test
    void testIsMnemonicInvalid() {
        // Mnemônico inexistente deve retornar falso
        assertFalse(Checker.isMnemonic("INVALID"));
    }

    @Test
    void testIsNumericLiteralDecimal() {
        assertTrue(Checker.isNumericLiteral("12345"));
    }

    @Test
    void testIsNumericLiteralHex() {
        assertTrue(Checker.isNumericLiteral("1A2B3C"));
    }

    @Test
    void testIsNumericLiteralInvalid() {
        assertFalse(Checker.isNumericLiteral("12G34"));
    }

    @Test
    void testIsValidRegisterName() {
        assertTrue(Checker.isValidRegisterName("A"));
        assertFalse(Checker.isValidRegisterName("Z"));
    }
}
