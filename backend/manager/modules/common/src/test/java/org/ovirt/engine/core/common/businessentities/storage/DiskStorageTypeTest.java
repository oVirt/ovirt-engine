package org.ovirt.engine.core.common.businessentities.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

public class DiskStorageTypeTest {
    @Test
    public void forClass() {
        Reflections reflections = new Reflections(getClass().getPackage().getName());
        Set<Class<? extends Disk>> diskClasses = reflections.getSubTypesOf(Disk.class);
        Set<Class<? extends Disk>> concreteDiskClasses = new HashSet<>();
        Set<DiskStorageType> diskStorageTypes = EnumSet.noneOf(DiskStorageType.class);
        for (Class<? extends Disk> diskClass : diskClasses) {
            if (!Modifier.isAbstract(diskClass.getModifiers())) {
                DiskStorageType diskStorageType = DiskStorageType.forClass(diskClass);
                assertNotNull(diskStorageType, "No type for " + diskClass);
                diskStorageTypes.add(diskStorageType);
                concreteDiskClasses.add(diskClass);
            }
        }
        assertEquals(concreteDiskClasses.size(), diskStorageTypes.size());
    }
}
