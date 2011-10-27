package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.*;

public class InstallerMessages {
    private Guid _vdsId;

    public InstallerMessages(Guid vdsId) {
        _vdsId = vdsId;
    }

    public void AddMessage(String message) {
        if (StringHelper.isNullOrEmpty(message)) {
            return;
        }
        String[] msgs = message.split("[\\n]", -1);
        if (msgs.length > 1) {
            for (String msg : msgs) {
                AddMessage(msg);
            }
            return;
        }

        if (!StringHelper.isNullOrEmpty(message)) {
            if (message.charAt(0) == '<') {
                try {
                    parseMessage(message);
                } catch (RuntimeException e) {
                    log.errorFormat(
                            "Installation of Host. Received illegal XML from Host. Message: {1}, Exception: {2}",
                            message, e.toString());
                }
            } else {
                log.info("VDS message: " + message);
            }
        }
    }

    private void parseMessage(String message) {
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
                logType = AuditLogType.VDS_INSTALL_IN_PROGRESS_ERROR;
            }

            if ((node.Attributes.get("component") != null)
                    && (!StringHelper.isNullOrEmpty(node.Attributes.get("component").getValue()))) {
                sb.append("Step: " + node.Attributes.get("component").getValue());
            }

            if ((node.Attributes.get("message") != null)
                    && (!StringHelper.isNullOrEmpty(node.Attributes.get("message").getValue()))) {
                sb.append("; ");
                sb.append("Details: " + node.Attributes.get("message").getValue());
                sb.append(" ");
            }

            if ((node.Attributes.get("result") != null)
                    && (!StringHelper.isNullOrEmpty(node.Attributes.get("result").getValue()))) {
                sb.append(" (" + node.Attributes.get("result").getValue() + ")");
            }
            AuditLogableBase logable = new AuditLogableBase(_vdsId);
            logable.AddCustomValue("Message", StringHelper.trimEnd(sb.toString(), ' '));
            AuditLogDirector.log(logable, logType);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(InstallerMessages.class);
}
