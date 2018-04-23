package org.ovirt.engine.api.restapi.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;

public class OsTypeMockUtils {

    public static void mockOsTypes() {
        OsRepository osRepositoryMock = mock(OsRepository.class);
        Map<Integer, String> osNames = new HashMap<>(1);
        osNames.put(0, "Unassigned");
        when(osRepositoryMock.getUniqueOsNames()).thenReturn(osNames);

        Map<ArchitectureType, Integer> defaultOSes = new HashMap<>();
        defaultOSes.put(ArchitectureType.x86_64, 0);
        when(osRepositoryMock.getDefaultOSes()).thenReturn(defaultOSes);

        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepositoryMock);
    }
}
