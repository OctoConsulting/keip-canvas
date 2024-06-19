package com.octo.keip.flow;

import com.octo.keip.flow.model.EipId;
import com.octo.keip.flow.model.EipNode;
import com.octo.keip.flow.model.ConnectionType;
import com.octo.keip.flow.model.Role;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class TestStax {

  private static final Map<String, String> eipNamespaceToUri =
      Map.of("integration", "http://www.springframework.org/schema/integration");

  private static final String DEFAULT_NAMESPACE = "http://www.springframework.org/schema/beans";

  private static final Map<String, String> XML_NAMESPACES =
      Map.of(
          "xsi",
          "http://www.w3.org/2001/XMLSchema-instance",
          "int",
          "http://www.springframework.org/schema/integration",
          "jms",
          "http://www.springframework.org/schema/integration/jms");

  public static void main(String[] args) throws XMLStreamException, TransformerException {
    var eipNode =
        new EipNode(
            "n1",
            new EipId("integration", "adapter"),
            "testnode",
            null,
            Role.ENDPOINT,
            ConnectionType.SOURCE,
            Map.of("attr1", "testval"),
            null);

    var eventFactory = XMLEventFactory.newFactory();
    var outputFactory = XMLOutputFactory.newInstance();
    //    outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

    var outputStream = new ByteArrayOutputStream();
    var writer = outputFactory.createXMLEventWriter(outputStream);
    writer.setDefaultNamespace(DEFAULT_NAMESPACE);

    writer.add(eventFactory.createStartDocument());

    writer.add(
        eventFactory.createStartElement(
            new QName("http://www.springframework.org/schema/beans", "beans"),
            null,
            getNamespaces(eventFactory, DEFAULT_NAMESPACE, XML_NAMESPACES)));

    var attributeIterator =
        eipNode.attributes().entrySet().stream()
            .map(e -> eventFactory.createAttribute(e.getKey(), e.getValue().toString()))
            .iterator();

    String nsUri = eipNamespaceToUri.get(eipNode.eipId().namespace());

    writer.add(
        eventFactory.createStartElement(
            writer.getPrefix(nsUri), nsUri, eipNode.eipId().name(), attributeIterator, null));

    writer.add(
        eventFactory.createEndElement(
            new QName(eipNode.eipId().namespace(), eipNode.eipId().name()), null));

    writer.add(eventFactory.createEndDocument());

    writer.flush();
    writer.close();

    System.out.println(formatXml(outputStream));
  }

  private static String formatXml(ByteArrayOutputStream byteStream) throws TransformerException {
    var transformerFactory = TransformerFactory.newInstance();
    var transformer = transformerFactory.newTransformer();

    // pretty print by indention
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    // add standalone="yes", add line break before the root element
    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

    var source = new StreamSource(new ByteArrayInputStream(byteStream.toByteArray()));
    var result = new StringWriter();
    transformer.transform(source, new StreamResult(result));
    return result.toString();
  }

  private static Iterator<Namespace> getNamespaces(
      XMLEventFactory eventFactory,
      String defaultNamespaceUri,
      Map<String, String> prefixToNamespace) {
    Stream<Namespace> defaultNamespace =
        Stream.of(eventFactory.createNamespace(defaultNamespaceUri));

    Stream<Namespace> otherNamespaces =
        prefixToNamespace.entrySet().stream()
            .map(e -> eventFactory.createNamespace(e.getKey(), e.getValue()));

    return Stream.concat(defaultNamespace, otherNamespaces).iterator();
  }
}
