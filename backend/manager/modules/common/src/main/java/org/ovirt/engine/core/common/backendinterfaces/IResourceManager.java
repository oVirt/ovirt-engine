package org.ovirt.engine.core.common.backendinterfaces;

import javax.ejb.Local;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@Local
public interface IResourceManager {
    VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters);

    void setup();

}
