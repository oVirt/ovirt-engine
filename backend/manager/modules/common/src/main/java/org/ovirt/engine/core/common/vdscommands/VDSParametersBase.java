package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.Set;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class VDSParametersBase {

    private BrokerCommandCallback callback;

    private Set<EngineError> expectedEngineErrors = Collections.emptySet();

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
        return tsb;
    }

    public VDSParametersBase withCallback(BrokerCommandCallback callback) {
        this.callback = callback;
        return this;
    }

    public BrokerCommandCallback getCallback() {
        return callback;
    }

    @Override
    public final String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
