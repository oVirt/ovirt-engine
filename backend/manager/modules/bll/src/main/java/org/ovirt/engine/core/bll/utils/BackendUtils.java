package org.ovirt.engine.core.bll.utils;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler;
import org.slf4j.Logger;

public class BackendUtils {
    public static final String BACKEND_COMMAND_OBJECTS_HANDLER_JNDI_NAME =
            "java:global/engine/bll/Backend!org.ovirt.engine.core.bll.interfaces.BackendCommandObjectsHandler";
    /**
     * This method should be used only at {@link BackendUtils} code for creating
     * and execution {@link BackendUtils} objects directly.
     * @return proxy object to create the {@link BackendUtils} objects and run them
     */
    public static BackendCommandObjectsHandler getBackendCommandObjectsHandler(Logger log) {
        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
            return (BackendCommandObjectsHandler) ctx.lookup(BACKEND_COMMAND_OBJECTS_HANDLER_JNDI_NAME);
        } catch (NamingException e) {
            log.error("Getting backend command objects handler failed: {}", e.getMessage());
            log.debug("Exception", e);
            return null;
        } finally {
            closeContext(log, ctx);
        }
    }

    public static void closeContext(Logger log, InitialContext ctx) {
        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch(NamingException e) {
            log.error("Error closing Context: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }
}
