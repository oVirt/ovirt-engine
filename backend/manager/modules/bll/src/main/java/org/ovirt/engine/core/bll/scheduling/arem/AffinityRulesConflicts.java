package org.ovirt.engine.core.bll.scheduling.arem;

public enum AffinityRulesConflicts {

    VM_TO_HOST_CONFLICT_IN_POSITIVE_AND_NEGATIVE_AFFINITY(
            "The affinity groups: {} , with hosts : {} and VMs : {} , have"
                    + " VM to host conflicts between positive and negative affinity groups"),
    VM_TO_HOST_CONFLICT_IN_ENFORCING_POSITIVE_AND_NEGATIVE_AFFINITY(
            "The affinity groups: {} , with hosts : {} and VMs : {} , have"
                    + " VM to host conflicts between positive and negative enforcing affinity groups", false),
    VM_TO_HOST_CONFLICTS_POSITIVE_VM_TO_VM_AFFINITY("The affinity groups: {} , with hosts : {} and VMs : {} , have"
            + " conflicts between VM to host affinity and VM to VM positive affinity"),
    VM_TO_HOST_CONFLICTS_NEGATIVE_VM_TO_VM_AFFINITY("The affinity groups: {} , with hosts : {} and VMs : {} , have"
            + " conflicts between VM to host affinity and VM to VM negative affinity"),
    NON_INTERSECTING_POSITIVE_HOSTS_AFFINITY_CONFLICTS("The affinity groups: {} , with hosts : {} and VMs : {} , have"
            + " non intersecting positive hosts conflicts"),
    VM_TO_VM_AFFINITY_CONFLICTS("Affinity Group collision detected in unified affinity group of VMs:{} and negative "
            + "affinity group: {} with VMs: {}", false);

    private String message;

    private boolean canBeSaved;

    AffinityRulesConflicts(final String message) {
        this.message = message;
        this.canBeSaved = true;
    }

    AffinityRulesConflicts(final String message, final boolean canBeSaved) {
        this.message = message;
        this.canBeSaved = canBeSaved;
    }

    public String getMessage() {
        return message;
    }

    public boolean canBeSaved() {
        return canBeSaved;
    }
}
