package org.ovirt.engine.core.common.backendinterfaces;

import javax.ejb.Local;

import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

@Local
public interface IResourceManager {
    VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters);

    /**
     * A non-blocking way of invoking VDSM calls. return immediately with a {@link Future} holding the
     * {@link VDSReturnValue}. The caller can use the <code>isDone()</code> method to check the call is actually done
     * and use the Future's <code>get(long timeout, TimeUnit)</code> to wait a certain amount of time for the answer.
     *
     * @param commandType
     * @param parameters
     * @return {@link Future} holding the {@link VDSReturnValue}
     */
    FutureVDSCall<VDSReturnValue> runFutureVdsCommand(FutureVDSCommandType commandType,
            VdsIdVDSCommandParametersBase parameters);

    void setup();

}
