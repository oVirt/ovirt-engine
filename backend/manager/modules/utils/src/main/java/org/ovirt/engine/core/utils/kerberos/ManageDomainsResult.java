package org.ovirt.engine.core.utils.kerberos;

public class ManageDomainsResult extends Exception {
    private static final long serialVersionUID = -2897637328396868452L;
    private ManageDomainsResultEnum enumResult;
    private int exitCode;
    private String detailedMessage;

    public ManageDomainsResult(ManageDomainsResultEnum enumResult) {
        this.exitCode = enumResult.getExitCode();
        this.detailedMessage = enumResult.getDetailedMessage();
        this.enumResult = enumResult;
    }

    public ManageDomainsResult(ManageDomainsResultEnum enumResult, String param) {
        this.exitCode = enumResult.getExitCode();
        this.detailedMessage = String.format(enumResult.getDetailedMessage(), param);
        this.enumResult = enumResult;
    }

    public ManageDomainsResult(ManageDomainsResultEnum enumResult, String... params) {
        this.exitCode = enumResult.getExitCode();
        this.detailedMessage = String.format(enumResult.getDetailedMessage(), params);
        this.enumResult = enumResult;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }

    public ManageDomainsResultEnum getEnumResult() {
        return enumResult;
    }

    public void setEnumResult(ManageDomainsResultEnum enumResult) {
        this.enumResult = enumResult;
    }

    public boolean isSuccessful() {
        return enumResult == ManageDomainsResultEnum.OK;
    }
}
