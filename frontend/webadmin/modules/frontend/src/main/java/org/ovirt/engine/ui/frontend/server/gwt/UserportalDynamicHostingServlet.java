package org.ovirt.engine.ui.frontend.server.gwt;

import javax.servlet.http.HttpServletRequest;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/**
 * A landing servlet for UserPortal project.
 *
 * @author Asaf Shakarchi
 */
public class UserportalDynamicHostingServlet extends GwtDynamicHostPageServlet {

    private static final long serialVersionUID = -8713825482196759603L;

    @Override
    protected String getSelectorScriptName() {
        return "org.ovirt.engine.ui.userportal.UserPortal.nocache.js"; //$NON-NLS-1$
    }

    @Override
    protected void initQueryParams(VdcQueryParametersBase queryParams, HttpServletRequest request) {
        super.initQueryParams(queryParams, request);

        // All UserPortal queries are filtered according to user permissions
        queryParams.setFiltered(true);
    }

}
