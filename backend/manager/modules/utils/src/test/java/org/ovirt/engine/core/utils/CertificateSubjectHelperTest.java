package org.ovirt.engine.core.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.config.ConfigValues;

@ExtendWith(MockConfigExtension.class)
public class CertificateSubjectHelperTest {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.OrganizationName, "ORG"));
    }

    @Test
    public void testGetCertificateSubject() {
        final String CERTIFICATE_SUBJECT = "O=ORG,CN=HOSTNAME";
        String certificateSubject = CertificateSubjectHelper.getCertificateSubject("HOSTNAME");
        assertEquals(CERTIFICATE_SUBJECT, certificateSubject);
    }
}
