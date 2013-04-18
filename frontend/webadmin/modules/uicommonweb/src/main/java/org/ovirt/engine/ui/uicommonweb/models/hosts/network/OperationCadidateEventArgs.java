package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.ui.uicompat.EventArgs;

/**
 * Arguments for an OperationCadidateEvent, triggered when a new Network Operation Candidate is created
 */
public class OperationCadidateEventArgs extends EventArgs {

    private final NetworkOperation candidate;

    private final NetworkItemModel<?> op1;

    private final NetworkItemModel<?> op2;

    private final boolean drop;

    public OperationCadidateEventArgs(NetworkOperation candidate,
            NetworkItemModel<?> op1,
            NetworkItemModel<?> op2,
            boolean drop) {

        this.candidate = candidate;
        this.op1 = op1;
        this.op2 = op2;
        this.drop = drop;
    }

    public NetworkOperation getCandidate() {
        return candidate;
    }

    public NetworkItemModel<?> getOp1() {
        return op1;
    }

    public NetworkItemModel<?> getOp2() {
        return op2;
    }

    public boolean isDrop() {
        return drop;
    }

}
