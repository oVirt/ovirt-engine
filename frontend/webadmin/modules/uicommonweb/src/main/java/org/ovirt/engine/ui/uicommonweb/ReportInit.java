package org.ovirt.engine.ui.uicommonweb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

public class ReportInit {

    private static final ReportInit INSTANCE = new ReportInit();
    private boolean reportsEnabled = false;
    private boolean xmlInitialized = false;
    private boolean urlInitialized = false;
    private final Event reportsInitEvent = new Event("ReportsInitialize", ReportInit.class);
    private String reportBaseUrl = "";

    private final Map<String, Resource> resourceMap =
            new HashMap<String, Resource>();

    public static ReportInit getInstance() {
        return INSTANCE;
    }

    public void init() {
        AsyncDataProvider.GetRedirectServletReportsPage(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        setReportBaseUrl((String) returnValue);
                    }
                }));

        parseReportsXML();
    }

    private ReportInit() {
    }

    public Event getReportsInitEvent() {
        return reportsInitEvent;
    }

    public Resource getResource(String type) {
        return resourceMap.get(type);
    }

    private void parseReportsXML() {

        RequestBuilder requestBuilder =
                new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + "Reports.xml");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                    setXmlInitialized();
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    try {
                        // parse the XML document into a DOM
                        Document messageDom = XMLParser.parse(response.getText());

                        Element reportsElement = (Element) messageDom.getElementsByTagName("reports").item(0);
                        NodeList resourcesNodeList = reportsElement.getElementsByTagName("resource");

                        initResources(resourcesNodeList);
                    } catch (DOMParseException e) {
                    } finally {
                        setXmlInitialized();
                    }

                }
            });
        } catch (RequestException e) {
            setXmlInitialized();
        }
    }

    private void initResources(NodeList nodeList) {
        Node resourceNode;
        int i = 0;

        // initialize the resources
        while ((resourceNode = nodeList.item(i)) != null) {
            Element resourceElement = ((Element) resourceNode);
            Resource resource =
                    new Resource(resourceElement.getAttribute("type"));

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
                            uriElement.getAttribute("idParamName"),
                            Boolean.parseBoolean(uriElement.getAttribute("multiple")));

            category.addURI(uri);
            i++;
        }
    }

    public static class Resource {
        private final String type;

        private final List<Category> catergoriesList = new LinkedList<ReportInit.Category>();

        public Resource(String type) {
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

    public static class Category {
        private final String name;
        private final List<URI> uriList = new LinkedList<ReportInit.URI>();

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

    public static class URI {
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

    public boolean isReportsEnabled() {
        return reportsEnabled;
    }

    private void setReportsEnabled(boolean reportsEnabled) {
        this.reportsEnabled = reportsEnabled;
    }

    public void setReportBaseUrl(String reportBaseUrl) {
        this.reportBaseUrl = reportBaseUrl;
        this.urlInitialized = true;
        checkIfInitFinished();
    }

    public String getReportBaseUrl() {
        return reportBaseUrl;
    }

    private void setXmlInitialized() {
        this.xmlInitialized = true;
        checkIfInitFinished();
    }

    private void checkIfInitFinished() {
        if (xmlInitialized && urlInitialized) {

            // Check if the reports should be enabled in this system
            if (!reportBaseUrl.equals("") && !resourceMap.isEmpty()) {
                setReportsEnabled(true);
            } else {
                setReportsEnabled(false);
            }

            // The initialization process blocks on this event after the login
            reportsInitEvent.raise(this, null);
        }
    }

}
