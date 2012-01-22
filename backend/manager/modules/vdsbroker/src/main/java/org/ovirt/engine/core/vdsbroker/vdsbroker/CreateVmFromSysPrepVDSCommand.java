package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.utils.StringUtils;

public class CreateVmFromSysPrepVDSCommand<P extends CreateVmFromSysPrepVDSCommandParameters>
        extends CreateVDSCommand<P> {
    public CreateVmFromSysPrepVDSCommand(P parameters) {
        super(parameters);
        String strSysPrepContent = SysprepHandler.GetSysPrep(parameters.getVm(), parameters.getHostName(),
                    parameters.getDomain(), parameters.getSysPrepParams());

        if (!StringHelper.EqOp(strSysPrepContent, "")) {
            byte[] binarySysPrep = StringUtils.charsetDecodeStringUTF8(strSysPrepContent);
            createInfo.add(VdsProperties.sysprepInf, binarySysPrep);
        }
    }
}
