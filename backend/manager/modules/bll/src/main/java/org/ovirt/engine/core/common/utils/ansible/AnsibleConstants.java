/*
Copyright (c) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
     * This is name of file which executes the host-upgrade via Ansible.
     */
    public static final String HOST_UPGRADE_PLAYBOOK = "ovirt-host-upgrade.yml";

    /**
     * This is name of callback plugin which is used to run update in check mode.
     */
    public static final String HOST_UPGRADE_CALLBACK_PLUGIN = "hostupgradeplugin";

    /**
     * This is name of file which executes the host-remove via Ansible.
     */
    public static final String HOST_REMOVE_PLAYBOOK = "ovirt-host-remove.yml";

    /**
     * This is name of file which executes the ova-export via Ansible.
     */
    public static final String EXPORT_OVA_PLAYBOOK = "ovirt-ova-export.yml";

    /**
     * This is name of file which executes the ova-query via Ansible.
     */
    public static final String QUERY_OVA_PLAYBOOK = "ovirt-ova-query.yml";

    /**
     * This is name of file which executes the ova-import via Ansible.
     */
    public static final String IMPORT_OVA_PLAYBOOK = "ovirt-ova-import.yml";

    /**
     * This is name of callback plugin which is used to get OVF from an OVA.
     */
    public static final String OVA_QUERY_CALLBACK_PLUGIN = "ovaqueryplugin";
}
