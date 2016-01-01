package org.ovirt.engine.ui.frontend.server.gwt.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.servlet.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves UI plugin static resources from local file system, relative to
 * {@linkplain PluginDataManager#resolvePluginDataPath UI plugin data path}.
 * <p>
 * Note that this servlet <em>does not</em> {@linkplain PluginDataManager#reloadData reload} UI plugin
 * descriptor/configuration data as part of its request handling. To reload such data, the user must reload WebAdmin
 * application in web browser.
 */
public class PluginResourceServlet extends HttpServlet {

    private static final long serialVersionUID = -8657760074902262500L;

    private static final Logger log = LoggerFactory.getLogger(PluginResourceServlet.class);

    private File baseDir;

    @Override
    public void init() throws ServletException {
        this.baseDir = new File(PluginDataManager.resolvePluginDataPath());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestPath = request.getPathInfo();
        String pluginName;
        String requestFilePath;
        final String slash = "/"; //$NON-NLS-1$

        // Ensure non-null request path
        requestPath = requestPath == null ? slash : requestPath;

        // Remove leading '/' character(s)
        requestPath = StringUtils.stripStart(requestPath, slash);

        // Split plugin name from relative file path
        String[] parts = requestPath.split(slash, 2);
        if (parts.length == 2 && StringUtils.isNotBlank(parts[0]) && StringUtils.isNotBlank(parts[1])) {
            pluginName = parts[0];
            requestFilePath = parts[1];
        } else {
            log.error("Missing UI plugin name and/or relative file path for request '{}'", request.getRequestURI()); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Fetch and validate plugin data
        PluginData pluginData = getPluginData(pluginName);
        if (pluginData == null) {
            log.error("No data available for UI plugin '{}'", pluginName); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (pluginData.getResourcePath() == null) {
            log.error("Local resource path not specified for UI plugin '{}'", pluginName); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Locate the requested file
        String filePath = pluginData.getResourcePath() + File.separator + requestFilePath;
        if (!ServletUtils.isSane(filePath)) {
            log.error("Requested file path '{}' is not sane", filePath); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(baseDir, filePath);
        if (file.isDirectory()) {
            log.error("Requested file path '{}' denotes a directory instead of file", filePath); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Send the content of the file
        response.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        ServletUtils.sendFile(request, response, file, null);
    }

    private PluginData getPluginData(String pluginName) {
        Collection<PluginData> currentData = PluginDataManager.getInstance().getCurrentData();
        for (PluginData data : currentData) {
            if (data.getName().equals(pluginName)) {
                return data;
            }
        }
        return null;
    }

}
