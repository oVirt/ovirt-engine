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
        "..",
        "ovirt-ansible-roles",
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
}
