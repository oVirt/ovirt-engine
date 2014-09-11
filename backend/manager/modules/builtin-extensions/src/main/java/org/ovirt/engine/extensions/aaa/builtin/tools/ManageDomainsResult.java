package org.ovirt.engine.extensions.aaa.builtin.tools;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Exception during configure domains. If this exception occurred its probably because
 * DB errors. This class define handling of configure exceptions
 */
public class ManageDomainsResult extends Exception {
    private static final long serialVersionUID = -2897637328396868452L;
    private ManageDomainsResultEnum enumResult;
    private int exitCode;
    private String detailedMessage;
    private final static Logger log = LoggerFactory.getLogger(ManageDomainsResult.class);

    /**
     * This constructor is present exception without additional params. we use this one without additional info, only
     * enum result type with default detailed massage.
     * @param enumResult
     *            - enum result type
     */
    public ManageDomainsResult(ManageDomainsResultEnum enumResult) {
        this.exitCode = enumResult.getExitCode();
        this.detailedMessage = enumResult.getDetailedMessage();
        this.enumResult = enumResult;
    }

    /**
     * This constructor gets params for enum result and defaultMsg for a case that params are wrong or include
     * misleading variables (like empty string)
     * @param enumResult
     *            - the error to publish
     * @param defaultMsg
     *            - default output when error found in input.
     * @param params
     *            - enumResult additional params
     */
    public ManageDomainsResult(String defaultMsg, ManageDomainsResultEnum enumResult, String... params) {
        this.exitCode = enumResult.getExitCode();
        this.enumResult = enumResult;
        boolean validParams = true;

        // setting detailed message
        // check params validation
        if (params.length == 0) {
            log.debug("Wrong exception's params received.");
            validParams = false;
        }
        else {
            // Verify parameters value
            for (String param : params) {
                if (StringUtils.isEmpty(param)) {
                    log.debug("Got null value.");
                    validParams = false;
                }
            }
        }

        // if all ok. we have params verified
        if (validParams) {
            this.detailedMessage = String.format(enumResult.getDetailedMessage(), params);
        }
        else {
            if (StringUtils.isEmpty(defaultMsg)) {
                this.detailedMessage = enumResult.getDetailedMessage() +
                  ": One of the parameters for this error is null and no default message to show";
            }
            else {
                this.detailedMessage = defaultMsg;
            }
        }
    }

    public ManageDomainsResult(ManageDomainsResultEnum enumResult, String... params) {
        this("", enumResult, params);
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

    public String getMessage() {
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
