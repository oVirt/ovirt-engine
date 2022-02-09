package org.ovirt.engine.core.bll;

import java.util.Map;
import java.util.function.BiConsumer;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AnsibleCommandParameters;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;

@NonTransactiveCommandAttribute
public class AnsiblePackOvaCommand <T extends AnsibleCommandParameters> extends AnsibleCommandBase<T> {
    private static final String CREATE_OVA_LOG_DIRECTORY = "ova";

    public AnsiblePackOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected AnsibleCommandConfig createCommand() {
        Map<String, Object> vars = getParameters().getVariables();
        return new AnsibleCommandConfig()
                .hosts(getVds())
                .variable("target_directory", vars.get("target_directory"))
                .variable("entity_type", vars.get("entity_type"))
                .variable("ova_size", vars.get("ova_size"))
                .variable("ova_name", vars.get("ova_name"))
                .variable("ovirt_ova_pack_ovf", vars.get("ovirt_ova_pack_ovf"))
                .variable("ovirt_ova_pack_disks", vars.get("ovirt_ova_pack_disks"))
                .variable("ovirt_ova_pack_tpm", vars.get("ovirt_ova_pack_tpm"))
                .variable("ovirt_ova_pack_nvram", vars.get("ovirt_ova_pack_nvram"))
                .variable("ovirt_ova_pack_padding", vars.get("ovirt_ova_pack_padding"))
                // /var/log/ovirt-engine/ova/ovirt-export-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(CREATE_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-export-ova-ansible")
                .logFileName(getVds().getHostName())
                .playAction("Pack OVA")
                .playbook(AnsibleConstants.EXPORT_OVA_PLAYBOOK);
    }

    @Override
    protected BiConsumer<String, String> getEventUrlConsumer() {
        return null;
    }
}
