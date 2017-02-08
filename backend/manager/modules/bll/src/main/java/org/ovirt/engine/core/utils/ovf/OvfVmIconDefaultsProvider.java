package org.ovirt.engine.core.utils.ovf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.dao.VmIconDefaultDao;

@Singleton
public class OvfVmIconDefaultsProvider {
    @Inject
    private VmIconDefaultDao vmIconDefaultDao;

    public Map<Integer, VmIconIdSizePair> getVmIconDefaults() {
        final Map<Integer, VmIconIdSizePair> result = new HashMap<>();
        final List<VmIconDefault> iconDefaults = vmIconDefaultDao.getAll();
        for (VmIconDefault iconDefault : iconDefaults) {
            result.put(iconDefault.getOsId(),
                    new VmIconIdSizePair(iconDefault.getSmallIconId(), iconDefault.getLargeIconId()));
        }
        if (!result.containsKey(OsRepository.DEFAULT_X86_OS)) {
            throw new EngineException(EngineError.DefaultIconPairNotFound);
        }
        return result;
    }
}
