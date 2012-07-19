package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.node.ObjectNode;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.server.gwt.plugin.PluginData;
import org.ovirt.engine.ui.frontend.server.gwt.plugin.PluginDataManager;

/**
 * A landing servlet for WebAdmin project.
 * <p>
 * Note that this servlet <b>does</b> {@linkplain PluginDataManager#reloadData reload} UI plugin
 * descriptor/configuration data as part of its request handling.
 *
 * @author Asaf Shakarchi
 */
public class WebadminDynamicHostingServlet extends GwtDynamicHostPageServlet {

    private static final long serialVersionUID = -6393009825835028397L;

    @Override
    protected String getSelectorScriptName() {
        return "webadmin.nocache.js"; //$NON-NLS-1$
    }

    @Override
    protected void writeAdditionalJsData(HttpServletRequest request, PrintWriter writer) {
        super.writeAdditionalJsData(request, writer);
        writeApplicationMode(request, writer);
        writePluginDefinitions(writer);
    }

    private void writeApplicationMode(HttpServletRequest request, PrintWriter writer) {
        Integer applicationMode = getApplicationMode(request);

        if (applicationMode != null) {
            Map<String, String> appModeData = new HashMap<String, String>();
            appModeData.put("value", String.valueOf(applicationMode)); //$NON-NLS-1$
            writeJsObject(writer, "applicationMode", appModeData); //$NON-NLS-1$
        }
    }

    private Integer getApplicationMode(HttpServletRequest request) {
        return (Integer) runPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ApplicationMode,
                        Config.DefaultConfigurationVersion), request);
    }

    private void writePluginDefinitions(PrintWriter writer) {
        List<PluginData> currentData = new ArrayList<PluginData>(
                PluginDataManager.getInstance().reloadAndGetCurrentData());
        Collections.sort(currentData);

        StringBuilder sb = new StringBuilder();
        sb.append("var pluginDefinitions = [ "); //$NON-NLS-1$

        final String comma = ", "; //$NON-NLS-1$
        int countdown = currentData.size();
        for (PluginData data : currentData) {
            sb.append("{ "); //$NON-NLS-1$

            appendJsObjectAttribute(sb, "name", data.getName(), true); //$NON-NLS-1$
            sb.append(comma);

            appendJsObjectAttribute(sb, "url", data.getUrl(), true); //$NON-NLS-1$
            sb.append(comma);

            ObjectNode config = data.mergeConfiguration();
            appendJsObjectAttribute(sb, "config", config.toString(), false); //$NON-NLS-1$
            sb.append(comma);

            appendJsObjectAttribute(sb, "enabled", Boolean.toString(data.isEnabled()), false); //$NON-NLS-1$
            sb.append(comma);

            // Remove trailing comma
            sb.setLength(sb.length() - comma.length());

            sb.append(" }"); //$NON-NLS-1$
            if (--countdown > 0) {
                sb.append(comma);
            }
        }

        sb.append(" ];"); //$NON-NLS-1$
        writer.append(sb.toString());
    }

}
