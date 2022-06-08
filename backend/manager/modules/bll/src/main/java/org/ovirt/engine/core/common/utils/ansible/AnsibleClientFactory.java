package org.ovirt.engine.core.common.utils.ansible;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnsibleClientFactory {

    @Inject
    private AnsibleCommandLogFileFactory ansibleCommandLogFileFactory;

    public AnsibleRunnerClient create(AnsibleCommandConfig command) {
        AnsibleRunnerLogger runnerLogger = ansibleCommandLogFileFactory.create(command);
        AnsibleRunnerClient client = new AnsibleRunnerClient();
        client.setLogger(runnerLogger);
        return client;
    }

}
