package org.ovirt.engine.core.common.businessentities.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.reflections.Reflections;

public class DiskStorageTypeTest {
    @Test
    public void forClass() throws Exception {
        Reflections reflections = new Reflections(getClass().getPackage().getName());
        Set<Class<? extends Disk>> diskClasses = reflections.getSubTypesOf(Disk.class);
        Set<Class<? extends Disk>> concreteDiskClasses = new HashSet<>();
        Set<DiskStorageType> diskStorageTypes = EnumSet.noneOf(DiskStorageType.class);
        for (Class<? extends Disk> diskClass : diskClasses) {
            if (!Modifier.isAbstract(diskClass.getModifiers())) {
                DiskStorageType diskStorageType = DiskStorageType.forClass(diskClass);
                assertNotNull("No type for " + diskClass, diskStorageType);
                diskStorageTypes.add(diskStorageType);
                concreteDiskClasses.add(diskClass);
            }
        }
        assertEquals(concreteDiskClasses.size(), diskStorageTypes.size());
    }
}
