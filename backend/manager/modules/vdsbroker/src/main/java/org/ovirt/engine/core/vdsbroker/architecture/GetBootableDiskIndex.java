package org.ovirt.engine.core.vdsbroker.architecture;


import org.ovirt.engine.core.utils.archstrategy.ArchCommand;

public class GetBootableDiskIndex implements ArchCommand {
    private int diskIndex;
    private int numOfReservedScsiIndexes;

    public GetBootableDiskIndex(int numOfReservedScsiIndexes) {
        this.numOfReservedScsiIndexes = numOfReservedScsiIndexes;
    }

    /**
     * CDROM is mapped to IDE, the index of first boot disk is 0
     */
    @Override
    public void runForX86_64() {
        diskIndex = 0;
    }

    /**
     *  On PPC, the index 0 is reserved for CDROM and is mapped to sda.
     *  The boot disk is mapped one behind.
     */
    @Override
    public void runForPPC64() {
        diskIndex = numOfReservedScsiIndexes;
    }

    /**
     *  S390X boots from first disk
     */
    @Override
    public void runForS390X() {
        diskIndex = 0;
    }

    public int returnValue() {
        return diskIndex;
    }
}
