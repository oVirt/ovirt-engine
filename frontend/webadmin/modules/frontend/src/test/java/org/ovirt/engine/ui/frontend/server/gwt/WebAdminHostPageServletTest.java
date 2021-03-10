package org.ovirt.engine.ui.frontend.server.gwt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.ui.frontend.server.gwt.plugin.PluginData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class WebAdminHostPageServletTest extends AbstractGwtDynamicHostPageServletTest<WebAdminHostPageServlet> {

    private static final String APPLICATION_MODE = "{ \"value\": \"123\" }"; //$NON-NLS-1$

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.DisplayUncaughtUIExceptions, true));
    }

    @Mock
    private ObjectNode mockApplicationModeObject;

    @Mock
    private ArrayNode mockPluginDefinitionsArray;

    @BeforeEach
    public void setUpMockRequest() {
        when(mockApplicationModeObject.toString()).thenReturn(APPLICATION_MODE);
        when(mockRequest.getAttribute(WebAdminHostPageServlet.ATTR_APPLICATION_MODE)).thenReturn(mockApplicationModeObject);
        when(mockRequest.getAttribute(WebAdminHostPageServlet.ATTR_PLUGIN_DEFS)).thenReturn(mockPluginDefinitionsArray);
    }

    @Override
    protected WebAdminHostPageServlet getTestServletSpy() {
        return spy(new WebAdminHostPageServlet());
    }

    @Override
    protected void setUpTestServlet() throws NoSuchAlgorithmException {
        super.setUpTestServlet();
        // Avoid touching PluginDataManager via getPluginData method
        doReturn(new ArrayList<PluginData>()).when(testServlet).getPluginData();
    }

    @Test
    public void testGetSelectorScriptName() {
        assertEquals("webadmin.nocache.js", testServlet.getSelectorScriptName()); //$NON-NLS-1$
    }

    @Test
    public void testFilterQueries() {
        assertFalse(testServlet.filterQueries());
    }

    @Test
    public void testDoGet_ExtraAttributes_WithoutUserInfoObject() throws IOException, ServletException {
        doReturn(mockApplicationModeObject).when(testServlet).getApplicationModeObject(any());
        doReturn(mockPluginDefinitionsArray).when(testServlet).getPluginDefinitionsArray(any());
        testServlet.doGet(mockRequest, mockResponse);
        verify(mockRequest).setAttribute(WebAdminHostPageServlet.ATTR_APPLICATION_MODE, mockApplicationModeObject);
        verify(mockRequest).setAttribute(WebAdminHostPageServlet.ATTR_PLUGIN_DEFS, mockPluginDefinitionsArray);
    }

    @Test
    public void testGetMd5Digest_WithExtraObjects_WithoutUserInfoObject() throws NoSuchAlgorithmException,
        UnsupportedEncodingException {
        MessageDigest result = testServlet.getMd5Digest(mockRequest);
        assertEquals(result, mockDigest);
        verify(mockDigest, atLeast(3)).update(byteArrayCaptor.capture());
        assertArrayEquals(SELECTOR_SCRIPT.getBytes(StandardCharsets.UTF_8), byteArrayCaptor.getAllValues().get(0));
        assertArrayEquals(APPLICATION_MODE.getBytes(StandardCharsets.UTF_8), byteArrayCaptor.getAllValues().get(1));
        assertArrayEquals(mockPluginDefinitionsArray.toString().getBytes(StandardCharsets.UTF_8), byteArrayCaptor.getAllValues().get(2));
    }

    @Test
    public void testGetApplicationModeObject() {
        ObjectNode result = testServlet.getApplicationModeObject(255);
        assertEquals("255", result.get("value").asText()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetPluginDefinitionsArray() {
        int mockDataCount = 10;
        List<PluginData> pluginData = new ArrayList<>();
        for (int i = 0; i < mockDataCount; i++) {
            PluginData mockData = mock(PluginData.class);
            when(mockData.getName()).thenReturn("name" + i); //$NON-NLS-1$
            when(mockData.getUrl()).thenReturn("url" + i); //$NON-NLS-1$
            when(mockData.mergeConfiguration()).thenReturn(mock(ObjectNode.class));
            when(mockData.isEnabled()).thenReturn(true);
            pluginData.add(mockData);
        }
        ArrayNode result = testServlet.getPluginDefinitionsArray(pluginData);
        assertEquals(mockDataCount, result.size());
        for (int i = 0; i < mockDataCount; i++) {
            JsonNode item = result.get(i);
            assertEquals(item.get("name").asText(), "name" + i); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals(item.get("url").asText(), "url" + i); //$NON-NLS-1$ //$NON-NLS-2$
            assertTrue(item.get("config") instanceof ObjectNode); //$NON-NLS-1$
            assertTrue(item.get("enabled").asBoolean()); //$NON-NLS-1$
        }
    }

}
