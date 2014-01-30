package org.ovirt.engine.core.authentication.provisional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.authentication.AuthenticationResult;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ProvisionalAuthenticationResult extends AuthenticationResult {


    private volatile static Map<String, String> passwordChangeMsgPerDomain = null;
    private String domain;
    private UserAuthenticationResult authResult;
    private static Log log = LogFactory.getLog(ProvisionalAuthenticationResult.class);


    public ProvisionalAuthenticationResult(String domain, UserAuthenticationResult userAuthResult) {
        this.authResult = userAuthResult;
        if (passwordChangeMsgPerDomain == null) {
            synchronized (ProvisionalAuthenticationResult.class) {
                if (passwordChangeMsgPerDomain == null) {
                    passwordChangeMsgPerDomain = new HashMap<String, String>();
                    String changePasswordUrl = Config.<String> getValue(ConfigValues.ChangePasswordMsg);
                    String[] pairs = changePasswordUrl.split(",");
                    for (String pair : pairs) {
                        // Split the pair in such a way that if the URL contains :, it will not be split to strings
                        String[] pairParts = pair.split(":", 2);
                        if (pairParts.length >= 2) {
                            try {
                                passwordChangeMsgPerDomain.put(pairParts[0], URLDecoder.decode(pairParts[1], "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                log.error("Eror in decoding the change password message/url. Message is: "
                                        + e.getMessage());
                                log.debug("", e);
                            }
                        }
                    }
                }
            }
        }
        this.domain = domain;

    }

    @Override
    public boolean isSuccessful() {
        return authResult.isSuccessful();
    }

    @Override
    public List<String> resolveMessage() {
        Iterator<VdcBllMessages> it = authResult.getErrorMessages().iterator();
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            VdcBllMessages current = it.next();
            if (current == VdcBllMessages.USER_PASSWORD_EXPIRED) {
                String passwordChangeMsg = passwordChangeMsgPerDomain.get(domain);
                if (passwordChangeMsg != null) {
                    if (passwordChangeMsg.indexOf("http") == 0 || passwordChangeMsg.indexOf("https") == 0) {
                        result.add(VdcBllMessages.USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED.name());
                        result.add(String.format("$URL %1$s", passwordChangeMsg));
                    } else {
                        result.add(VdcBllMessages.USER_PASSWORD_EXPIRED_CHANGE_MSG_PROVIDED.name());
                        result.add(String.format("$MSG %1$s", passwordChangeMsg));
                    }
                } else {
                    result.add(current.name());
                }
            } else {
                result.add(current.name());

            }
        }
        return result;
    }

}
