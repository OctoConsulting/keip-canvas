package com.octo.keip.translate.graph

import com.octo.keip.translate.dto.Flow
import com.octo.keip.translate.dto.FlowEdge
import com.octo.keip.translate.model.EipNode
import spock.lang.Specification

class GuavaGraphTest extends Specification {

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

        when:
        def graph = GuavaGraph.from(flow)
        def nodes = graph.traverse()

        then:
        def actualOrder = nodes.map { it.id() }.toList()
        actualOrder == ["1", "2", "3", "4", "5"]
    }

    // TODO: Use Builder?
    private static EipNode newNode(String id) {
        return new EipNode(id, null, null, null, null, null)
    }
}
