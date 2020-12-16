package org.ovirt.engine.core.sso.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.sso.api.ClientInfo;
import org.ovirt.engine.core.sso.db.SsoDao;

@ApplicationScoped
public class SsoClientsRegistry {
    private final ConcurrentMap<String, ClientInfo> registry = new ConcurrentHashMap<>();

    @Inject
    private Instance<SsoDao> ssoDao;

    public ClientInfo getClientInfo(String clientId) {
        return registry.computeIfAbsent(clientId,
                _id -> {
                    // TODO limit to n calls per minute for clientId
                    return ssoDao.get().getSsoClientInfo(clientId);
                });
    }

    @PostConstruct
    public void loadRegistry() {
        registry.putAll(ssoDao.get().getAllSsoClientsInfo());
    }
}
