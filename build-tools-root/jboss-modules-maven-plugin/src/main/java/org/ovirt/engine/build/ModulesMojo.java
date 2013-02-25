package org.ovirt.engine.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;

@Mojo(name = "jboss-modules", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE)
public class ModulesMojo extends AbstractMojo {

    /**
     * The maven project.
     */
    @Component
    private MavenProject project;

    /**
     * The project helper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The name of the module.
     */
    @Parameter(property = "moduleName", required = false)
    private String moduleName;

    /**
     * The slot of the module.
     */
    @Parameter(property = "moduleSlot", required = false, defaultValue = "main")
    private String moduleSlot;

    /**
     * The list of modules to generate.
     */
    @Parameter(property = "modules")
    private List<Module> modules;

    /**
     * The temporary directory where modules will be stored.
     */
    private File modulesDir;

    public void execute() throws MojoExecutionException {
        // Make sure the list of modules is not empty:
        if (modules == null) {
            modules = new ArrayList<Module>(1);
        }

        // Populate the module map and slot map with the value for the
        // artifact of this project:
        if (modules.isEmpty()) {
            Module module = new Module();
            module.setArtifactId(project.getArtifactId());
            module.setGroupId(project.getGroupId());
            modules.add(module);
        }

        // Locate the target directory:
        File targetDir = new File(project.getBuild().getDirectory());

        // Create the modules directory in the temporary build directory:
        modulesDir = new File(targetDir, "modules");
        getLog().info("Creating modules directory \"" + modulesDir + "\"");
        if (!modulesDir.exists()) {
            if (!modulesDir.mkdirs()) {
                throw new MojoExecutionException(
                        "Can't create target modules directory \"" +
                         modulesDir.getAbsolutePath() + "\"");
            }
        }

        // Copy any content from the source modules directory to the modules
        // directory:
        String sourcePath = "src" + File.separator + "main" + File.separator + "modules";
        File sourceDir = new File(project.getBasedir(), sourcePath);
        getLog().info("Copying module resources to \"" + modulesDir + "\"");
        if (sourceDir.exists()) {
            try {
                FileUtils.copyDirectoryStructure(sourceDir,  modulesDir);
            }
            catch (IOException exception) {
                throw new MojoExecutionException(
                        "Can't copy source modules directory \"" + sourceDir.getAbsolutePath() + "\" " +
                        "to target modules directory \"" + modulesDir.getAbsolutePath() + "\"",
                        exception);
            }
        }

        // Generate the modules:
        for (Module module: modules) {
            createModule(module);
        }

        // Create the archive containing all the contents of the modules
        // directory:
        File modulesArchive = new File(targetDir, project.getBuild().getFinalName() + "-modules.zip");
        ZipArchiver modulesArchiver = new ZipArchiver();
        modulesArchiver.setDestFile(modulesArchive);
        modulesArchiver.addDirectory(modulesDir);
        getLog().info("Creating module archive \"" + modulesArchive + "\"");
        try {
            modulesArchiver.createArchive();
        }
        catch (Exception exception) {
            throw new MojoExecutionException(
                    "Can't generate modules archive \"" + modulesArchive.getAbsolutePath() + "\"",
                    exception);
        }

        // Attach the generated zip file containing the modules as an
        // additional artifact:
        getLog().info("Attaching modules artifact \"" + modulesArchive + "\"");
        projectHelper.attachArtifact(project, "zip", "modules", modulesArchive);
    }

    private void createModule(Module module) throws MojoExecutionException {
        // Create the slot directory:
        String modulePath = module.getModuleName().replace(".", File.separator);
        String slotPath = modulePath + File.separator + module.getModuleSlot();
        File slotDir = new File(modulesDir, slotPath);
        getLog().info("Creating slot directory \"" + slotDir + "\"");
        if (!slotDir.exists()) {
            if (!slotDir.mkdirs()) {
                throw new MojoExecutionException(
                        "Can't create module directory \"" +
                        slotDir.getAbsolutePath() + "\"");
            }
        }

        // Find the dependency with the same group and artifact id that the module:
        Artifact matchingArtifact = null;
        if (module.matches(project.getArtifact())) {
            matchingArtifact = project.getArtifact();
        }
        else {
            for (Artifact currentArtifact: project.getDependencyArtifacts()) {
                if (module.matches(currentArtifact)) {
                    matchingArtifact = currentArtifact;
               }
            }
        }
        if (matchingArtifact == null) {
            throw new MojoExecutionException(
                    "Can't find dependency matching artifact id \"" + module.getArtifactId() + "\" " +
                    "and group id \"" + module.getGroupId() + "\"");
        }

        // Copy the artifact to the slot directory:
        File artifactFrom = matchingArtifact.getFile();
        if (artifactFrom == null) {
            throw new MojoExecutionException(
                    "Can't find file for artifact id \"" + module.getArtifactId() + "\" " +
                    "and group id \"" + module.getGroupId() + "\"");
        }
        File artifactTo = new File(slotDir, module.getResourcePath());
        getLog().info("Copying artifact to \"" + artifactTo + "\"");
        try {
            FileUtils.copyFile(artifactFrom, artifactTo);
        }
        catch (IOException exception) {
            throw new MojoExecutionException(
                    "Can't copy artifact from \"" + artifactFrom.getAbsolutePath() + "\" " +
                    "to \"" + artifactTo.getAbsolutePath() + "\"",
                    exception);
        }
    }

}
