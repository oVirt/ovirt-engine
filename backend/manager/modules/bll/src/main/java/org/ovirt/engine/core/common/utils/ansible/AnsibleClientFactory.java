package org.ovirt.engine.core.common.utils.ansible;

import javax.inject.Singleton;

@Singleton
public class AnsibleClientFactory {

    public AnsibleRunnerClient create(AnsibleCommandConfig command) {
        AnsibleRunnerClient client = new AnsibleRunnerClient();
        return client;
    }

}
