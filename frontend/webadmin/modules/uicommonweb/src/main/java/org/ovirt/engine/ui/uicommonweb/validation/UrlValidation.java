package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.Uri;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class UrlValidation implements IValidation {

    private final Set<String> allowedSchemes = new HashSet<String>();

    public UrlValidation(String[] allowedSchemes) {
        if (allowedSchemes == null) {
            this.allowedSchemes.add(""); //$NON-NLS-1$
        } else {
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
        if (!allowedSchemes.contains(uri.getScheme())) {
            res.setSuccess(false);
            res.getReasons().add(getSchemeMessage());
        }
        return res;
    }

    protected String getUriMessage() {
        return ConstantsManager.getInstance().getConstants().uriInvalidFormat();
    }

    protected String getSchemeMessage() {
        return ConstantsManager.getInstance().getConstants().urlSchemeNotHttp();
    }

    protected HostAddressValidation getHostValidation() {
        return new HostAddressValidation();
    }

}
