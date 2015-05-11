package org.ovirt.engine.core.bll.validator.network;

import java.util.List;

import org.ovirt.engine.core.common.errors.EngineMessage;

public interface NetworkExclusivenessValidator {

    boolean isNetworkExclusive(List<NetworkType> networksOnIface);

    EngineMessage getViolationMessage();
}
