package org.ovirt.engine.core.common.scheduling;

import org.ovirt.engine.core.common.businessentities.VM;

public interface VmOverheadCalculator {
    /**
     * Return the total amount of RAM required to run the given VM.
     * It includes VM size, expected QEMU overhead and other memory taken from
     * the system by running the VM (such as page tables).
     *
     * Please note the return value is just an estimation of the memory
     * requirements, The actual amount of RAM required may be larger or smaller.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    int getTotalRequiredMemMb(VM vm);

    /**
     * Return the total amount of RAM required to run the given VM, ignoring
     * memory needed for huge pages.
     * It includes VM size without huge pages, expected QEMU overhead
     * and other memory taken from the system by running the VM (such as page tables).
     *
     * Please note the return value is just an estimation of the memory
     * requirements, The actual amount of RAM required may be larger or smaller.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    int getTotalRequiredMemWithoutHugePagesMb(VM vm);

    /**
     * Return the total amount of RAM required to run the given VM, ignoring
     * memory needed for huge pages.
     * It includes VM size without huge pages, expected QEMU overhead
     * and other memory taken from the system by running the VM (such as page tables).
     *
     * Please note the return value is just an estimation of the memory
     * requirements, The actual amount of RAM required may be larger or smaller.
     *
     * @param vm the relevant VM
     * @param numOfCpus number of CPUs
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    int getTotalRequiredMemWithoutHugePagesMb(VM vm, int numOfCpus);

    /**
     * Get the total expected memory overhead, including the expected QEMU
     * overhead and other memory taken from the system by running the VM
     * (such as page tables).
     *
     * Please note the return value is just an estimation of the memory
     * requirements, The actual amount of RAM required may be larger or smaller.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    int getOverheadInMb(VM vm);

    /**
     * Get the total expected memory overhead, including the expected QEMU
     * overhead and other memory taken from the system by running the VM
     * (such as page tables).
     *
     * Please note the return value is just an estimation of the memory
     * requirements, The actual amount of RAM required may be larger or smaller.
     *
     * @param vm the relevant VM
     * @param numOfCpus number of CPUs
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    int getOverheadInMb(VM vm, int numOfCpus);

    /**
     * Get the size of possible memory overhead. This represents memory
     * that might be allocated by QEMU, but is not used immediately.
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     */
    int getPossibleOverheadInMb(VM vm);

    /**
     * Get the size of possible memory overhead. This represents memory
     * that might be allocated by QEMU, but is not used immediately.
     *
     * @param vm the relevant VM
     * @param numOfCpus number of CPUs
     * @return required amount of memory in MiB
     */
    int getPossibleOverheadInMb(VM vm, int numOfCpus);

    /**
     * Get the memory overhead QEMU imposes on the VM immediately.
     * The value contains the needed memory size for page tables and
     * memory hotplug structures + expected fixed overhead (shared libraries
     * and internal QEMU structures).
     *
     * @param vm the relevant VM
     * @return required amount of memory in MiB
     * @throws RuntimeException
     *             thrown in case the cluster architecture cannot be identified
     */
    int getStaticOverheadInMb(VM vm);

    /**
     * Returns the required size for saving all the memory used by this VM.
     * It is useful for determining the size to be allocated in storage
     * when hibernating the VM or taking a snapshot with memory.
     *
     * @param vm The VM to compute the memory size for.
     * @return Memory size for allocation in bytes.
     */
    long getSnapshotMemorySizeInBytes(VM vm);
}
