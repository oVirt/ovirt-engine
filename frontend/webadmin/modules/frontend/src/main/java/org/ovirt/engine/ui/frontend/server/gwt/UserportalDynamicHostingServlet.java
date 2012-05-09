package org.ovirt.engine.ui.frontend.server.gwt;

/**
 * A landing servlet for UserPortal project.
 *
 * @author Asaf Shakarchi
 */
public class UserportalDynamicHostingServlet extends GwtDynamicHostPageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getSelectorScriptName() {
        return "org.ovirt.engine.ui.userportal.UserPortal.nocache.js"; //$NON-NLS-1$
    }

    @Override
    protected boolean filterQueries() {
        return true;
    }

}
