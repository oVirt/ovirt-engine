package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * A landing servlet for WebAdmin project.
 *
 * @author Asaf Shakarchi
 */
public class WebadminDynamicHostingServlet extends GwtDynamicHostPageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getSelectorScriptName() {
        return "webadmin.nocache.js"; //$NON-NLS-1$
    }

    @Override
    protected void writeAdditionalJsData(HttpServletRequest request, PrintWriter writer) {
        super.writeAdditionalJsData(request, writer);

        Integer applicationMode = getApplicationMode(request);

        if (applicationMode != null) {
            Map<String, String> appModeData = new HashMap<String, String>();
            appModeData.put("value", String.valueOf(applicationMode)); //$NON-NLS-1$
            writeJsObject(writer, "applicationMode", appModeData); //$NON-NLS-1$
        }
    }

    private Integer getApplicationMode(HttpServletRequest request) {
        return (Integer) runPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ApplicationMode),
                request);
    }

}
