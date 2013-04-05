/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.build;

import org.apache.maven.artifact.Artifact;

public class Module {
    private String artifactId;
    private String groupId;
    private String moduleName;
    private String moduleSlot;

    public Module() {
        // Nothing.
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getModuleName() {
        if (moduleName != null) {
            return moduleName;
        }
        return groupId + "." + artifactId;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleSlot() {
        if (moduleSlot != null) {
            return moduleSlot;
        }
        return "main";
    }

    public void setModuleSlot(String moduleSlot) {
        this.moduleSlot = moduleSlot;
    }

    public String getResourcePath() {
        return artifactId + ".jar";
    }

    public boolean matches(Artifact artifact) {
        return artifact.getArtifactId().equals(artifactId) && artifact.getGroupId().equals(groupId);
    }
}

