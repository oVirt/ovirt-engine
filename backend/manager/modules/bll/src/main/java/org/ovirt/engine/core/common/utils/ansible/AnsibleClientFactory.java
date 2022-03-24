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
//        client.setUUID(command.getUuid());
//        client.setJobEvents(String.format("%1$s/artifacts/%2$s/job_events/", AnsibleConstants.HOST_DEPLOY_PROJECT_DIR, command.getUuid()));
        return client;
    }

}
