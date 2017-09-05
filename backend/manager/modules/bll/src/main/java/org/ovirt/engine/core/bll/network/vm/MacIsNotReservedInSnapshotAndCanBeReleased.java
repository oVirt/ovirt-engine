package org.ovirt.engine.core.bll.network.vm;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.bll.snapshots.CountMacUsageDifference;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNic;

public class MacIsNotReservedInSnapshotAndCanBeReleased {
    public boolean macCanBeReleased(String macOfNicBeingRemoved, VM vm, CountMacUsageDifference countMacUsageDifference) {
        if (!isRunningStatelessVm(vm)) {
            return true;
        }

        return macCanBeReleased(macOfNicBeingRemoved, countMacUsageDifference);
    }

    public boolean macCanBeReleased(String macOfNicBeingRemoved, VM vm, SnapshotsManager snapshotsManager) {
        if (!isRunningStatelessVm(vm)) {
            return true;
        }

        CountMacUsageDifference countMacUsageDifference = new CountMacUsageDifference(
                snapshotsManager.macsInStatelessSnapshot(vm.getId()),
                macsWhichShouldExistAfterCommandIsFinished(macOfNicBeingRemoved, vm));

        return macCanBeReleased(macOfNicBeingRemoved, countMacUsageDifference);


    }

    /**
     * @param macOfNicBeingRemoved mac of NIC being removed.
     * @param countMacUsageDifference MAC usage difference between original snapshot and system state after remove
     * operation
     *
     * This can be really confusing, so to explain:
     *
     * if not running as stateless, you can release mac you have in hand for release.
     *
     * If running stateless you can release mac, if usageDifference is 0. That means, that you have MAC to be
     * released, which is not used after operations in this command, and it is not used in original snapshot.
     * No one is using it, so it's possible to release it. If difference is negative, it means, that MAC is used
     * less in state after this command is done, but it's still used in original snapshot, so we have to keep it
     * reserved. If difference is positive, it means, that MAC is used less than before this command
     * (because we are trying to release this mac), but it is still used more than in original snapshot,
     * therefore we can safely release it.
     */
    private boolean macCanBeReleased(String macOfNicBeingRemoved, CountMacUsageDifference countMacUsageDifference) {
        return !(countMacUsageDifference.usageDifference(macOfNicBeingRemoved) < 0);
    }

    private boolean isRunningStatelessVm(VM vm) {
        return vm.isStateless() && vm.isRunning();
    }

    private Stream<String> macsWhichShouldExistAfterCommandIsFinished(String macOfNicBeingRemoved, VM vm) {
        List<String> currentMacs =
                vm.getInterfaces().stream().map(VmNic::getMacAddress).collect(Collectors.toList());

        //we cannot use filter here, because there can be duplicate mac addresses.
        currentMacs.remove(macOfNicBeingRemoved);

        return currentMacs.stream();
    }
}
