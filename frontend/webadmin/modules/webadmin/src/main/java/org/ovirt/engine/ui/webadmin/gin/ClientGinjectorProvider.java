package org.ovirt.engine.ui.webadmin.gin;

import com.google.gwt.core.client.GWT;

/**
 * Creates the {@link ClientGinjector} and provides access to its instance.
 * <p>
 * Accessing Ginjector this way is useful for non-managed components (application classes that don't participate in
 * dependency injection, such as custom widgets).
 */
public class ClientGinjectorProvider {

    private static final ClientGinjector ginjector = GWT.create(ClientGinjector.class);

    public static ClientGinjector instance() {
        return ginjector;
    }

}
