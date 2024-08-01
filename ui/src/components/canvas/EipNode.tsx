import { Button, Stack, Tile } from "@carbon/react"
import { ServiceId } from "@carbon/react/icons"
import { Handle, NodeProps, Position } from "reactflow"
import { EipNodeData, Layout } from "../../api/flow"
import { ConnectionType, EipRole } from "../../api/generated/eipComponentDef"
import { ChildNodeId, EipId } from "../../api/id"
import { lookupEipComponent } from "../../singletons/eipDefinitions"
import getIconUrl from "../../singletons/eipIconCatalog"
import {
  useAppActions,
  useGetChildren,
  useGetLayout,
  useIsChildSelected,
} from "../../singletons/store"
import { toTitleCase } from "../../utils/titleTransform"
import "./nodes.scss"

interface ChildrenIconsProps {
  childrenNames: string[]
  parentNodeId: string
  parentEipId: EipId
}

const defaultNamespace = "integration"

const renderHorizontalHandles = (connectionType: ConnectionType) => {
  switch (connectionType) {
    case "source":
      return <Handle id="output" type="source" position={Position.Right} />
    case "sink":
      return <Handle id="input" type="target" position={Position.Left} />
    case "passthru":
    case "request_reply":
      return (
        <>
          <Handle id="output" type="source" position={Position.Right} />
          <Handle id="input" type="target" position={Position.Left} />
        </>
      )
    case "tee":
      return (
        <>
          <Handle id="output" type="source" position={Position.Right} />
          <Handle id="discard" type="source" position={Position.Bottom} />
          <Handle id="input" type="target" position={Position.Left} />
        </>
      )
  }
}

const renderVerticalHandles = (connectionType: ConnectionType) => {
  switch (connectionType) {
    case "source":
      return <Handle id="output" type="source" position={Position.Bottom} />
    case "sink":
      return <Handle id="input" type="target" position={Position.Top} />
    case "passthru":
    case "request_reply":
      return (
        <>
          <Handle id="output" type="source" position={Position.Bottom} />
          <Handle id="input" type="target" position={Position.Top} />
        </>
      )
    case "tee":
      return (
        <>
          <Handle id="output" type="source" position={Position.Bottom} />
          <Handle id="discard" type="source" position={Position.Right} />
          <Handle id="input" type="target" position={Position.Top} />
        </>
      )
  }
}

// TODO: Limit handles to the appropriate number of connections
const renderHandles = (
  connectionType: ConnectionType,
  layoutType: Layout["orientation"]
) =>
  layoutType === "horizontal"
    ? renderHorizontalHandles(connectionType)
    : renderVerticalHandles(connectionType)

const getNamespacedTitle = (eipId: EipId) => {
  if (eipId.namespace === defaultNamespace) {
    return toTitleCase(eipId.name)
  }
  return toTitleCase(eipId.namespace) + " " + toTitleCase(eipId.name)
}

const getClassNames = (props: NodeProps<EipNodeData>, role: EipRole) => {
  const roleClsName =
    role === "channel" ? "eip-channel-node" : "eip-endpoint-node"
  const selectedClsName = props.selected ? "eip-node-selected" : ""
  return ["eip-node", roleClsName, selectedClsName].join(" ")
}

const ChildIconButton = (props: ChildNodeId) => {
  const { updateSelectedChildNode } = useAppActions()
  const selected = useIsChildSelected(props)

  const clsNames = ["child-icon-button"]
  selected && clsNames.push("child-icon-button-focused")

  return (
    <Button
      className={clsNames.join(" ")}
      hasIconOnly
      renderIcon={ServiceId}
      iconDescription={props.name}
      size="sm"
      tooltipPosition="bottom"
      kind="primary"
      onClick={(ev) => {
        ev.stopPropagation()
        updateSelectedChildNode(props)
      }}
    />
  )
}

// TODO: Account for a large number of children to be displayed
// TODO: Create a mapping of children to icons (with a fallback option)
const ChildrenIcons = ({ childrenNames, parentNodeId }: ChildrenIconsProps) => {
  return (
    <Stack className="eip-node-children" orientation="horizontal" gap={2}>
      {childrenNames.map((name) => (
        <ChildIconButton key={name} name={name} parentNodeId={parentNodeId} />
      ))}
    </Stack>
  )
}

// TODO: Consider separating into Endpoint and Channel custom node types
export const EipNode = (props: NodeProps<EipNodeData>) => {
  // TODO: clearSelectedChildNode is used in too many different components. See if that can be reduced (or elimnated).
  const { clearSelectedChildNode } = useAppActions()
  const childrenState = useGetChildren(props.id)
  const hasChildren = childrenState.length > 0

  const { data } = props
  const componentDefinition = lookupEipComponent(data.eipId)!
  const layout = useGetLayout()
  const handles = renderHandles(
    componentDefinition.connectionType,
    layout.orientation
  )

  return (
    <Tile
      className={getClassNames(props, componentDefinition.role)}
      onClick={hasChildren ? () => clearSelectedChildNode() : undefined}
    >
      <div>{getNamespacedTitle(data.eipId)}</div>
      <img className="eip-node-image" src={getIconUrl(data.eipId)} />
      <div
        className="eip-node-label"
        style={hasChildren ? { marginBottom: "0.5rem" } : {}}
      >
        <strong>{data.label}</strong>
      </div>
      {hasChildren && (
        <ChildrenIcons
          childrenNames={childrenState}
          parentNodeId={props.id}
          parentEipId={props.data.eipId}
        />
      )}
      {handles}
    </Tile>
  )
}
