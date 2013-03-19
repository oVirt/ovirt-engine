/**
 * TODO:
 * Commented out test class in order to cancel dependency on PowerMock
 * This should be revisited.
 */

//package org.ovirt.engine.core.utils.hostinstall;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//import static org.powermock.api.mockito.PowerMockito.when;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.Spy;
//import org.ovirt.engine.core.common.config.Config;
//import org.ovirt.engine.core.common.config.ConfigValues;
//import org.ovirt.engine.core.utils.FileUtil;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
///**
// * This test class only tests the logic inside the runCommandArray method, since the rest of the class is logic-free, or
// * externally dependent
// */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ Config.class, FileUtil.class })
//@PowerMockIgnore("org.apache.log4j.*")
//public class OpenSslCAWrapperTest {
//    static final int defaultSignCertTimeoutInSeconds = 30;
//    @Spy
//    OpenSslCAWrapper openSslWrapper = new OpenSslCAWrapper();
//    @Mock
//    Process processMock;
//    @Mock
//    Runtime runtimeMock;
//
//    // Static mock does not hold if done only in @BeforeClass, must be done before each test
//    @Before
//    public void setup() throws IOException {
//        MockitoAnnotations.initMocks(this);
//
//        // Mocking the Config class
//        mockStatic(Config.class);
//        when(Config.resolveSignScriptPath()).thenReturn(new String());
//        when(Config.GetValue(any(ConfigValues.class))).thenReturn(new String());
//        when(Config.GetValue(ConfigValues.SignCertTimeoutInSeconds)).thenReturn(defaultSignCertTimeoutInSeconds);
//        when(Config.resolveCABasePath()).thenReturn(new String());
//
//        // Mocking the org.ovirt.engine.core.utils FileUtil class
//        mockStatic(FileUtil.class);
//        when(FileUtil.fileExists(any(String.class))).thenReturn(true);
//
//        // Mocking the process and openSslWrapper
//        when(processMock.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
//        when(processMock.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
//        when(openSslWrapper.getRuntime()).thenReturn(runtimeMock);
//        when(runtimeMock.exec(any(String[].class))).thenReturn(processMock);
//    }
//
//    @Test
//    public void signCertificateRequestValidTest() {
//        processSucceeds();
//        assertTrue(execSignCertificateRequest());
//    }
//
//    @Test
//    public void signCertificateRequestBadExitValueTest() {
//        processFails();
//        assertFalse(execSignCertificateRequest());
//    }
//
//    @Test
//    public void signCertificateRequestTimeoutTest() {
//        processSucceeds();
//        // In order to cause a timeout, timeout is set to zero
//        when(Config.GetValue(ConfigValues.SignCertTimeoutInSeconds)).thenReturn(0);
//        assertFalse(execSignCertificateRequest());
//    }
//
//    private void processSucceeds() {
//        setExternalReturnValue(0);
//    }
//
//    private void processFails() {
//        setExternalReturnValue(1);
//    }
//
//    private void setExternalReturnValue(final int returnValue) {
//        when(processMock.exitValue()).thenReturn(returnValue);
//    }
//
//    private boolean execSignCertificateRequest() {
//        return openSslWrapper.signCertificateRequest(null, 0, null);
//    }
//}
