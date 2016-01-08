package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.File;
import java.io.FilenameFilter;
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

    // No de-obfuscator by default
    private StackTraceDeobfuscator deobfuscator = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
      super.init(config);
      final String applicationName = getServletContext().getInitParameter(APP_NAME);
      if (applicationName == null) {
          throw new ServletException("Application name not specified"); //$NON-NLS-1$
      }
      File symbolMapDirectory = new File(EngineLocalConfig.getInstance().getUsrDir(),
              "/gwt-symbols/" + applicationName + "/symbolMaps"); //$NON-NLS-1$ $NON-NLS-2$
      boolean symbolMapsDirectoryExists = symbolMapDirectory.exists() && symbolMapDirectory.isDirectory();
      File[] files = symbolMapDirectory.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
              return name != null && name.toLowerCase().endsWith("symbolmap"); //$NON-NLS-1$
          }
      });
      if(!symbolMapsDirectoryExists || files == null || files.length == 0) {
          log.info("GWT symbolmaps are not installed, " //$NON-NLS-1$
                  + "please install them to de-obfuscate the UI stack traces"); //$NON-NLS-1$
      } else {
          //Only set the symbolMaps directory if it passed the tests.
          setSymbolMapsDirectory(symbolMapDirectory.getAbsolutePath()); //$NON-NLS-1$
      }
    }

    /**
     * Logs a Log Record which has been serialized using GWT RPC on the server.
     *
     * @return either an error message, or null if logging is successful.
     */
    public final String logOnServer(LogRecord logRecord) {
        logOnServer(logRecord, getPermutationStrongName(), deobfuscator);
        //Return something to satisfy the interface contract. consider returning de-obfuscated stack trace.
        return null;
    }

    /**
     * Log a potentially de-obfuscated Log Record.
     * @param logRecord The log record to log.
     * @param strongName The permutation name used to generate the obfuscated Log Record.
     * @param deobfuscator The de-obfuscated, can be null.
     */
    private void logOnServer(LogRecord logRecord, String strongName, StackTraceDeobfuscator deobfuscator) {
        if (deobfuscator != null) {
            logRecord = RemoteLoggingServiceUtil.deobfuscateLogRecord(deobfuscator, logRecord, strongName);
        }
        log.error("Permutation name: " + strongName); //$NON-NLS-1$
        log.error(logRecord.getMessage(), logRecord.getThrown());
    }

    /**
     * Set the file system location one can find the symbol maps at.
     * @param symbolMapsDir The directory the symbol maps can be found at.
     */
    public void setSymbolMapsDirectory(String symbolMapsDir) {
        deobfuscator = StackTraceDeobfuscator.fromFileSystem(symbolMapsDir);
    }
}
