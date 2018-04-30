package org.ovirt.engine.ui.frontend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ErrorTranslatorTest {

    private ErrorTranslator translator = new ErrorTranslator();

    @Test
    public void isVariableDeclaration_simpleVariableDeclaration() {
        assertTrue(translator.isVariableDeclaration("$x aaa")); //$NON-NLS-1$
    }

    @Test
    public void isVariableDeclaration_simpleReferenceToVariable() {
        assertFalse(translator.isVariableDeclaration("${x} aaa")); //$NON-NLS-1$
    }

    @Test
    public void isVariableDeclaration_declarationContainsVariableReference() {
        assertTrue(translator.isVariableDeclaration("$x aaa ${a}")); //$NON-NLS-1$
    }

    @Test
    public void isVariableDeclaration_variableReferenceContainsDeclaration() {
        assertFalse(translator.isVariableDeclaration("${a} aaa $x")); //$NON-NLS-1$
    }
}
