package org.ovirt.engine.ui.frontend.server.gwt.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.ServletUtils;

/**
 * Servlet that serves UI plugin static resources (files) from local file system, relative to
 * {@linkplain PluginDataManager#resolvePluginDataPath UI plugin data path}.
 * <p>
 * Note that this servlet <b>does not</b> {@linkplain PluginDataManager#reloadData reload} UI plugin
 * descriptor/configuration data as part of its request handling.
 */
public class PluginResourceServlet extends HttpServlet {

    private static final long serialVersionUID = -8657760074902262500L;

    private static final Logger logger = Logger.getLogger(PluginResourceServlet.class);

    private File baseDir;

    @Override
    public void init() throws ServletException {
        this.baseDir = new File(PluginDataManager.resolvePluginDataPath());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestPath = request.getPathInfo();
        String pluginName, requestFilePath;
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
            logger.error("Missing UI plugin name and/or relative file path for request [" + request.getRequestURI() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Fetch and validate plugin data
        PluginData pluginData = getPluginData(pluginName);
        if (pluginData == null) {
            logger.error("No data available for UI plugin [" + pluginName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (pluginData.getResourcePath() == null) {
            logger.error("Local resource path not specified for UI plugin [" + pluginName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Locate the requested file
        String filePath = pluginData.getResourcePath() + File.separator + requestFilePath;
        if (!ServletUtils.isSane(filePath)) {
            logger.error("Requested file path [" + filePath + "] is not sane"); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(baseDir, filePath);
        if (file.isDirectory()) {
            logger.error("Requested file path [" + filePath + "] denotes a directory instead of file"); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Send the content of the file
        response.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        String mimeType = ServletUtils.getMimeMap().getContentType(file);
        ServletUtils.sendFile(request, response, file, mimeType);
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
