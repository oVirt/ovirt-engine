package org.ovirt.engine.ui.frontend.server.gwt;

import org.ovirt.engine.core.utils.branding.BrandingTheme.ApplicationType;

/**
 * UserPortal GWT application host page servlet.
 */
public class UserPortalHostPageServlet extends GwtDynamicHostPageServlet {

    private static final long serialVersionUID = -8713825482196759603L;

    @Override
    protected String getSelectorScriptName() {
        return "org.ovirt.engine.ui.userportal.UserPortal.nocache.js"; //$NON-NLS-1$
    }

    @Override
    protected boolean filterQueries() {
        return true;
    }

    @Override
    public ApplicationType getApplicationType() {
        return ApplicationType.USER_PORTAL;
    }
}
