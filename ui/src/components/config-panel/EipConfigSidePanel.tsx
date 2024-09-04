import { HeaderPanel } from "@carbon/react"
import { useState } from "react"
import { Edge, useOnSelectionChange, useStoreApi } from "reactflow"
import { DYNAMIC_EDGE_TYPE, DynamicEdge, EipFlowNode } from "../../api/flow"
import { Attribute } from "../../api/generated/eipComponentDef"
import { childIdToString } from "../../api/id"
import {
  FLOW_CONTROLLED_ATTRIBUTES,
  lookupEipComponent,
} from "../../singletons/eipDefinitions"
import { useAppActions, useGetSelectedChildNode } from "../../singletons/store"
import ChildNodeConfig from "./ChildNodeConfig"
import DynamicEdgeConfig from "./DynamicEdgeConfig"
import RootNodeConfig from "./RootNodeConfig"

// TODO: filter out dynamic routing based attributes and children
const filterConfigurableAttributes = (attrs?: Attribute[]) => {
  return attrs
    ? attrs.filter((attr) => !FLOW_CONTROLLED_ATTRIBUTES.has(attr.name))
    : []
}

const isDynamicEdge = (edge: Edge) => edge?.type === DYNAMIC_EDGE_TYPE

const EipConfigSidePanel = () => {
  const reactFlowStore = useStoreApi()
  const { clearSelectedChildNode } = useAppActions()
  const [selectedNode, setSelectedNode] = useState<EipFlowNode | null>(null)
  const [selectedEdge, setSelectedEdge] = useState<DynamicEdge | null>(null)
  const selectedChild = useGetSelectedChildNode()

  useOnSelectionChange({
    onChange: ({ nodes, edges }) => {
      selectedChild && clearSelectedChildNode()
      const numSelected = nodes.length + edges.length
      setSelectedNode(numSelected === 1 ? nodes[0] : null)
      setSelectedEdge(
        numSelected === 1 && isDynamicEdge(edges[0]) ? edges[0] : null
      )
    },
  })

  const eipComponent = selectedNode
    ? lookupEipComponent(selectedNode.data.eipId)
    : null

  let sidePanelContent
  // TODO: Simplify conditionals
  if (selectedChild && selectedNode && eipComponent) {
    const childElement = eipComponent.childGroup!.children.find(
      (e) => e.name === selectedChild.name
    )!
    const configurableAttrs = filterConfigurableAttributes(
      childElement.attributes
    )
    sidePanelContent = (
      <ChildNodeConfig
        key={childIdToString(selectedChild)}
        childId={selectedChild}
        parentName={eipComponent.name}
        attributes={configurableAttrs}
        hasChildren={Boolean(childElement?.childGroup)}
      />
    )
  } else if (selectedNode && eipComponent) {
    const configurableAttrs = filterConfigurableAttributes(
      eipComponent.attributes
    )
    sidePanelContent = (
      <RootNodeConfig
        key={selectedNode.id}
        node={selectedNode}
        attributes={configurableAttrs}
        childGroup={eipComponent.childGroup}
      />
    )
  } else if (selectedEdge) {
    const { nodeInternals } = reactFlowStore.getState()
    const sourceNode = nodeInternals.get(selectedEdge.source)
    const targetNode = nodeInternals.get(selectedEdge.target)

    sidePanelContent = (
      <DynamicEdgeConfig
        key={selectedEdge.id}
        edge={{ ...selectedEdge, sourceNode, targetNode }}
      />
    )
  } else {
    // Returning an empty fragment because the HeaderPanel component spams the logs with error messages if it doesn't have any children.
    sidePanelContent = <></>
  }

  // TODO: disable this typescript warning globally
  // eslint-disable-next-line @typescript-eslint/prefer-nullish-coalescing
  const showPanel = Boolean(selectedNode || selectedChild || selectedEdge)

  return (
    <HeaderPanel
      className={showPanel ? "node-config-panel" : ""}
      expanded={showPanel}
    >
      {sidePanelContent}
    </HeaderPanel>
  )
}

export default EipConfigSidePanel
