package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class InstallerMessages {
    private VDS _vds;
    private String _correlationId;
    private int _sequence = 0;

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
        logable.AddCustomValue("Message", text);
        switch (severity) {
        case INFO:
            logType = AuditLogType.VDS_INSTALL_IN_PROGRESS;
            log.infoFormat("Installation {0}: {1}", _vds.getHostName(), text);
            break;
        default:
        case WARNING:
            logable.setCustomId(_sequence++);
            logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_WARNING;
            log.warnFormat("Installation {0}: {1}", _vds.getHostName(), text);
            break;
        case ERROR:
            logable.setCustomId(_sequence++);
            logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_ERROR;
            log.errorFormat("Installation {0}: {1}", _vds.getHostName(), text);
            break;
        }
        AuditLogDirector.log(logable, logType);
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
                    error = _internalPostOldXmlFormat(message);
                } catch (RuntimeException e) {
                    error = true;
                    log.errorFormat(
                        "Installation of Host. Received illegal XML from Host. Message: {0}",
                        message,
                        e
                    );
                }
            } else {
                log.info("VDS message: " + message);
            }
        }
        return error;
    }

    private boolean _internalPostOldXmlFormat(String message) {
        boolean error = false;
        XmlDocument doc = new XmlDocument();
        doc.LoadXml(message);
        XmlNode node = doc.ChildNodes[0];
        if (node != null) {
            StringBuilder sb = new StringBuilder();
            // check status
            Severity severity;
            if (node.Attributes.get("status") == null) {
                severity = Severity.WARNING;
            } else if (node.Attributes.get("status").getValue().equals("OK")) {
                severity = Severity.INFO;
            } else if (node.Attributes.get("status").getValue().equals("WARN")) {
                severity = Severity.WARNING;
            } else {
                error = true;
                severity = Severity.ERROR;
            }

            if ((node.Attributes.get("component") != null)
                    && (StringUtils.isNotEmpty(node.Attributes.get("component").getValue()))) {
                sb.append("Step: " + node.Attributes.get("component").getValue());
            }

            if ((node.Attributes.get("message") != null)
                    && (StringUtils.isNotEmpty(node.Attributes.get("message").getValue()))) {
                sb.append("; ");
                sb.append("Details: " + node.Attributes.get("message").getValue());
                sb.append(" ");
            }

            if ((node.Attributes.get("result") != null)
                    && (StringUtils.isNotEmpty(node.Attributes.get("result").getValue()))) {
                sb.append(" (" + node.Attributes.get("result").getValue() + ")");
            }
            post(severity, sb.toString());
        }

        return error;
    }

    private static final Log log = LogFactory.getLog(InstallerMessages.class);
}
