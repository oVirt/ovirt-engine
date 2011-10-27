package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.bll.WindowsErrorsTranslationMap;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.jwin32.jwin32;

import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class LUAuthenticateUserCommand extends LUBrokerCommandBase {
    private static LogCompat log = LogFactoryCompat.getLog(LUAuthenticateUserCommand.class);

    public LUAuthenticateUserCommand(LdapUserPasswordBaseParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        AdUser user = new AdUser();
        log.debug("Executing LUAuthenticateUserCommand");
        IntByReference lToken = new IntByReference();
        try {
            if (jwin32.advapi32.LogonUserW(
                            new WString(getLoginName().toString()),
                            new WString(getDomain().toString()),
                            new WString(getPassword()),
                            jwin32.LOGON32_LOGON_NETWORK,
                            jwin32.LOGON32_PROVIDER_DEFAULT,
                            lToken
                        )

            ) {
                // Login successful now lets collect the user data.
                user = (AdUser) LdapFactory.getInstance(getDomain()).RunAdAction(
                            AdActionType.GetAdUserByUserName, new LdapSearchByUserNameParameters(
                                    getParameters().getSessionId(), getDomain(), getLoginName())).getReturnValue();
                user.setPassword(getPassword());
                UserAuthenticationResult result = new UserAuthenticationResult(user);
                setReturnValue(result);
                setSucceeded(true);
            } else {
                int lastError = jwin32.kernel32.GetLastError();
                log.error("Last error is: " + lastError);
                VdcBllMessages errorCode = WindowsErrorsTranslationMap.getError(lastError);
                //If there was an error - at first define a general error code of authentication failure, but
                //try to get a more concrete error code
                UserAuthenticationResult result = new UserAuthenticationResult(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
                if (errorCode != null) {
                    log.debug("Found error code " + result.toString());
                    //A more concrete error code is found
                    result = new UserAuthenticationResult(errorCode);
                } else {
                    log.debug("No error code found, using default error code of USER_FAILED_TO_AUTHENTICATE");
                }

                setReturnValue(result);
                setSucceeded(false);
            }
        } catch (RuntimeException ee) {
            // TODO: Need normal Error handling
            // QLogger.getInstance().Warn("Failed authenticating " +
            // getLoginName() + "@" + getDomain(), ee);
        } finally {
            if (lToken.getValue() != 0) {
                jwin32.kernel32.CloseHandle(lToken.getValue());
            }
        }
    }
}
