package org.ovirt.engine.core;

import java.io.InputStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FixturesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private Document fixturesDocument;
    private XPathExpression tablesOfDatasetExpression;
    private XPathExpression columnsExpression;
    private XPathExpression rowExpression;

    @Before
    public void setUp() throws Exception {
        try (InputStream fixturesStream = FixturesTest.class.getResourceAsStream("/fixtures.xml")) {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            fixturesDocument = documentBuilder.parse(fixturesStream);

            XPath xPath = XPathFactory.newInstance().newXPath();

            tablesOfDatasetExpression = xPath.compile("/dataset/table");
            columnsExpression = xPath.compile("column");
            rowExpression = xPath.compile("row");
        }
    }

    @Test
    public void testValidColumnCount() throws Exception {
        NodeList tableNodes = getNodeListForXpath(fixturesDocument, tablesOfDatasetExpression);
        getNodeStream(tableNodes).forEach(this::verifyCorrectColumnCountInTable);
    }

    private void verifyCorrectColumnCountInTable(Node tableNode) {
        String tableName = tableNode.getAttributes().getNamedItem("name").getNodeValue();
        int definedNumberOfColumns = getDefinedNumberOfColumns(tableNode);

        NodeList rowNodes = getNodeListForXpath(tableNode, rowExpression);
        IntStream intStream = indexedStreamOverNodeList(rowNodes);

        intStream.forEach(i -> assertNumberOfColumns(definedNumberOfColumns, rowNodes.item(i), i, tableName));
    }

    private int getDefinedNumberOfColumns(Node tableNode) {
        NodeList columnNodes = getNodeListForXpath(tableNode, columnsExpression);
        return columnNodes.getLength();
    }

    private void assertNumberOfColumns(int expectedNumberOfColumns, Node rowNode, int rowIndex, String tableName) {
        int numberOfValueNodes = getNumberOfNonTextElements(rowNode);
        String message = String.format("Row #%d of table '%s' should have %d columns",
                rowIndex,
                tableName,
                expectedNumberOfColumns);

        errorCollector.checkThat(message, numberOfValueNodes, CoreMatchers.is(expectedNumberOfColumns));
    }

    private int getNumberOfNonTextElements(Node rowNode) {
        return (int)getNodeStream(rowNode.getChildNodes()).filter(e -> e.getNodeType() == Node.ELEMENT_NODE).count();
    }

    private Stream<Node> getNodeStream(NodeList rowNodes) {
        return indexedStreamOverNodeList(rowNodes).mapToObj(rowNodes::item);
    }

    private IntStream indexedStreamOverNodeList(NodeList rowNodes) {
        return IntStream.range(0, rowNodes.getLength());
    }

    private NodeList getNodeListForXpath(Node node, XPathExpression xpath) {
        try {
            return (NodeList) xpath.evaluate(node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
