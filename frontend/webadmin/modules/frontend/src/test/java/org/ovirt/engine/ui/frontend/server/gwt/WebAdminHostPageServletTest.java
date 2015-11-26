package org.ovirt.engine.ui.frontend.server.gwt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.ui.frontend.server.gwt.plugin.PluginData;

@RunWith(MockitoJUnitRunner.class)
public class WebAdminHostPageServletTest extends AbstractGwtDynamicHostPageServletTest<WebAdminHostPageServlet> {

    private static final String APPLICATION_MODE = "{ \"value\": \"123\" }"; //$NON-NLS-1$

    @ClassRule
    public static MockConfigRule mcr =
    new MockConfigRule(
            mockConfig(ConfigValues.UnsupportedLocalesFilterOverrides, new ArrayList<String>()),
            mockConfig(ConfigValues.DisplayUncaughtUIExceptions, Boolean.TRUE));

    @Mock
    private ObjectNode mockApplicationModeObject;

    // Cannot use @Mock since ArrayNode is final
    private ArrayNode mockPluginDefinitionsArray;

    @Before
    public void setUpMockRequest() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode mockPluginDef = mapper.createObjectNode();
        mockPluginDef.put("foo", "bar"); //$NON-NLS-1$ //$NON-NLS-2$
        mockPluginDefinitionsArray = mapper.createArrayNode();
        mockPluginDefinitionsArray.add(mockPluginDef);
        when(mockApplicationModeObject.toString()).thenReturn(APPLICATION_MODE);
        when(mockRequest.getAttribute(WebAdminHostPageServlet.ATTR_APPLICATION_MODE)).thenReturn(mockApplicationModeObject);
        when(mockRequest.getAttribute(WebAdminHostPageServlet.ATTR_PLUGIN_DEFS)).thenReturn(mockPluginDefinitionsArray);
        when(mockRequest.getAttribute(WebAdminHostPageServlet.ATTR_ENGINE_REPORTS_BASE_URL)).thenReturn("");
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
        testServlet.reportBaseUrl = "";
        testServlet.reportRedirectUrl = "";
        testServlet.reportRightClickRedirectUrl = "";
    }

    @Test
    public void testGetSelectorScriptName() {
        assertEquals(testServlet.getSelectorScriptName(), "webadmin.nocache.js"); //$NON-NLS-1$
    }

    @Test
    public void testFilterQueries() {
        assertFalse(testServlet.filterQueries());
    }

    @Test
    public void testDoGet_ExtraAttributes_WithoutUserInfoObject() throws IOException, ServletException {
        doReturn(mockApplicationModeObject).when(testServlet).getApplicationModeObject(any(Integer.class));
        doReturn(mockPluginDefinitionsArray).when(testServlet).getPluginDefinitionsArray(anyListOf(PluginData.class));
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
        ObjectNode result = testServlet.getApplicationModeObject(Integer.valueOf(255));
        assertEquals(result.get("value").asText(), "255"); //$NON-NLS-1$ //$NON-NLS-2$
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
        assertEquals(result.size(), mockDataCount);
        for (int i = 0; i < mockDataCount; i++) {
            JsonNode item = result.get(i);
            assertEquals(item.get("name").asText(), "name" + i); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals(item.get("url").asText(), "url" + i); //$NON-NLS-1$ //$NON-NLS-2$
            assertTrue(item.get("config") instanceof ObjectNode); //$NON-NLS-1$
            assertEquals(item.get("enabled").asBoolean(), true); //$NON-NLS-1$
        }
    }

}
