package org.ovirt.engine.ui.uicommonweb.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;

/**
 * Enum representing console protocol.
 * Console protocol is determined by it's backing class and priority (if a VM supports more than one protocol at the
 * same time, priority determines precedence of protocols).
 * Protocols with higher number have higher priority.
 */
public enum ConsoleProtocol {

    SPICE(SpiceConsoleModel.class, 3),
    VNC(VncConsoleModel.class, 2),
    RDP(RdpConsoleModel.class, 1);

    private final Class<? extends ConsoleModel> model;
    private final int priority;

    private ConsoleProtocol(Class<? extends ConsoleModel> model, int priority) {
        this.model = model;
        this.priority = priority;
    }

    public boolean isBackedBy(Class<? extends ConsoleModel> model) {
        return this.model.equals(model);
    }

    public static ConsoleProtocol getProtocolByModel(Class<? extends ConsoleModel> model) {
        for (ConsoleProtocol value : values()) {
            if (value.isBackedBy(model)) {
                return value;
            }
        }

        return null;
    }

    static class PriorityComparator implements Comparator<ConsoleProtocol>, Serializable {

        private static final long serialVersionUID = -4511422219352593185L;

        @Override
        public int compare(ConsoleProtocol fst, ConsoleProtocol snd) {
            if (fst == null && snd == null) {
                return 0;
            }
            if (fst == null) {
                return -1;
            }
            if (snd == null) {
                return 1;
            }
            return fst.priority - snd.priority;
        }

    }

    public static List<ConsoleProtocol> getProtocolsByPriority() {
        List<ConsoleProtocol> consoleProtocols = new ArrayList(Arrays.asList(ConsoleProtocol.values()));
        Collections.sort(consoleProtocols, new PriorityComparator());
        return consoleProtocols;
    }

    public Class getBackingClass() {
        return model;
    }
}
