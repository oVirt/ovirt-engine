package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogRecord;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.server.StackTraceDeobfuscator;
import com.google.gwt.logging.server.RemoteLoggingServiceUtil;
import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class OvirtRemoteLoggingService extends RemoteServiceServlet implements RemoteLoggingService {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 2063012772195038326L;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(OvirtRemoteLoggingService.class);

    /**
     * Init parameter key name.
     */
    private static final String APP_NAME = "applicationName"; //$NON-NLS-1$

    private StackTraceDeobfuscator deobfuscator;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String applicationName = getServletContext().getInitParameter(APP_NAME);
        if (applicationName == null) {
            throw new ServletException("Application name not specified"); //$NON-NLS-1$
        }

        Path symbolMapDirectory = EngineLocalConfig.getInstance().getUsrDir().toPath().resolve("gwt-symbols"); //$NON-NLS-1$
        Path symbolMapZipFile = symbolMapDirectory.resolve(applicationName + "/symbolMaps.zip"); //$NON-NLS-1$

        try {
            initDeobfuscator(FileSystems.newFileSystem(symbolMapZipFile, null));
        } catch (Exception e) {
            log.warn("Cannot read GWT symbol maps: " + symbolMapZipFile, e); //$NON-NLS-1$
        }
    }

    private void initDeobfuscator(FileSystem zipFs) {
        deobfuscator = new StackTraceDeobfuscator() {
            protected InputStream openInputStream(String fileName) throws IOException {
                return Files.newInputStream(zipFs.getPath("/" + fileName)); //$NON-NLS-1$
            }
        };
    }

    /**
     * Logs a {@link LogRecord} on the server.
     *
     * @return Either an error message, or null if logging was successful.
     */
    public final String logOnServer(LogRecord logRecord) {
        logOnServer(logRecord, getPermutationStrongName(), deobfuscator);
        // Return null to indicate that remote logging operation was successful.
        return null;
    }

    /**
     * Logs a potentially de-obfuscated {@link LogRecord}.
     *
     * @param logRecord The log record to log.
     * @param strongName GWT permutation strong name.
     * @param deobfuscator Stack trace de-obfuscator, can be null.
     */
    private void logOnServer(LogRecord logRecord, String strongName, StackTraceDeobfuscator deobfuscator) {
        if (deobfuscator != null) {
            logRecord = RemoteLoggingServiceUtil.deobfuscateLogRecord(deobfuscator, logRecord, strongName);
        }
        log.error("Permutation name: " + strongName); //$NON-NLS-1$
        log.error(logRecord.getMessage(), logRecord.getThrown());
    }

}
