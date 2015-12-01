package org.ovirt.engine.api.restapi.utils;

import static org.easymock.EasyMock.expect;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;

public class OsTypeMockUtils {

    public static void mockOsTypes() {
        IMocksControl control = EasyMock.createNiceControl();
        OsRepository osRepositoryMock = control.createMock(OsRepository.class);
        HashMap<Integer, String> osNames = new HashMap<>(1);
        osNames.put(0, "Unassigned");
        expect(osRepositoryMock.getUniqueOsNames()).andReturn(osNames).anyTimes();

        HashMap<ArchitectureType, Integer> defaultOSes = new HashMap<>();
        defaultOSes.put(ArchitectureType.x86_64, 0);
        expect(osRepositoryMock.getDefaultOSes()).andReturn(defaultOSes);

        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepositoryMock);
        control.replay();
    }
}
