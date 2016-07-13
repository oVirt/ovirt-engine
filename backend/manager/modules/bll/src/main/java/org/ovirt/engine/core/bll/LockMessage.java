package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * It allows to create message string with variable replacements in format
 * "engineMessage$var1Name var1Value$var2Name var2Value".
 *
 * @see CommandBase#extractVariableDeclarations
 */
public class LockMessage {
    private final StringBuilder messageBuilder = new StringBuilder();

    public LockMessage(EngineMessage message) {
        messageBuilder.append(message.name());
    }

    /**
     * It adds variable replacement.
     * @param variableName variable name
     * @param variableValue variable value
     * @return fluent this
     */
    public LockMessage with(String variableName, String variableValue) {
        messageBuilder.append(ReplacementUtils.createSetVariableString(variableName, variableValue));
        return this;
    }

    /**
     * It adds variable replacement if {@code variableValue} is not {@code null}.
     * @param variableName variable name
     * @param variableValue variable value
     * @return fluent this
     */
    public LockMessage withOptional(String variableName, String variableValue) {
        if (variableValue != null) {
            with(variableName, variableValue);
        }
        return this;
    }

    @Override
    public String toString() {
        return messageBuilder.toString();
    }
}
