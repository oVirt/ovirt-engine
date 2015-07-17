package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterEditWarnings implements Serializable {

    /**
     * Represents group of warnings with:
     * <ul>
     *     <li>main message describing nature of given warning</li>
     *     <li>names of affected cluster entities (with optional details per name)</li>
     * </ul>
     */
    public static class Warning implements Serializable {

        private String mainMessage;
        private Map<String, String> detailsByName = new HashMap<>();

        private Warning() {
        }

        public Warning(String mainMessage) {
            this.mainMessage = mainMessage;
        }

        public String getMainMessage() {
            return mainMessage;
        }

        public Map<String, String> getDetailsByName() {
            return detailsByName;
        }

        public boolean isEmpty() {
            return detailsByName.isEmpty();
        }
    }

    private List<Warning> hostWarnings;
    private List<Warning> vmWarnings;

    public ClusterEditWarnings() {
        this(new ArrayList<Warning>(), new ArrayList<Warning>());
    }

    public ClusterEditWarnings(List<Warning> hostWarnings, List<Warning> vmWarnings) {
        this.hostWarnings = hostWarnings;
        this.vmWarnings = vmWarnings;
    }

    public List<Warning> getHostWarnings() {
        return hostWarnings;
    }

    public List<Warning> getVmWarnings() {
        return vmWarnings;
    }

    public boolean isEmpty() {
        return hostWarnings.isEmpty() && vmWarnings.isEmpty();
    }
}
