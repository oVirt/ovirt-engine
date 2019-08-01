package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@Singleton
public class NetworkLocking {
    public Map<String, Pair<String, String>> getNetworkProviderLock(Guid providerId) {
        if (providerId == null) {
            return null;
        }

        return Collections.singletonMap(providerId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.PROVIDER,
                        new LockMessage(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_LOCKED)
                                .with("providerId", providerId.toString())));
    }
}
