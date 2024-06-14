package com.octo.keip.flow.graph

import com.octo.keip.flow.dto.Flow
import com.octo.keip.flow.dto.FlowEdge
import com.octo.keip.flow.model.eip.EipNode
import spock.lang.Specification

import java.util.stream.Stream

class GuavaGraphTest extends Specification {

    def "empty flow returns an empty node stream"() {
        when:
        def graph = GuavaGraph.from(new Flow([], []))
        def nodes = graph.traverse()

        then:
        nodes.toList().isEmpty()
    }

    def "detached edge throws exception"(String sourceId, String targetId) {
        given:
        def n1 = newNode("1")
        def n2 = newNode("2")

        def e1 = new FlowEdge("a", sourceId, targetId)

        def flow = new Flow([n1, n2], [e1])

        when:
        GuavaGraph.from(flow)

        then:
        thrown(IllegalArgumentException)

        where:
        sourceId | targetId
        "0"      | "20"
        "1"      | "5"
        "5"      | "2"
    }

    def "multiple single element flows"() {
        given:
        def n1 = newNode("1")
        def n2 = newNode("2")

        def flow = new Flow([n1, n2], [])
        def graph = GuavaGraph.from(flow)

        when:
        def nodes = graph.traverse()

        then:
        getIds(nodes) == ["1", "2"]
    }

    /**
     * 1 -> 2 -> 3
     * 21 -> 22
     */
    def "multiple flows"() {
        given:
        def start1 = newNode("1")
        def middle1 = newNode("2")
        def end1 = newNode("3")

        def start2 = newNode("4")
        def end2 = newNode("5")

        def edge1 = new FlowEdge("1", "1", "2")
        def edge2 = new FlowEdge("2", "2", "3")
        def edge3 = new FlowEdge("3", "4", "5")

        def flow = new Flow([start1, middle1, end1, start2, end2], [edge1, edge2, edge3])
        def graph = GuavaGraph.from(flow)

        when:
        def nodes = graph.traverse()

        then:
        getIds(nodes) == ["1", "2", "3", "4", "5"]
    }

    def "duplicate node ids throws exception"() {
        given:
        def n1 = newNode("1")
        def n2 = newNode("2")
        def n3 = newNode("1")

        def flow = new Flow([n1, n2, n3], [])

        when:
        GuavaGraph.from(flow)

        then:
        thrown(IllegalArgumentException)
    }

    def "graph with a cycle throws exception"() {
        given:
        def n1 = newNode("1")
        def n2 = newNode("2")
        def n3 = newNode("3")

        def e1 = new FlowEdge("a", "1", "2")
        def e2 = new FlowEdge("b", "2", "3")
        def e3 = new FlowEdge("c", "3", "1")

        def flow = new Flow([n1, n2, n3], [e1, e2, e3])

        when:
        GuavaGraph.from(flow)

        then:
        thrown(IllegalArgumentException)
    }

    // TODO: Use Builder?
    private static EipNode newNode(String id) {
        return new EipNode(id, null, null, null, null, null)
    }

    private static List<String> getIds(Stream<EipNode> nodes) {
        return nodes.map { it.id() }.toList()
    }
}
