package org.ovirt.engine.core.common.utils;


import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;


public class CertificateSubjectHelperTest {

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.OrganizationName, "ORG"));
    @Test
    public void testGetCertificateSubject() {
        final String CERTIFICATE_SUBJECT = "O=ORG,CN=HOSTNAME";
        String certificateSubject = CertificateSubjectHelper.getCertificateSubject("HOSTNAME");
        assertEquals(CERTIFICATE_SUBJECT, certificateSubject);
    }
}
