package org.ovirt.engine.core.common.utils.cinderlib;

public class CinderlibReturnValue {
    private int returnCode;
    private String output;

    public CinderlibReturnValue(int returnCode, String output) {
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
}
