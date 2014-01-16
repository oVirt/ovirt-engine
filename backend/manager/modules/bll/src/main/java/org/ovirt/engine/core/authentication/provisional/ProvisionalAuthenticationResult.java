package org.ovirt.engine.core.authentication.provisional;

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

public class ProvisionalAuthenticationResult extends AuthenticationResult {


    private volatile static Map<String, String> passwordChangeUrlsPerDomain = null;
    private String domain;
    private UserAuthenticationResult authResult;

    public ProvisionalAuthenticationResult(String domain, UserAuthenticationResult userAuthResult) {
        this.authResult = userAuthResult;
        if (passwordChangeUrlsPerDomain == null) {
            synchronized (ProvisionalAuthenticationResult.class) {
                if (passwordChangeUrlsPerDomain == null) {
                    passwordChangeUrlsPerDomain = new HashMap<String, String>();
                    String changePasswordUrl = Config.<String> getValue(ConfigValues.ChangePasswordUrl);
                    String[] pairs = changePasswordUrl.split(",");
                    for (String pair : pairs) {
                        // Split the pair in such a way that if the URL contains :, it will not be split to strings
                        String[] pairParts = pair.split(":", 2);
                        if (pairParts.length >= 2) {
                            passwordChangeUrlsPerDomain.put(pairParts[0], pairParts[1]);
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
                String passwordChangeUrl = passwordChangeUrlsPerDomain.get(domain);
                if (passwordChangeUrl != null) {
                    result.add(VdcBllMessages.USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED.name());
                    result.add(String.format("$URL %1$s", passwordChangeUrl));
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
