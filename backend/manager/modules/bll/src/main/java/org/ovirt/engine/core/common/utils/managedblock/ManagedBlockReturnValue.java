package org.ovirt.engine.core.common.utils.managedblock;

import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class ManagedBlockReturnValue {
    private int returnCode;
    private String output;

    public ManagedBlockReturnValue(int returnCode, String output) {
        this.returnCode = returnCode;
        this.output = output;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean getSucceed() {
        return returnCode == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ManagedBlockReturnValue)) {
            return false;
        }
        ManagedBlockReturnValue that = (ManagedBlockReturnValue) o;
        return returnCode == that.returnCode &&
                Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnCode, output);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("returnCode", returnCode)
                .append("output", output)
                .toString();
    }
}
