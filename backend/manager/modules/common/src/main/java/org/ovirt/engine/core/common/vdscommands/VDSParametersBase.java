package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.Set;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class VDSParametersBase {

    private boolean runAsync;
    private Set<EngineError> expectedEngineErrors = Collections.<EngineError>emptySet();

    public VDSParametersBase() {
        runAsync = true;
    }

    public boolean getRunAsync() {
        return runAsync;
    }

    public void setRunAsync(boolean value) {
        runAsync = value;
    }

    public Set<EngineError> getExpectedEngineErrors() {
        return expectedEngineErrors;
    }

    public void setExpectedEngineErrors(Set<EngineError> errors) {
        expectedEngineErrors = errors;
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        if (!getExpectedEngineErrors().isEmpty()) {
            tsb.append("expectedEngineErrors", getExpectedEngineErrors());
        }
        return tsb.append("runAsync", runAsync);
    }

    @Override
    public final String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
