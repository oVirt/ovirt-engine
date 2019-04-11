package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockEngineLocalConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.TrustStoreTestUtils;

@ExtendWith(MockEngineLocalConfigExtension.class)
public class ConfigureConsoleOptionsQueryTest extends
        AbstractQueryTest<ConfigureConsoleOptionsParams, ConfigureConsoleOptionsQuery<ConfigureConsoleOptionsParams>> {

    private static final String SSO_TOKEN = "C_K4s4ZDf-EmOqRuz7JiIdGZnCnC00Z9-bh2zR3WrBPs5jMbyT7srNkpInmVSGe3ir0bINWX5VUuEajBb-Wc2A";
    private static final String CA_CERTIFICATE = "-----BEGIN CERTIFICATE-----\\nMIIDijCCAnKgAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwNTELMAkGA1UEBhMCVVMxDTALBgNVBAoM\\nBFRlc3QxFzAVBgNVBAMMDmhhcHB5Ym94LjQ1NjM3MB4XDTE2MDgxNTE0NDk1OFoXDTI2MDgxNDE0\\nNDk1OFowNTELMAkGA1UEBhMCVVMxDTALBgNVBAoMBFRlc3QxFzAVBgNVBAMMDmhhcHB5Ym94LjQ1\\nNjM3MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA17DZocXTA3jWsT0IrVC4WYKAppqz\\nxCzcHgym6WSPBqlZqaPpfWiARIHps3oQfyLe7ys+ADvHYfb58Ntaor+SANvkvLd+ShOUGMFnsyZ6\\nY5h3+NVgQsHR2vt96IVIQ0L56HWHDsB5Z1qz3PnkpTOHW32rcjwf2CGKJcfWa5j1iFlidbX1Ztzx\\nNmn7uDZ5crJGaZrYwcF2BcazD35CKQRx5MiwQDoeZcBZQ+LoMzE1F1TpAT44HahsbY3IJ8kmPoxI\\ncwTjo0dSYAwpeLR51ba+nzSMXB3ldrooCjo2EyjmC6z9MCGQG3jtImAytTSlMNiSqnpJvkSIjLbs\\ntAm9SgyLqQIDAQABo4GjMIGgMB0GA1UdDgQWBBR12nbSeneEed3FST/aht0IiqrWBjBeBgNVHSME\\nVzBVgBR12nbSeneEed3FST/aht0IiqrWBqE5pDcwNTELMAkGA1UEBhMCVVMxDTALBgNVBAoMBFRl\\nc3QxFzAVBgNVBAMMDmhhcHB5Ym94LjQ1NjM3ggIQADAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB\\n/wQEAwIBBjANBgkqhkiG9w0BAQUFAAOCAQEAzQrHzdPdKYrl51iW3ETjx9OSNhwrj2/JWde2T1XG\\nCYX4kAFvlU8MRZrQAwll5hHkbIbCXxGr5wpgLWQGPbQ44n4rAdsvfGZoF7SIy31ihjhDB4OmSlG3\\n2U1FnisjPdsGioW8iu3TokRTpxVSIcYlyDUd4pWBncrclCCqpxAlm/K+lD2LgDgXIYUKp0DfVvUx\\n8NQMpXincdvCnmdyWPEG84rlHIRA5l/Qw+LsX6/HSzbL+4xEOLDDBa82WXsSyiUwf9URLwPY4cWQ\\nkK5G3KKKn5RQwjXZV2/OVXIikuX8aJopjOrBFtDrIW7P0tyT4diGRmDE1oUpBa2ZoO9tCfUqIQ==\\n-----END CERTIFICATE-----\\n";
    private static final String HOST_SUBJECT = "O=Test,CN=192.168.122.229";

    public static Stream<Pair<String, String>> mockEngineLocalConfiguration() {
        return Stream.of(
                new Pair<>("ENGINE_PKI_TRUST_STORE", TrustStoreTestUtils.getTrustStorePath()),
                new Pair<>("ENGINE_HTTPS_PKI_TRUST_STORE", TrustStoreTestUtils.getTrustStorePath()),
                new Pair<>("ENGINE_FQDN", "engine-host"),
                new Pair<>("ENGINE_PROXY_ENABLED", "false"),
                new Pair<>("ENGINE_HTTPS_PORT", "8443"),
                new Pair<>("ENGINE_PROXY_HTTPS_PORT", "443")
        );
    }

    @Mock
    BackendInternal backend;

    @Mock
    SessionDataContainer sessionDataContainer;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.concat(
                AbstractQueryTest.mockConfiguration(),
                Stream.of(
                        MockConfigDescriptor.of(ConfigValues.ConsoleToggleFullScreenKeys, "shift+f11"),
                        MockConfigDescriptor.of(ConfigValues.ConsoleReleaseCursorKeys, "shift+f12"),
                        MockConfigDescriptor.of(ConfigValues.RemapCtrlAltDelDefault, true),
                        MockConfigDescriptor.of(ConfigValues.FullScreenWebadminDefault, false),
                        MockConfigDescriptor.of(ConfigValues.EnableSpiceRootCertificateValidation, true)
                )
        );
    }

    @Test
    public void shouldFailtWhenNoId() {
        when(getQueryParameters().getOptions()).thenReturn(new ConsoleOptions(GraphicsType.SPICE));
        assertFalse(getQuery().validateInputs());
    }

    @Test
    public void shouldFailtWhenNoGraphicsType() {
        when(getQueryParameters().getOptions()).thenReturn(new ConsoleOptions());
        assertFalse(getQuery().validateInputs());
    }

    @Test
    public void testInputDataOk() {
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.SPICE));
        doReturn(mockVm(GraphicsType.SPICE)).when(getQuery()).getCachedVm();
        assertTrue(getQuery().validateInputs());
    }

    @Test
    public void failOnStoppedVm() {
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.SPICE));
        VM mockVm = mockVm(GraphicsType.SPICE);
        mockVm.setStatus(VMStatus.Down);
        doReturn(mockVm).when(getQuery()).getCachedVm();

        getQuery().validateInputs();
        assertFalse(getQuery().getQueryReturnValue().getSucceeded());
    }

    @Test
    public void failGetSpiceOnVncVm() {
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.SPICE));
        VM mockVm = mockVm(GraphicsType.VNC);
        doReturn(mockVm).when(getQuery()).getCachedVm();
        getQuery().validateInputs();
        assertFalse(getQuery().getQueryReturnValue().getSucceeded());
    }

    private ConsoleOptions getValidOptions(GraphicsType graphicsType) {
        ConsoleOptions options = new ConsoleOptions(graphicsType);
        options.setVmId(Guid.Empty);
        return options;
    }

    @Test
    public void shouldCallSetTicket() {
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.VNC));
        when(getQueryParameters().isSetTicket()).thenReturn(true);
        mockSessionDataContainer();
        mockGetCaCertificate();
        doReturn(mockVm(GraphicsType.VNC)).when(getQuery()).getCachedVm();

        ActionReturnValue result = new ActionReturnValue();
        result.setSucceeded(true);
        result.setActionReturnValue("nbusr123");
        doReturn(result).when(backend).runAction(eq(ActionType.SetVmTicket), any());
        doReturn(null).when(getQuery()).getHost();
        doReturn(false).when(getQuery()).isKernelFips();
        doReturn(false).when(getQuery()).isVncEncryptionEnabled();

        getQuery().getQueryReturnValue().setSucceeded(true);
        getQuery().executeQueryCommand();
        verify(backend, times(1)).runAction(eq(ActionType.SetVmTicket), any());
    }

    @Test
    public void shouldFillUseSsl() {
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.VNC));
        when(getQueryParameters().isSetTicket()).thenReturn(true);
        mockSessionDataContainer();
        mockGetCaCertificate();
        doReturn(mockVm(GraphicsType.VNC)).when(getQuery()).getCachedVm();

        ActionReturnValue result = new ActionReturnValue();
        result.setSucceeded(true);
        result.setActionReturnValue("nbusr123");
        doReturn(result).when(backend).runAction(eq(ActionType.SetVmTicket), any());

        VdsDynamic vds = new VdsDynamic();
        vds.setVncEncryptionEnabled(true);
        doReturn(vds).when(getQuery()).getHost();
        doReturn(false).when(getQuery()).isKernelFips();
        doReturn(false).when(getQuery()).isVncEncryptionEnabled();

        getQuery().getQueryReturnValue().setSucceeded(true);
        getQuery().executeQueryCommand();
        ConsoleOptions options = getQuery().getQueryReturnValue().getReturnValue();
        assertTrue(options.isUseSsl());
    }

    @Test
    public void shouldFillUsernameInFipsMode() {
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.VNC));
        when(getQueryParameters().isSetTicket()).thenReturn(true);
        mockSessionDataContainer();
        mockGetCaCertificate();
        doReturn(mockVm(GraphicsType.VNC)).when(getQuery()).getCachedVm();

        ActionReturnValue result = new ActionReturnValue();
        result.setSucceeded(true);
        result.setActionReturnValue("nbusr123");
        doReturn(result).when(backend).runAction(eq(ActionType.SetVmTicket), any());

        VDS vds = new VDS();
        vds.setHostName("test.host.name");
        QueryReturnValue qrv = new QueryReturnValue();
        qrv.setReturnValue(vds);
        doReturn(qrv).when(backend).runInternalQuery(eq(QueryType.GetVdsByName), any());

        VdsDynamic vdsDynamic = new VdsDynamic();
        vdsDynamic.setVncEncryptionEnabled(true);
        doReturn(vdsDynamic).when(getQuery()).getHost();
        doReturn(true).when(getQuery()).isKernelFips();

        getQuery().getQueryReturnValue().setSucceeded(true);
        getQuery().executeQueryCommand();
        ConsoleOptions options = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("vnc-00000000-0000-0000-0000-000000000000", options.getUsername());
    }

    @Test
    @MockedConfig("spiceRelatedConfig")
    public void failWhenCertEnforcedAndCANotFound() {
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.SPICE));
        mockSessionDataContainer();
        mockGetVdsCertificateSubjectByVmId();
        doReturn(mockVm(GraphicsType.SPICE)).when(getQuery()).getCachedVm();

        QueryReturnValue caResult = new QueryReturnValue();
        caResult.setSucceeded(false);
        doReturn(caResult).when(backend).runInternalQuery(eq(QueryType.GetCACertificate), any());

        getQuery().getQueryReturnValue().setSucceeded(true);
        getQuery().executeQueryCommand();
        assertFalse(getQuery().getQueryReturnValue().getSucceeded());
    }

    public static Stream<MockConfigDescriptor<?>> spiceRelatedConfig() {
        return Stream.concat(mockConfiguration(), Stream.of(
                MockConfigDescriptor.of(ConfigValues.SSLEnabled, false),
                MockConfigDescriptor.of(ConfigValues.EnableUSBAsDefault, false)
                )
        );
    }

    @Test
    public void fillRemoteViewerUrl_nothingToReplace() {
        testFillRemoteViewerUrl(
                "some",
                "s",
                "o",
                "some"
        );
    }

    @Test
    public void fillRemoteViewerUrl_replaceBaseUrl_invalidUrlNotReplaced() {
        testFillRemoteViewerUrl(
                "some" + ConfigureConsoleOptionsQuery.ENGINE_BASE_URL + "other",
                " replaced ",
                "o",
                "some" + ConfigureConsoleOptionsQuery.ENGINE_BASE_URL + "other"
        );
    }

    @Test
    public void fillRemoteViewerUrl_replaceBaseUrl() {
        testFillRemoteViewerUrl(
                "some" + ConfigureConsoleOptionsQuery.ENGINE_BASE_URL + "other",
                "http://www.ovirt.org/a/b",
                "",
                "some" + "http://www.ovirt.org/a/b" + "other"
        );
    }

    @Test
    public void fillRemoteViewerUrl_absoluteUrl() {
        testFillRemoteViewerUrl(
                "some" + ConfigureConsoleOptionsQuery.CONSOLE_CLIENT_RESOURCES_URL + "other",
                "o",
                "http://www.ovirt.org/a/b",
                "some" + "http://www.ovirt.org/a/b" + "other"
        );
    }

    @Test
    public void fillRemoteViewerUrl_relativeUrl_baseUrlMalformed() {
        testFillRemoteViewerUrl(
                "some" + ConfigureConsoleOptionsQuery.CONSOLE_CLIENT_RESOURCES_URL + "other",
                "o s w",
                "b/c",
                "some" + "b/c" + "other"
        );
    }

    @Test
    public void fillRemoteViewerUrl_relativeUrl_baseUrlCorrect() {
        testFillRemoteViewerUrl(
                "some" + ConfigureConsoleOptionsQuery.CONSOLE_CLIENT_RESOURCES_URL + "other",
                "http://www.ovirt.org",
                "b/c",
                "some" + "http://www.ovirt.org/b/c" + "other"
        );
    }

    @Test
    public void fillRemoteViewerUrl_relativeUrl_baseUrlCorrect_slashsAround() {
        testFillRemoteViewerUrl(
                "some" + ConfigureConsoleOptionsQuery.CONSOLE_CLIENT_RESOURCES_URL + "other",
                "http://www.ovirt.org/",
                "/b/c",
                "some" + "http://www.ovirt.org/b/c" + "other"
        );
    }

    private void testFillRemoteViewerUrl(String toRepalce, String baseUrl, String resourceUrl, String expected) {
        ConsoleOptions options = new ConsoleOptions();
        when(getQueryParameters().getOptions()).thenReturn(getValidOptions(GraphicsType.SPICE));
        getQuery().fillRemoteViewerUrl(
                options,
                toRepalce,
                baseUrl,
                resourceUrl
        );

        assertEquals(expected, options.getRemoteViewerNewerVersionUrl());
    }

    private VM mockVm(GraphicsType graphicsType) {
        VM vm = new VM();
        vm.setId(Guid.Empty);
        vm.getGraphicsInfos().put(graphicsType, new GraphicsInfo().setIp("host").setPort(5901));
        vm.setStatus(VMStatus.Up);
        vm.setRunOnVdsName("some.host");
        return vm;
    }

    void mockSessionDataContainer() {
        doReturn(SSO_TOKEN).when(sessionDataContainer).getSsoAccessToken(any());
    }

    void mockGetCaCertificate() {
        QueryReturnValue caCertificateReturnValue = new QueryReturnValue();
        caCertificateReturnValue.setSucceeded(true);
        caCertificateReturnValue.setReturnValue(CA_CERTIFICATE);
        doReturn(caCertificateReturnValue).when(backend)
                .runInternalQuery(eq(QueryType.GetCACertificate), any());
    }

    void mockGetVdsCertificateSubjectByVmId() {
        QueryReturnValue hostSubjectReturnValue = new QueryReturnValue();
        hostSubjectReturnValue.setReturnValue(HOST_SUBJECT);
        doReturn(hostSubjectReturnValue).when(backend)
                .runInternalQuery(eq(QueryType.GetVdsCertificateSubjectByVmId), any());
    }

}
