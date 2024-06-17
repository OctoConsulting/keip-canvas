package com.octo.keip.flow.xml;

import com.ctc.wstx.stax.WstxEventFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.octo.keip.flow.model.eip.EipGraph;
import com.octo.keip.flow.model.eip.EipId;
import com.octo.keip.flow.model.eip.EipNode;
import com.octo.keip.flow.xml.spring.DefaultXmlTransformer;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

// TODO: EipNodeXmlTranslator registry
public class GraphXmlTransformer {

  private final XMLEventFactory eventFactory = WstxEventFactory.newFactory();
  private final XMLOutputFactory outputFactory = WstxOutputFactory.newFactory();

  // TODO: Make this configurable
  private final NodeXmlTransformer defaultNodeTransformer = new DefaultXmlTransformer();

  // TODO: Make this configurable
  private final String defaultNamespace = "http://www.springframework.org/schema/beans";

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
    writer.setDefaultNamespace(defaultNamespace);

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
    return eventFactory.createStartElement(
        new QName("http://www.springframework.org/schema/beans", "beans"),
        null,
        collectNamespaces(graph));
  }

  /**
   * Does a first path through the graph to collect all the namespaces up-front, in order to define
   * them on the root element. This approach forces us to traverse the graph at least two times (one
   * to gather namespaces and the other to transform the nodes), so it might prove too inefficient.
   */
  private Iterator<Namespace> collectNamespaces(EipGraph graph) {
    Stream<EipNode> nodes = graph.traverse();
    // TODO: What's the behavior if the namespace key is not in the map (unknown eip namespace)?
    Stream<Namespace> collectedNamespaces =
        nodes
            .map(n -> n.eipId().namespace())
            .distinct()
            .map(ns -> eventFactory.createNamespace(ns, this.mappedNamespaceUris.get(ns)));

    Stream<Namespace> defaultNamespace =
        Stream.of(eventFactory.createNamespace(this.defaultNamespace));

    return Stream.concat(defaultNamespace, collectedNamespaces).iterator();
  }

  // TODO: Assign a default node transformer
  private void writeNode(EipNode node, EipGraph graph, XMLEventWriter writer)
      throws XMLStreamException {
    NodeXmlTransformer transformer =
        this.registeredNodeTransformers.getOrDefault(node.eipId(), this.defaultNodeTransformer);
    List<XmlElement> elements =
        transformer.apply(node, graph.predecessors(node), graph.successors(node));
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

  // TODO: Support pretty-printing?
}
