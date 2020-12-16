package org.ovirt.engine.core.sso.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.sso.api.ClientInfo;
import org.ovirt.engine.core.sso.db.SsoDao;
import org.ovirt.engine.core.sso.utils.MissingClientIdCallThrottler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SsoClientsRegistry {
    public static final Logger logger = LoggerFactory.getLogger(SsoClientsRegistry.class);

    private final ConcurrentMap<String, ClientInfo> registry = new ConcurrentHashMap<>();

    @Inject
    private MissingClientIdCallThrottler throttler;

    @Inject
    private SsoDao ssoDao;

    public ClientInfo getClientInfo(String clientId) {
        return registry.computeIfAbsent(clientId,
                _id -> {
                    if (throttler.attemptToMakeACall(clientId)) {
                        return ssoDao.getSsoClientInfo(clientId);
                    } else {
                        logger.debug("Attempt to fetch missing client with id {} throttled", clientId);
                        return null;
                    }
                });
    }

    @PostConstruct
    public void loadRegistry() {
        registry.putAll(ssoDao.getAllSsoClientsInfo());
    }

    // for testing
    void setThrottler(MissingClientIdCallThrottler throttler) {
        this.throttler = throttler;
    }

    // for testing
    void setSsoDao(SsoDao ssoDao) {
        this.ssoDao = ssoDao;
    }
}
