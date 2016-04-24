package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.Uri;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class UrlValidation implements IValidation {

    private final Set<String> allowedSchemes = new HashSet<>();

    public UrlValidation(String... allowedSchemes) {
        if (allowedSchemes != null) {
            for (String scheme : allowedSchemes) {
                this.allowedSchemes.add(scheme.toLowerCase());
            }
        }
    }

    @Override
    public ValidationResult validate(Object value) {
        Uri uri = new Uri((String) value);
        ValidationResult res = new ValidationResult();

        if (!uri.isValid()) {
            res.setSuccess(false);
            res.getReasons().add(getUriMessage());
            return res;
        }

        res = getHostValidation().validate(uri.getAuthority().getHost());
        String scheme = uri.getScheme();
        if (!allowedSchemes.contains(scheme)) {
            res.setSuccess(false);
            res.getReasons().add(getSchemeMessage(scheme));
        }
        return res;
    }

    protected String getUriMessage() {
        return ConstantsManager.getInstance().getConstants().uriInvalidFormat();
    }

    protected String getSchemeMessage(String passedScheme) {
        if (allowedSchemes.isEmpty()) {
            return ConstantsManager.getInstance().getMessages().urlSchemeMustBeEmpty(passedScheme);
        } else {
            StringBuilder allowedSchemeList = new StringBuilder();
            for (String scheme : allowedSchemes) {
                allowedSchemeList.append("- ").append(scheme).append('\n'); // $NON-NLS-1$
            }
            return passedScheme.isEmpty() ? ConstantsManager.getInstance()
                    .getMessages()
                    .urlSchemeMustNotBeEmpty(allowedSchemeList.toString()) : ConstantsManager.getInstance()
                    .getMessages()
                    .urlSchemeInvalidScheme(passedScheme, allowedSchemeList.toString());
        }
    }

    protected HostAddressValidation getHostValidation() {
        return new UriHostAddressValidation();
    }

}
