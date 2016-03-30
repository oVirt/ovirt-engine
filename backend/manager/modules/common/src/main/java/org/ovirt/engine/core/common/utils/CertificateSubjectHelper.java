package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class CertificateSubjectHelper {

    public static String getCertificateSubject(String hostName) {
        String certificateSubject=null;
        if (hostName != null) {
            certificateSubject = "O=" + getOrganizationName().replace("\\", "\\\\").replace(",", "\\,") +
                    ",CN=" + hostName.replace("\\", "\\\\").replace(",", "\\,");
        }

        return certificateSubject;
    }

    private static String getOrganizationName() {
        return Config.getValue(ConfigValues.OrganizationName);
    }

}
