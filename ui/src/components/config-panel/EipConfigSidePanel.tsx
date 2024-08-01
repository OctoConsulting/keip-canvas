import { HeaderPanel } from "@carbon/react"
import { useState } from "react"
import { useOnSelectionChange } from "reactflow"
import { EipFlowNode } from "../../api/flow"
import { Attribute } from "../../api/generated/eipComponentDef"
import { childIdToString } from "../../api/id"
import { lookupEipComponent } from "../../singletons/eipDefinitions"
import { useAppActions, useGetSelectedChildNode } from "../../singletons/store"
import ChildNodeConfig from "./ChildNodeConfig"
import RootNodeConfig from "./RootNodeConfig"

const flowControlledAttributes = new Set([
  "id",
  "channel",
  "input-channel",
  "output-channel",
])

const getConfigurableAttributes = (attrs: Attribute[] | undefined) => {
  return attrs
    ? attrs.filter((attr) => !flowControlledAttributes.has(attr.name))
    : []
}

const EipConfigSidePanel = () => {
  const { clearSelectedChildNode } = useAppActions()
  const [selectedNode, setSelectedNode] = useState<EipFlowNode | null>(null)
  const selectedChild = useGetSelectedChildNode()

  useOnSelectionChange({
    onChange: ({ nodes }) => {
      selectedChild && clearSelectedChildNode()
      setSelectedNode(nodes.length === 1 ? nodes[0] : null)
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
    const configurableAttrs = getConfigurableAttributes(childElement.attributes)
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
    // TODO: Pass in channelId to Panel
    const configurableAttrs = getConfigurableAttributes(eipComponent.attributes)
    sidePanelContent = (
      <RootNodeConfig
        key={selectedNode.id}
        node={selectedNode}
        attributes={configurableAttrs}
        childGroup={eipComponent.childGroup}
      />
    )
  } else {
    // Returning an empty fragment because the HeaderPanel component spams the logs with error messages if it doesn't have any children.
    sidePanelContent = <></>
  }

  const showPanel = Boolean(selectedNode ?? selectedChild)

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
