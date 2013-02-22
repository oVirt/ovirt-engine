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

