package org.ovirt.engine.api.restapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockConfigExtension.class)
public class DisplayHelperTest {
    private static final String CERTIFICATE = "O=Redhat,CN=X.Y.Z.Q";
    private static final String CA_CERT = "dummy-cert";
    private static final String ORG = "ORG";
    private static final List<Guid> GUIDS = Arrays.asList(
            new Guid("11111111-1111-1111-1111-111111111111"),
            new Guid("22222222-2222-2222-2222-222222222222"));

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.OrganizationName, ORG));
    }

    @Test
    public void testAddDisplayCertificate() {
        Vm vm = new Vm();
        Certificate certificate = new Certificate();
        DisplayHelper.addDisplayCertificate(vm, certificate);

        assertSame(certificate, vm.getDisplay().getCertificate());
    }

    @Test
    public void testGetDisplayCertificatesForMultipleEntitiesNoResult() {
        BackendResource res = mock(BackendResource.class);
        QueryReturnValue result = new QueryReturnValue();
        result.setSucceeded(false);
        when(res.runQuery(eq(QueryType.GetVdsCertificateSubjectsByVmIds), any())).thenReturn(result);

        Map<Guid, Certificate> certificates = DisplayHelper.getDisplayCertificatesForMultipleEntities(res, GUIDS);
        assertTrue(certificates.isEmpty());
    }

    @Test
    public void testGetDisplayCertificatesForMultipleEntities() {
        BackendResource res = mock(BackendResource.class);

        QueryReturnValue result = new QueryReturnValue();
        result.setSucceeded(true);
        Map<Guid, String> subjects = GUIDS.stream().collect(Collectors.toMap(Function.identity(), id -> CERTIFICATE));
        result.setReturnValue(subjects);
        when(res.runQuery(eq(QueryType.GetVdsCertificateSubjectsByVmIds), any())).thenReturn(result);

        result = new QueryReturnValue();
        result.setSucceeded(true);
        result.setReturnValue(CA_CERT);
        when(res.runQuery(eq(QueryType.GetCACertificate), any())).thenReturn(result);


        Map<Guid, Certificate> certificates = DisplayHelper.getDisplayCertificatesForMultipleEntities(res, GUIDS);
        assertEquals(GUIDS.size(), certificates.size());
        for (Guid guid : GUIDS) {
            Certificate cert = certificates.get(guid);
            assertNotNull(cert);
            assertEquals(CERTIFICATE, cert.getSubject());
            assertEquals(CA_CERT, cert.getContent());
            assertEquals(ORG, cert.getOrganization());
        }
    }
}
