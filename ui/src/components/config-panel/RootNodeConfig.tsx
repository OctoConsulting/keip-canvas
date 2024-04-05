import { Stack, TextArea, TextInput } from "@carbon/react"
import { Attribute, EipChildGroup } from "../../api/eipSchema"
import { EipFlowNode } from "../../api/flow"
import { ROOT_PARENT, useAppActions } from "../../singletons/store"
import AttributeConfigForm from "./AttributeConfigForm"
import ChildSelector from "./ChildSelector"
import ConfigurationInputTabs from "./ConfigurationTabs"

interface PanelContentProps {
  node: EipFlowNode
  attributes: Attribute[]
  childGroup?: EipChildGroup
}

const NodeIdentifierInputs = ({ node }: { node: EipFlowNode }) => {
  const { updateNodeLabel } = useAppActions()
  return (
    <Stack gap={6} className="side-panel-padded-container">
      <TextInput
        id="nodeId"
        labelText="NodeId"
        disabled
        defaultValue={node.id}
      />
      <TextInput id={"channelId"} labelText="ChannelId" disabled />
      <TextInput
        id="nodeLabel"
        labelText="Label"
        defaultValue={node ? node.data.label : ""}
        onChange={(e) => updateNodeLabel(node.id, e.target.value)}
      />
      <TextArea
        labelText="Description"
        helperText="Optional description of the selected node's behavior"
        enableCounter
        maxCount={600}
      ></TextArea>
    </Stack>
  )
}

const RootNodeConfig = ({
  node,
  attributes,
  childGroup,
}: PanelContentProps) => (
  <Stack gap={8}>
    <NodeIdentifierInputs node={node} />
    <ConfigurationInputTabs
      hasAttributes={attributes.length > 0}
      hasChildren={Boolean(childGroup && childGroup.children.length > 0)}
      attributesForm={
        <AttributeConfigForm
          id={node.id}
          parentId={ROOT_PARENT}
          attrs={attributes}
        />
      }
      childrenForm={
        <ChildSelector nodeId={node.id} childGroup={childGroup!} />
      }
    />
  </Stack>
)

export default RootNodeConfig