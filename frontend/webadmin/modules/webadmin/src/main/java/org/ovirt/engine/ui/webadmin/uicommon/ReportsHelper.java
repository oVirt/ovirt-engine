package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class ReportsHelper {

    private static final ReportsHelper INSTANCE = new ReportsHelper();
    private final Map<String, Resource> resourceMap =
            new HashMap<String, Resource>();

    public static ReportsHelper getInstance() {
        return INSTANCE;
    }

    public void init() {
        parseReportsXML();
    }

    public ReportsHelper() {
    }

    private Resource getResource(String type) {
        return resourceMap.get(type);
    }

    private void parseReportsXML() {
        // parse the XML document into a DOM
        Document messageDom =
                XMLParser.parse(ClientGinjectorProvider.instance().getApplicationResources().reportsXml().getText());

        Element reportsElement = (Element) messageDom.getElementsByTagName("reports").item(0);
        NodeList resourcesNodeList = reportsElement.getElementsByTagName("resource");

        initResources(resourcesNodeList);
    }

    private void initResources(NodeList nodeList) {
        Node resourceNode;
        int i = 0;

        // initialize the resources
        while ((resourceNode = nodeList.item(i)) != null) {
            Element resourceElement = ((Element) resourceNode);
            Resource resource =
                    new Resource(resourceElement.getAttribute("type"),
                            resourceElement.getAttribute("id"),
                            Boolean.parseBoolean(resourceElement.getAttribute("multiple")));

            // initialize resource categories
            addCategories2Resource(resourceElement, resource);

            resourceMap.put(resource.getType(), resource);

            i++;
        }
    }

    private void addCategories2Resource(Element resourceElement, Resource resource) {

        NodeList nodeList = resourceElement.getElementsByTagName("category");
        Node node;
        int i = 0;

        while ((node = nodeList.item(i)) != null) {
            Element categoryElement = ((Element) node);
            Category category = new Category(categoryElement.getAttribute("name"));

            // initialize categories uri's
            addUris2Category(categoryElement, category);

            resource.addCategory(category);
            i++;
        }
    }

    private void addUris2Category(Element categoryElement, Category category) {

        NodeList nodeList = categoryElement.getElementsByTagName("uri");
        Node node;
        int i = 0;

        while ((node = nodeList.item(i)) != null) {
            Element uriElement = ((Element) node);
            URI uri =
                    new URI(uriElement.getAttribute("name"),
                            uriElement.getAttributes()
                                    .getNamedItem("description")
                                    .getNodeValue(),
                            uriElement.getFirstChild().getNodeValue(),
                            uriElement.getAttribute("id"),
                            Boolean.parseBoolean(uriElement.getAttribute("multiple")));

            category.addURI(uri);
            i++;
        }
    }

    private static class Resource {
        private final String type;

        private final List<Category> catergoriesList = new LinkedList<ReportsHelper.Category>();

        public Resource(String type, String id, boolean multiple) {
            super();
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public List<Category> getCatergoriesList() {
            return catergoriesList;
        }

        void addCategory(Category category) {
            catergoriesList.add(category);
        }
    }

    private static class Category {
        private final String name;
        private final List<URI> uriList = new LinkedList<ReportsHelper.URI>();

        public Category(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        void addURI(URI uri) {
            uriList.add(uri);
        }

        public List<URI> getUriList() {
            return uriList;
        }
    }

    private static class URI {
        private final String name;
        private final String description;
        private final String value;
        private final String id;
        private boolean multiple = false;

        public URI(String name, String description, String value, String id, boolean multiple) {
            super();
            this.name = name;
            this.description = description;
            this.value = value;
            this.id = id;
            this.multiple = multiple;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public String getId() {
            return id;
        }

        public boolean isMultiple() {
            return multiple;
        }
    }

    public <T> List<ActionButtonDefinition<T>> getResourceSubActions(String resourceType,
            final SearchableListModel model) {
        List<ActionButtonDefinition<T>> subActions = new LinkedList<ActionButtonDefinition<T>>();

        Resource resource = getResource(resourceType);
        if (resource != null) {
            for (Category category : resource.getCatergoriesList()) {
                List<ActionButtonDefinition<T>> categerySubActions =
                        getCategorySubActions(category, model);
                subActions.add(new WebAdminMenuBarButtonDefinition<T>(category.getName(), categerySubActions, true) {
                    @Override
                    public boolean isVisible(List<T> selectedItems) {
                        boolean isVisible = false;

                        for (ActionButtonDefinition<T> subAction : getSubActions()) {
                            if (subAction.isVisible(selectedItems)) {
                                return true;
                            }
                        }
                        return isVisible;
                    }
                });
            }
        }

        return subActions;
    }

    public <T> List<ActionButtonDefinition<T>> getCategorySubActions(final Category category,
            final SearchableListModel model) {
        List<ActionButtonDefinition<T>> subActions = new LinkedList<ActionButtonDefinition<T>>();

        for (final URI uri : category.getUriList()) {
            subActions.add(new WebAdminButtonDefinition<T>(uri.getName(), true, false, true, true, uri.getDescription()) {

                @Override
                public boolean isVisible(List<T> selectedItems) {
                    return isEnabled(selectedItems);
                }

                @Override
                protected UICommand resolveCommand() {

                    return model.addOpenReportCommand(uri.getId(), uri.isMultiple(), uri.getValue());
                }
            });
        }

        return subActions;
    }
}
