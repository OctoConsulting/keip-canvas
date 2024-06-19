package com.octo.keip.flow.xml;

import com.ctc.wstx.stax.WstxEventFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.octo.keip.flow.model.EipGraph;
import com.octo.keip.flow.model.EipId;
import com.octo.keip.flow.model.EipNode;
import com.octo.keip.flow.xml.spring.DefaultXmlTransformer;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

// TODO: EipNodeXmlTranslator registry
// TODO: Support pretty-printing?
public class GraphXmlTransformer {

  private final XMLEventFactory eventFactory = WstxEventFactory.newFactory();
  private final XMLOutputFactory outputFactory = WstxOutputFactory.newFactory();

  private static final String XSI_PREFIX = "xsi";

  // TODO: Make these configurable
  private final NodeXmlTransformer defaultNodeTransformer = new DefaultXmlTransformer();
  private static final String DEFAULT_NAMESPACE = "http://www.springframework.org/schema/beans";
  private static final URI BASE_SCHEMA_LOCATION =
      URI.create("https://www.springframework.org/schema/integration/");

  private final Map<String, String> mappedNamespaceUris;

  private Map<EipId, NodeXmlTransformer> registeredNodeTransformers = Collections.emptyMap();

  public GraphXmlTransformer(Map<String, String> mappedNamespaceUris) {
    this.mappedNamespaceUris = mappedNamespaceUris;
  }

  // TODO: Could potentially move Writer to ctor
  // TODO: Handle explicit connections to channel components
  public void toXml(EipGraph graph, Writer output) throws XMLStreamException {
    // TODO: Set default namespace
    XMLEventWriter writer = outputFactory.createXMLEventWriter(output);
    writer.setDefaultNamespace(DEFAULT_NAMESPACE);

    writer.add(eventFactory.createStartDocument());

    StartElement root = createRootElement(graph);
    writer.add(root);

    // TODO: Refactor. Ugly.
    graph
        .traverse()
        .forEach(
            node -> {
              try {
                writeNode(node, graph, writer);
              } catch (XMLStreamException e) {
                throw new RuntimeException(e);
              }
            });

    writer.add(eventFactory.createEndElement(root.getName(), null));

    writer.add(eventFactory.createEndDocument());

    writer.flush();
    writer.close();
  }

  // TODO: Abstract creating the root element
  private StartElement createRootElement(EipGraph graph) {
    List<String> eipNamespaces = collectEipNamespaces(graph);
    return eventFactory.createStartElement(
        new QName("http://www.springframework.org/schema/beans", "beans"),
        getRootAttributes(eipNamespaces),
        getRootNamespaces(eipNamespaces));
  }

  /**
   * Does a first pass through the graph to collect all the included namespaces up-front, in order
   * to define them on the root element. This approach forces us to do an extra traversal of the
   * graph, so it might prove too inefficient.
   */
  private List<String> collectEipNamespaces(EipGraph graph) {
    // TODO: What's the behavior if the namespace key is not in the map (unknown eip namespace)?
    // Validate.
    return graph.traverse().map(n -> n.eipId().namespace()).distinct().toList();
  }

  private Iterator<Attribute> getRootAttributes(List<String> eipNamespaces) {
    Stream<String> defaultNamespaceLocation =
        Stream.of(
            DEFAULT_NAMESPACE, "https://www.springframework.org/schema/beans/spring-beans.xsd");
    Stream<String> collectedLocations =
        eipNamespaces.stream()
            .flatMap(ns -> Stream.of(this.mappedNamespaceUris.get(ns), getSchemaLocation(ns)));

    // TODO: Figure out how to use line breaks as the separator
    String locString =
        Stream.concat(defaultNamespaceLocation, collectedLocations)
            .collect(Collectors.joining(" "));

    return List.of(
            eventFactory.createAttribute(
                XSI_PREFIX,
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                "schemaLocation",
                locString))
        .iterator();
  }

  private Iterator<Namespace> getRootNamespaces(List<String> eipNamespaces) {
    Namespace defaultNamespace = eventFactory.createNamespace(DEFAULT_NAMESPACE);
    Namespace xsiNamespace =
        eventFactory.createNamespace(XSI_PREFIX, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
    Stream<Namespace> collectedNamespaces =
        eipNamespaces.stream()
            .map(ns -> eventFactory.createNamespace(ns, this.mappedNamespaceUris.get(ns)));

    return Stream.concat(Stream.of(defaultNamespace, xsiNamespace), collectedNamespaces).iterator();
  }

  // TODO: Assign a default node transformer
  private void writeNode(EipNode node, EipGraph graph, XMLEventWriter writer)
      throws XMLStreamException {
    NodeXmlTransformer transformer =
        this.registeredNodeTransformers.getOrDefault(node.eipId(), this.defaultNodeTransformer);
    List<XmlElement> elements = transformer.apply(node, graph);
    for (XmlElement element : elements) {
      writeElement(element, writer);
    }
  }

  private void writeElement(XmlElement element, XMLEventWriter writer) throws XMLStreamException {
    writer.add(
        eventFactory.createStartElement(
            element.prefix(),
            this.mappedNamespaceUris.get(element.prefix()),
            element.localName(),
            attributeIterator(element.attributes()),
            null));

    for (XmlElement c : element.children()) {
      writeElement(c, writer);
    }

    writer.add(
        eventFactory.createEndElement(
            element.prefix(), this.mappedNamespaceUris.get(element.prefix()), element.localName()));
  }

  private Iterator<Attribute> attributeIterator(Map<String, Object> attributes) {
    return attributes.entrySet().stream()
        .map(e -> eventFactory.createAttribute(e.getKey(), e.getValue().toString()))
        .iterator();
  }

  // TODO: Should this be calculated or configured?
  private static String getSchemaLocation(String eipNamespace) {
    String path =
        ("integration".equals(eipNamespace))
            ? "spring-integration.xsd"
            : String.format("%s/spring-integration-%s.xsd", eipNamespace, eipNamespace);
    return BASE_SCHEMA_LOCATION.resolve(path).toString();
  }
}
