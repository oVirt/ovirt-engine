package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class AutoRecoveryManagerTest {

    @InjectMocks
    private AutoRecoveryManager manager;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.AutoRecoveryAllowedTypes, new HashMap<>()));
    }

    @Mock
    private BackendInternal backendMock;

    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @Mock
    private InterfaceDao interfaceDaoMock;

    @Mock
    private NetworkDao networkDaoMock;

    // Entities needing recovery
    private List<VDS> vdss = new ArrayList<>();
    private List<StorageDomain> storageDomains = new ArrayList<>();

    @BeforeEach
    public void setup() {
        final VDS vds = new VDS();
        vdss.add(vds);
        when(vdsDaoMock.listFailedAutorecoverables()).thenReturn(vdss);

        StorageDomain domain = new StorageDomain();
        domain.setStoragePoolId(Guid.newGuid());
        storageDomains.add(domain);
        when(storageDomainDaoMock.listFailedAutorecoverables()).thenReturn(storageDomains);
    }

    @Test
    public void onTimerFullConfig() {
        Config.<Map<String, String>> getValue(ConfigValues.AutoRecoveryAllowedTypes).put("storage domains",
                Boolean.TRUE.toString());
        Config.<Map<String, String>> getValue(ConfigValues.AutoRecoveryAllowedTypes).put("hosts",
                Boolean.TRUE.toString());
        manager.recover();
        verify(backendMock, times(vdss.size())).runInternalAction(eq(ActionType.ActivateVds), any());
        verify(backendMock, times(storageDomains.size())).runInternalAction(eq(ActionType.ConnectDomainToStorage), any());
    }

    @Test
    public void onTimerFalseConfig() {
        Config.<Map<String, String>> getValue(ConfigValues.AutoRecoveryAllowedTypes).put("storage domains",
                Boolean.FALSE.toString());
        Config.<Map<String, String>> getValue(ConfigValues.AutoRecoveryAllowedTypes).put("hosts",
                Boolean.FALSE.toString());
        manager.recover();
        verify(backendMock, never()).runInternalAction(eq(ActionType.ActivateVds), any());
        verify(backendMock, never()).runInternalAction(eq(ActionType.ConnectDomainToStorage), any());
    }
}
