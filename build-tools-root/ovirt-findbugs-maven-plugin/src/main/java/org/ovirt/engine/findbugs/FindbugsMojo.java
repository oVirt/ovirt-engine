/*
* Copyright (c) 2015 Red Hat, Inc.
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

package org.ovirt.engine.findbugs;

import static org.codehaus.plexus.util.xml.Xpp3DomUtils.mergeXpp3Dom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This mojo is intended to call the normal findbugs plugin with the multiple filter files that are used by the oVirt
 * Engine project.
 */
@Mojo(name = "findbugs", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@SuppressWarnings("unused")
public class FindbugsMojo extends AbstractMojo {
    // Inject a reference to the maven project:
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    // Inject a reference to the current maven session.
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    // Inject a reference to the maven plugin manager:
    @Component
    private BuildPluginManager pluginManager;

    /**
     * This parameters indicates the names of the global resources (in the classpath of the plugin) that will be
     * loaded as findbugs filters.
     */
    @Parameter
    private List<String> globalFilters;

    /**
     * This parameter indicates the names of the project files that will be loaded as findbugs filters.
     */
    @Parameter
    private List<String> projectFilters;

    /**
     * Executes the mojo. This will merge the multiple exclude filters used by the oVirt Engine project and then will
     * call the original findbugs plugin, with the merged filters.
     */
    public void execute() throws MojoExecutionException {
        // Load the general filters and the filters from the project that is currently being processed:
        List<NodeList> nodeLists = new ArrayList<>();
        if (globalFilters != null) {
            for (String path : globalFilters) {
                NodeList nodes = loadGlobalFilters(path);
                if (nodes != null) {
                    nodeLists.add(nodes);
                }
            }
        }
        if (projectFilters != null) {
            for (String path : projectFilters) {
                NodeList nodes = loadProjectFilters(path);
                if (nodes != null) {
                    nodeLists.add(nodes);
                }
            }
        }

        // Save all the loaded filters to a single file created in the target directory of the project:
        File filterFile = new File(project.getBasedir(), "target" + File.separator + "findbugs-filters.xml");
        getLog().info("Saving filters to file \"" + filterFile.getAbsolutePath() + "\"");
        saveNodes(filterFile, nodeLists);

        // Load the description of the findbugs plugin:
        Plugin findbugsPluginModel = new Plugin();
        findbugsPluginModel.setGroupId("org.codehaus.mojo");
        findbugsPluginModel.setArtifactId("findbugs-maven-plugin");
        findbugsPluginModel.setVersion("3.0.2");
        PluginDescriptor findbugsPluginDescriptor;
        try {
            // We need to use reflection here in order to support Maven 3.0 and Maven 3.1, see the comments in the
            // "invoke" method for details:
            Object remoteRepositories = invoke(project, "getRemotePluginRepositories");
            Object repositorySession = invoke(session, "getRepositorySession");
            findbugsPluginDescriptor = (PluginDescriptor) invoke(
                pluginManager,
                "loadPlugin",
                findbugsPluginModel,
                remoteRepositories,
                repositorySession
            );
        }
        catch (Exception exception) {
            throw new MojoExecutionException(
                "Can't load findbugs plugin \"" + findbugsPluginModel.getId() + "\"",
                exception
            );
        }
        getLog().info("Loaded the findbugs plugin \"" + findbugsPluginDescriptor.getId() + "\"");

        // Get the descriptor of the findbugs mojo:
        MojoDescriptor findbugsMojoDescriptor = findbugsPluginDescriptor.getMojo("findbugs");
        if (findbugsMojoDescriptor == null) {
            throw new MojoExecutionException(
                "Can't find findbugs mojo"
            );
        }
        getLog().info("Loaded the findbugs mojo \"" + findbugsMojoDescriptor.getId() + "\"");

        // Prepare the configuration for the findbugs mojo, including the reference to the filters file that has been
        // previously created:
        Xpp3Dom findbugsConfiguration = makeConfiguration(filterFile);

        // Merge the previously prepared configuration with the defaults:
        Xpp3Dom findbugsDefaults = toDom(findbugsMojoDescriptor.getMojoConfiguration());
        findbugsConfiguration = mergeXpp3Dom(findbugsConfiguration, findbugsDefaults);

        // Execute the findbugs plugin:
        MojoExecution findbugsExecution = new MojoExecution(findbugsMojoDescriptor, findbugsConfiguration);
        try {
            pluginManager.executeMojo(session, findbugsExecution);
        }
        catch (Exception exception) {
            throw new MojoExecutionException(
                "Execution of findbugs mojo failed",
                exception
            );
        }
    }

    /**
     * Saves a list XML nodes to a findbugs filters file.
     *
     * @param file the file where the XML document will be saved
     * @param nodeLists the nodes to save
     */
    private void saveNodes(File file, List<NodeList> nodeLists) throws MojoExecutionException {
        // Create an empty XML document:
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (Exception exception) {
            throw new MojoExecutionException(
                "Can't create XML document builder",
                exception
            );
        }
        Document document = builder.newDocument();

        // Add the root element to the document:
        Element root = document.createElement("FindBugsFilter");
        document.appendChild(root);

        // Ad all the filters to as children of the root element:
        for (NodeList nodeList : nodeLists) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Node imported = document.importNode(node, true);
                root.appendChild(imported);
            }
        }

        // Write the XML document to the file:
        File directory = file.getParentFile();
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new MojoExecutionException(
                    "Can't create directory \"" + directory.getAbsolutePath() + "\""
                );
            }
        }
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        }
        catch (TransformerConfigurationException exception) {
            throw new MojoExecutionException(
                "Can't create XML transformer",
                exception
            );
        }
        Source in = new DOMSource(document);
        Result out = new StreamResult(file);
        try {
            transformer.transform(in, out);
        }
        catch (TransformerException exception) {
            throw new MojoExecutionException(
                "Can't save filters to file \"" + file.getAbsolutePath() + "\"",
                exception
            );
        }
    }

    /**
     * Loads the findbugs filters stored in the given resource of this plugin.
     */
    private NodeList loadGlobalFilters(String path) throws MojoExecutionException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                getLog().warn("The filters resource \"" + path + "\" doesn't exist");
                return null;
            }
            getLog().info("Loading filters resource \"" + path + "\"");
            return loadNodes(in);
        }
        catch (IOException exception) {
            throw new MojoExecutionException(
                "Error loading filters resource \"" + path + "\"",
                exception
            );
        }
    }

    /**
     * Loads the findbugs filters stored in the {@code exclude-general.xml} file inside the base directory of the
     * project being processed.
     */
    private NodeList loadProjectFilters(String path) throws MojoExecutionException {
        File file = new File(project.getBasedir(), path);
        if (!file.exists()) {
            getLog().info("The filters file \"" + file.getAbsolutePath() + "\" doesn't exist");
            return null;
        }
        getLog().info("Loading filters file \"" + file.getAbsolutePath() + "\"");
        try (InputStream in = new FileInputStream(file)) {
            return loadNodes(in);
        }
        catch (IOException exception) {
            throw new MojoExecutionException(
                "Error loading filters file \"" + file.getAbsolutePath() + "\"",
                exception
            );
        }
    }

    /**
     * Parses the XML document from the given stream and returns all its child elements.
     *
     * @param in the input stream to read the XML document from
     * @return the list of children of the root element of the document
     */
    private NodeList loadNodes(InputStream in) throws MojoExecutionException {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (Exception exception) {
            throw new MojoExecutionException("Can't create XML document builder", exception);
        }
        Document document;
        try {
            document = builder.parse(in);
        }
        catch (Exception exception) {
            throw new MojoExecutionException("Can't parse XML document", exception);
        }
        return document.getDocumentElement().getChildNodes();
    }

    /**
     * Creates the custom configuration that will be used to execute the findbugs plugin.
     *
     * @param filterFile the file containing the filters
     * @return a XPP3 DOM tree containing the configuration
     */
    private Xpp3Dom makeConfiguration(File filterFile) {
        Xpp3Dom configuration = new Xpp3Dom("configuration");

        Xpp3Dom xmlOutput = new Xpp3Dom("xmlOutput");
        xmlOutput.setValue("true");
        configuration.addChild(xmlOutput);

        Xpp3Dom xmlOutputDirectory = new Xpp3Dom("xmlOutputDirectory");
        xmlOutputDirectory.setValue("target/site");
        configuration.addChild(xmlOutputDirectory);

        if (filterFile != null) {
            Xpp3Dom excludeFilterFile = new Xpp3Dom("excludeFilterFile");
            excludeFilterFile.setValue(filterFile.getAbsolutePath());
            configuration.addChild(excludeFilterFile);
        }

        return configuration;
    }

    /**
     * Converts the a Plexus configuration into an XPP3 DOM tree.
     *
     * @param configuration the Plexus configuration to convert
     * @return the XPP3 DOM tree
     */
    private Xpp3Dom toDom(PlexusConfiguration configuration) {
        Xpp3Dom dom = new Xpp3Dom(configuration.getName());
        dom.setValue(configuration.getValue(null));
        for (String name : configuration.getAttributeNames()) {
            dom.setAttribute(name, configuration.getAttribute(name));
        }
        for (PlexusConfiguration child : configuration.getChildren()) {
            dom.addChild(toDom(child));
        }
        return dom;
    }

    /**
     * Maven 3.1 changed the <i>aether</i> library it uses, from {@code org.sonatype.aether} to
     * {@code org.eclipse.aether}. As a result code that calls methods of that library contains references to types
     * that aren't resolvable with the new library. In order to avoid those references, and thus work in both 3.0 and
     * 3.1, we need to use reflection for those calls, so no references to the classes will be stored in the our own
     * {@code .class} files. This hack should be removed once we force a version of Maven greater or equal than 3.1,
     * but that isn't currently possible because EL6 only provides 3.0.
     */
    private Object invoke(Object target, String name, Object... parameters) throws MojoExecutionException {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(name)) {
                    return method.invoke(target, parameters);
                }
            }
            throw new MojoExecutionException(
                "Can't find method \"" + name + "\" in class \"" + target.getClass().getName() + "\"."
            );
        }
        catch (Exception exception) {
            throw new MojoExecutionException(
                "Error calling method \"" + name + "\" in class \"" + target.getClass().getName() + "\"",
                exception
            );
        }
    }
}
