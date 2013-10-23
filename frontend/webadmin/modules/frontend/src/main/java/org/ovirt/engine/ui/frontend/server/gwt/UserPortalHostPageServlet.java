package org.ovirt.engine.ui.frontend.server.gwt;


/**
 * UserPortal GWT application host page servlet.
 */
public class UserPortalHostPageServlet extends GwtDynamicHostPageServlet {

    private static final long serialVersionUID = -8713825482196759603L;

    @Override
    protected String getSelectorScriptName() {
        return "userportal.nocache.js"; //$NON-NLS-1$
    }

    @Override
    protected boolean filterQueries() {
        return true;
    }
}
