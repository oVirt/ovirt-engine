package org.ovirt.engine.core.bll;

import java.lang.reflect.Field;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ovirt.engine.core.common.osinfo.OsRepository;

public class VmHandlerRule extends TestWatcher {

    private CpuFlagsManagerHandler originalCpuFlagsManagerHandler;
    private boolean cpuFlagsManagerHandlerSet = false;

    private OsRepository originalOsRepository;
    private boolean osRepositorySet = false;

    @Override
    protected void finished(Description description) {
        super.finished(description);
        if (cpuFlagsManagerHandlerSet) {
            setCpuFlagsManagerHandler(originalCpuFlagsManagerHandler);
            cpuFlagsManagerHandlerSet = false;
        }
        if (osRepositorySet) {
            setOsRepository(originalOsRepository);
            osRepositorySet = false;
        }
    }

    private CpuFlagsManagerHandler setCpuFlagsManagerHandler(CpuFlagsManagerHandler cpuFlagsManagerHandler) {
        try {
            Field field = VmHandler.class.getDeclaredField("cpuFlagsManagerHandler");
            field.setAccessible(true);
            final CpuFlagsManagerHandler oldHandler = (CpuFlagsManagerHandler) field.get(VmHandler.class);
            field.set(VmHandler.class, cpuFlagsManagerHandler);
            return oldHandler;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCpuFlagsManagerHandler(CpuFlagsManagerHandler newHandler) {
        CpuFlagsManagerHandler oldHandler = setCpuFlagsManagerHandler(newHandler);
        if (!cpuFlagsManagerHandlerSet) {
            cpuFlagsManagerHandlerSet = true;
            originalCpuFlagsManagerHandler = oldHandler;
        }
    }

    private OsRepository setOsRepository(OsRepository osRepository) {
        try {
            Field field = VmHandler.class.getDeclaredField("osRepository");
            field.setAccessible(true);
            final OsRepository oldRepository = (OsRepository) field.get(VmHandler.class);
            field.set(VmHandler.class, osRepository);
            return oldRepository;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateOsRepository(OsRepository newOsRepository) {
        final OsRepository oldOsRepository = setOsRepository(newOsRepository);
        if (!cpuFlagsManagerHandlerSet) {
            osRepositorySet = true;
            originalOsRepository = oldOsRepository;
        }
    }
}
