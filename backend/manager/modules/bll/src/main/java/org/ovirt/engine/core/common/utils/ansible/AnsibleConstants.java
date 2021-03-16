/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.common.utils.ansible;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * This file hold constants used for ansible execution.
 */
public class AnsibleConstants {

    /**
     * Path to host-deploy-post-tasks. User can create a tasks in this file to extend host-deploy process.
     */
    public static final Path HOST_DEPLOY_POST_TASKS_FILE_PATH = Paths.get(
        EngineLocalConfig.getInstance().getEtcDir().toString(),
        "ansible",
        "ovirt-host-deploy-post-tasks.yml"
    );

    /**
     * This is name of file which executes the host-deploy via Ansible.
     */
    public static final String HOST_DEPLOY_PLAYBOOK = "ovirt-host-deploy.yml";

    /**
     * This is name of file which executes fetches existing hosted engine configuration file via Ansible.
     */
    public static final String FETCH_HE_CONFIG_FILE_PLAYBOOK = "ovirt-fetch-he-config.yml";

    /**
     * This is name of file which executes the host-check-upgrade via Ansible.
     */
    public static final String HOST_CHECK_UPGRADE_PLAYBOOK = "ovirt-host-check-upgrade.yml";

    /**
     * This is name of file which executes the host-upgrade via Ansible.
     */
    public static final String HOST_UPGRADE_PLAYBOOK = "ovirt-host-upgrade.yml";

    /**
     * This is name of file which executes the host-enroll-certificate via Ansible.
     */
    public static final String HOST_ENROLL_CERTIFICATE = "ovirt-host-enroll-certificate.yml";

    /**
     * This is name of file which executes the host-remove via Ansible.
     */
    public static final String HOST_REMOVE_PLAYBOOK = "ovirt-host-remove.yml";

    /**
     * This is name of file which executes the ova-export via Ansible.
     */
    public static final String EXPORT_OVA_PLAYBOOK = "ovirt-ova-export.yml";

    /**
     * This is name of file which executes the image-measure via Ansible.
     */
    public static final String IMAGE_MEASURE_PLAYBOOK = "ovirt-image-measure.yml";

    /**
     * This is name of file which executes the ova-query via Ansible.
     */
    public static final String QUERY_OVA_PLAYBOOK = "ovirt-ova-query.yml";

    /**
     * This is name of file which executes the ova-import via Ansible.
     */
    public static final String IMPORT_OVA_PLAYBOOK = "ovirt-ova-import.yml";

    /**
     * This is name of file which executes the ova-external-data via Ansible.
     */
    public static final String OVA_EXTERNAL_DATA_PLAYBOOK = "ovirt-ova-external-data.yml";

    /**
     * This is name of file which executes the lvmcache via Ansible.
     */
    public static final String CREATE_BRICK_PLAYBOOK = "create-brick.yml";

    /**
     *  This is the name of file which executes Replace Gluster
     */
    public static final String REPLACE_GLUSTER_PLAYBOOK = "replace-gluster.yml";

    /**
     *  This is the name of file which executes host-fix-encrypted-migrations via Ansible
     */
    public static final String HOST_MIGRATION_CERTS = "ovirt-host-fix-encrypted-migrations.yml";

    /**
     * Name of the task where VDSM_ID is loaded, so we can distinguish this task and parse it.
     */
    public static final String TASK_VDSM_ID = "Fetch vdsm id";

    public static final String HOST_DEPLOY_LOG_DIRECTORY = "host-deploy";

    public static final Path ANSIBLE_RUNNER_SERVICE_LOG = Paths.get("/var/log/ovirt-engine/ansible-runner-service.log");
}
