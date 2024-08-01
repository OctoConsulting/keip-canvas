import { Stack, TextArea } from "@carbon/react"
import { Attribute } from "../../api/generated/eipComponentDef"
import { ChildNodeId } from "../../api/id"
import { toTitleCase } from "../../utils/titleTransform"
import AttributeConfigForm from "./AttributeConfigForm"
import ConfigurationInputTabs from "./ConfigurationTabs"

interface ChildAttributePanelProps {
  childId: ChildNodeId
  parentName: string
  attributes: Attribute[]
  hasChildren: boolean
}

// TODO: Add description for child
const ChildNodeConfig = ({
  childId,
  parentName,
  attributes,
  hasChildren,
}: ChildAttributePanelProps) => (
  <Stack gap={6}>
    <Stack gap={6} className="side-panel-padded-container">
      <h4>{toTitleCase(parentName)}</h4>
      <h5>{toTitleCase(childId.name)}</h5>
    </Stack>
    <ConfigurationInputTabs
      hasAttributes={attributes.length > 0}
      hasChildren={hasChildren}
      attributesForm={
        <AttributeConfigForm
          id={childId.name}
          parentId={childId.parentNodeId}
          attrs={attributes}
        />
      }
      childrenForm={
        <TextArea
          labelText="Children XML"
          helperText="Pass in any additional nested children as XML elements"
          id="children-xml-escape"
          enableCounter
          maxCount={5000}
        />
      }
    />
  </Stack>
)

export default ChildNodeConfig
