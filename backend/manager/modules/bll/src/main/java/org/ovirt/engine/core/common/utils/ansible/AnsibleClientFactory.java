package org.ovirt.engine.core.common.utils.ansible;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnsibleClientFactory {

    @Inject
    private AnsibleCommandLogFileFactory ansibleCommandLogFileFactory;

    public AnsibleRunnerHTTPClient create(AnsibleCommandConfig command) {
        AnsibleRunnerLogger runnerLogger = ansibleCommandLogFileFactory.create(command);
        AnsibleRunnerHTTPClient client = new AnsibleRunnerHTTPClient();
        client.setLogger(runnerLogger);
        return client;
    }

}
