package org.ovirt.engine.core.bll;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.AnsibleImageMeasureCommandParameters;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHttpClient;

@NonTransactiveCommandAttribute
public class AnsibleImageMeasureCommand <T extends AnsibleImageMeasureCommandParameters> extends AnsibleCommandBase<T> {
    public static final Pattern DISK_TARGET_SIZE_PATTERN = Pattern.compile("required size: ([0-9]+).*", Pattern.DOTALL);

    @Inject
    private AnsibleRunnerHttpClient runnerClient;

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

    private void pullOutput() {
        String output = getParameters().getStringBuilder().toString();
        Matcher matcher = DISK_TARGET_SIZE_PATTERN.matcher(output);
        if (!matcher.find()) {
            log.error("failed to measure image, output: {}", output);
            throw new EngineException(EngineError.GeneralException, "Failed to measure image");
        }
        getParameters().getDisks()
                .stream()
                .filter(disk -> disk.getId().equals(getParameters().getDiskId()))
                .findFirst().ifPresent(disk -> disk.setActualSizeInBytes(Long.parseLong(matcher.group(1))));
    }

    @Override
    protected BiConsumer<String, String> getEventUrlConsumer() {
        StringBuilder stdout = new StringBuilder();
        getParameters().setStringBuilder(stdout);
        return (eventName, eventUrl) -> stdout.append(runnerClient.getCommandStdout(eventUrl));
    }

    @Override
    public ActionReturnValue endAction() {
        pullOutput();
        return super.endAction();
    }
}
