package org.ovirt.engine.core.bll;

import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AnsibleCommandParameters;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHTTPClient;

@NonTransactiveCommandAttribute
public class AnsibleImageMeasureCommand <T extends AnsibleCommandParameters> extends AnsibleCommandBase<T> {

    @Inject
    private AnsibleRunnerHTTPClient runnerClient;

    public AnsibleImageMeasureCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected AnsibleCommandConfig createCommand() {
        return new AnsibleCommandConfig()
                .hosts(getVds())
                .variable("image_path", getParameters().getVariables().get("image_path"))
                .playAction(getParameters().getPlayAction())
                // /var/log/ovirt-engine/ova/ovirt-image-measure-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory("ova")
                .logFilePrefix("ovirt-image-measure-ansible")
                .logFileName(getVds().getHostName())
                .playbook(AnsibleConstants.IMAGE_MEASURE_PLAYBOOK);
    }

    @Override
    protected BiConsumer<String, String> getEventUrlConsumer() {
        StringBuilder stdout = new StringBuilder();
        getParameters().setStringBuilder(stdout);
        return (eventName, eventUrl) -> stdout.append(runnerClient.getCommandStdout(eventUrl));
    }
}
