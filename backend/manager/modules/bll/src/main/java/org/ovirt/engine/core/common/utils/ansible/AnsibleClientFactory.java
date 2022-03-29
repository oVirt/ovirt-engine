package org.ovirt.engine.core.common.utils.ansible;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnsibleClientFactory {

    @Inject
    private AnsibleCommandLogFileFactory ansibleCommandLogFileFactory;

    public AnsibleRunnerHttpClient create(AnsibleCommandConfig command) {
        AnsibleRunnerLogger runnerLogger = ansibleCommandLogFileFactory.create(command);
        AnsibleRunnerHttpClient client = new AnsibleRunnerHttpClient();
        client.setLogger(runnerLogger);
        return client;
    }

}
