package org.ovirt.engine.core.utils;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class CertificateSubjectHelper {

    public static String getCertificateSubject(String hostName) {
        String certificateSubject=null;
        if (hostName != null) {
            certificateSubject = "";
            String organization = getOrganizationName();
            if (!StringUtils.isEmpty(organization)) {
                certificateSubject += "O=" + organization + ",";
            }
            certificateSubject += "CN=" + hostName.replace("\\", "\\\\").replace(",", "\\,");
        }

        return certificateSubject;
    }

    public static String getOrganizationName() {
        String orgName = Config.getValue(ConfigValues.OrganizationName);
        if (orgName == null) {
            return "";
        }
        return orgName.replace("\\", "\\\\").replace(",", "\\,");
    }

}
