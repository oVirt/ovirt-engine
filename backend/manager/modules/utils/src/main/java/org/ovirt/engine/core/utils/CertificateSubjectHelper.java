package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class CertificateSubjectHelper {

    public static String getCertificateSubject(String hostName) {
        String certificateSubject=null;
        if (hostName != null) {
            certificateSubject = "O=" + getOrganizationName() +
                    ",CN=" + hostName.replace("\\", "\\\\").replace(",", "\\,");
        }

        return certificateSubject;
    }

    public static String getOrganizationName() {
        String orgName = Config.getValue(ConfigValues.OrganizationName);
        return orgName.replace("\\", "\\\\").replace(",", "\\,");
    }

}
