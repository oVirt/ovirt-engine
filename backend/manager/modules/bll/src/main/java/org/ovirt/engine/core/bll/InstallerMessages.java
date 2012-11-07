package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class InstallerMessages {
    private Guid _vdsId;

    public InstallerMessages(Guid vdsId) {
        _vdsId = vdsId;
    }

    public boolean AddMessage(String message) {
        boolean error = false;
        if (StringUtils.isEmpty(message)) {
            return error;
        }
        String[] msgs = message.split("[\\n]", -1);
        if (msgs.length > 1) {
            for (String msg : msgs) {
                error = AddMessage(msg) || error;
            }
            return error;
        }

        if (StringUtils.isNotEmpty(message)) {
            if (message.charAt(0) == '<') {
                try {
                    error = parseMessage(message);
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

    private boolean parseMessage(String message) {
        boolean error = false;
        XmlDocument doc = new XmlDocument();
        doc.LoadXml(message);
        XmlNode node = doc.ChildNodes[0];
        if (node != null) {
            StringBuilder sb = new StringBuilder();
            // check status
            AuditLogType logType;
            if (node.Attributes.get("status") == null) {
                logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_WARNING;
            } else if (node.Attributes.get("status").getValue().equals("OK")) {
                logType = AuditLogType.VDS_INSTALL_IN_PROGRESS;
            } else if (node.Attributes.get("status").getValue().equals("WARN")) {
                logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_WARNING;
            } else {
                error = true;
                logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_ERROR;
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
            AuditLogableBase logable = new AuditLogableBase(_vdsId);
            logable.AddCustomValue("Message", StringUtils.stripEnd(sb.toString(), " "));
            AuditLogDirector.log(logable, logType);
        }

        return error;
    }

    private static Log log = LogFactory.getLog(InstallerMessages.class);
}
