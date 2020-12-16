package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.function.BiConsumer;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AnsibleCommandParameters;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHttpClient;
import org.ovirt.engine.core.compat.CommandStatus;

public abstract class AnsibleCommandBase <T extends AnsibleCommandParameters> extends CommandBase<T> {

    @Inject
    private AnsibleExecutor ansibleExecutor;

    @Inject
    private AnsibleRunnerHttpClient runnerClient;

    @Inject
    @Typed(AnsibleCallback.class)
    private Instance<AnsibleCallback> callbackProvider;

    protected abstract AnsibleCommandConfig createCommand();
    protected abstract BiConsumer<String, String> getEventUrlConsumer();

    public AnsibleCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init(){
        super.init();
        setVdsId(getParameters().getHostId());
    }

    @Override
    protected void executeCommand() {
        AnsibleCommandConfig command = createCommand();
        AnsibleReturnValue ansibleReturnValue;

        BiConsumer<String, String> eventUrlConsumer = getEventUrlConsumer();
        if (eventUrlConsumer != null) {
            ansibleReturnValue = ansibleExecutor.runCommand(command, log, eventUrlConsumer, true);
        } else {
            ansibleReturnValue = ansibleExecutor.runCommand(command, true);
        }

        boolean succeeded = ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK;
        if (!succeeded) {
            setCommandStatus(CommandStatus.FAILED);
            throw new EngineException(EngineError.GeneralException);
        } else {
            getParameters().setPlayUuid(ansibleReturnValue.getPlayUuid());
            getParameters().setLogFile(ansibleReturnValue.getLogFile().toString());
            persistCommand(getParameters().getParentCommand(), true);
            setSucceeded(true);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
