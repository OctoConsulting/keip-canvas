import { Stack, TextArea, TextInput } from "@carbon/react"
import { ChangeEvent, useMemo, useState } from "react"
import { EipFlowNode } from "../../api/flow"
import { Attribute, EipChildGroup } from "../../api/generated/eipComponentDef"
import {
  ROOT_PARENT,
  useAppActions,
  useGetNodeDescription,
} from "../../singletons/store"
import debounce from "../../utils/debounce"
import { AttributeConfigForm } from "./AttributeConfigForm"
import ChildSelector from "./ChildSelector"
import ConfigurationInputTabs from "./ConfigurationTabs"

interface PanelContentProps {
  node: EipFlowNode
  attributes: Attribute[]
  childGroup?: EipChildGroup
}

const NodeIdentifierInputs = ({ node }: { node: EipFlowNode }) => {
  const { updateNodeLabel, updateNodeDescription } = useAppActions()
  const description = useGetNodeDescription(node.id)

  const [isLabelValid, setIsLabelValid] = useState(true)

  const handleDescriptionUpdates = useMemo(
    () =>
      debounce(
        (ev: ChangeEvent<HTMLTextAreaElement>) =>
          updateNodeDescription(node.id, ev.target.value),
        300
      ),
    [node.id, updateNodeDescription]
  )

  const handleLabelUpdates = useMemo(
    () =>
      debounce((ev: ChangeEvent<HTMLInputElement>) => {
        const err = updateNodeLabel(node.id, ev.target.value)
        setIsLabelValid(!err)
      }, 300),
    [node.id, updateNodeLabel]
  )

  return (
    <Stack gap={6} className="cfg-panel__container__padding-add">
      <TextInput
        id="nodeId"
        labelText="NodeId"
        disabled
        defaultValue={node.id}
      />
      <TextInput
        id="nodeLabel"
        labelText="Label"
        defaultValue={node ? node.data.label : ""}
        enableCounter
        invalid={!isLabelValid}
        invalidText="Node labels must be unique"
        maxCount={60}
        placeholder="New Node"
        onChange={handleLabelUpdates}
      />
      <TextArea
        labelText="Description"
        helperText="Optional description of the selected node's behavior"
        enableCounter
        maxCount={600}
        defaultValue={description}
        onChange={handleDescriptionUpdates}
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
      childrenForm={<ChildSelector nodeId={node.id} childGroup={childGroup!} />}
    />
  </Stack>
)

export default RootNodeConfig
