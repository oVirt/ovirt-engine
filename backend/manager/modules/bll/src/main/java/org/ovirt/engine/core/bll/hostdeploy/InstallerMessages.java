package org.ovirt.engine.core.bll.hostdeploy;

import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.uutils.xml.SecureDocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class InstallerMessages {
    private static final Logger log = LoggerFactory.getLogger(InstallerMessages.class);

    private VDS _vds;
    private String _correlationId;

    public enum Severity {
        INFO,
        WARNING,
        ERROR
    };

    public InstallerMessages(VDS vds) {
        _vds = vds;
    }

    public void setCorrelationId(String correlationId) {
        _correlationId = correlationId;
    }

    public void post(Severity severity, String text) {
        AuditLogType logType;
        AuditLogableBase logable = new AuditLogableBase(_vds.getId());
        logable.setCorrelationId(_correlationId);
        logable.addCustomValue("Message", text);
        switch (severity) {
        case INFO:
            logType = AuditLogType.VDS_INSTALL_IN_PROGRESS;
            log.info("Installation '{}': {}", _vds.getHostName(), text);
            break;
        default:
        case WARNING:
            logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_WARNING;
            log.warn("Installation '{}': {}", _vds.getHostName(), text);
            break;
        case ERROR:
            logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_ERROR;
            log.error("Installation '{}': {}", _vds.getHostName(), text);
            break;
        }
        new AuditLogDirector().log(logable, logType);
    }

    public boolean postOldXmlFormat(String message) {
        boolean error = false;
        if (StringUtils.isEmpty(message)) {
            return error;
        }
        String[] msgs = message.split("[\\n]", -1);
        if (msgs.length > 1) {
            for (String msg : msgs) {
                error = postOldXmlFormat(msg) || error;
            }
            return error;
        }

        if (StringUtils.isNotEmpty(message)) {
            if (message.charAt(0) == '<') {
                try {
                    error = internalPostOldXmlFormat(message);
                } catch (RuntimeException e) {
                    error = true;
                    log.error(
                        "Installation of Host. Received illegal XML from Host. Message: {}",
                        message);
                    log.debug("Exception", e);
                }
            } else {
                log.info("VDS message: {}", message);
            }
        }
        return error;
    }

    private boolean internalPostOldXmlFormat(String message) {
        boolean error = false;
        Document doc = null;
        try {
            doc = SecureDocumentBuilderFactory.newDocumentBuilderFactory().newDocumentBuilder().parse(new InputSource(new StringReader(message)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Element node = doc.getDocumentElement();
        if (node != null) {
            StringBuilder sb = new StringBuilder();
            // check status
            Severity severity;
            if (StringUtils.isEmpty(node.getAttribute("status"))) {
                severity = Severity.WARNING;
            } else if (node.getAttribute("status").equals("OK")) {
                severity = Severity.INFO;
            } else if (node.getAttribute("status").equals("WARN")) {
                severity = Severity.WARNING;
            } else {
                error = true;
                severity = Severity.ERROR;
            }

            if (StringUtils.isNotEmpty(node.getAttribute("component"))) {
                sb.append("Step: " + node.getAttribute("component"));
            }

            if (StringUtils.isNotEmpty(node.getAttribute("message"))) {
                sb.append("; ");
                sb.append("Details: " + node.getAttribute("message"));
                sb.append(" ");
            }

            if (StringUtils.isNotEmpty(node.getAttribute("result"))) {
                sb.append(" (" + node.getAttribute("result") + ")");
            }

            post(severity, sb.toString());
        }

        return error;
    }
}
