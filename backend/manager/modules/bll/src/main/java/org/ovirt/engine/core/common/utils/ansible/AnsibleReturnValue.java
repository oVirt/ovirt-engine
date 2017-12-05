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

/**
 * A class returned by AnsibleExecutor::runCommand. It stores the return code of the ansible-playbook execution
 * and it may store the stdout of the execution.
 */
public class AnsibleReturnValue {

    /**
     * Is set to return code which is returned by ansible-playbook command.
     */
    private AnsibleReturnCode ansibleReturnCode;

    /**
     * Stdout of playbook execution. It is set only if user is using custom stdout callback plugin.
     */
    private String stdout;

    public AnsibleReturnValue(AnsibleReturnCode ansibleReturnCode) {
        this(ansibleReturnCode, null);
    }

    public AnsibleReturnValue(AnsibleReturnCode ansibleReturnCode, String stdout) {
        this.ansibleReturnCode = ansibleReturnCode;
        this.stdout = stdout;
    }

    public AnsibleReturnCode getAnsibleReturnCode() {
        return ansibleReturnCode;
    }

    public String getStdout() {
        return stdout;
    }

    public void setAnsibleReturnCode(AnsibleReturnCode ansibleReturnCode) {
        this.ansibleReturnCode = ansibleReturnCode;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
}
