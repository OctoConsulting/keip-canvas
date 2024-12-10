import { produce } from "immer"
import { nanoid } from "nanoid/non-secure"
import { Edge, Position, XYPosition } from "reactflow"
import {
  ChannelMapping,
  DYNAMIC_EDGE_TYPE,
  DynamicEdge,
  EIP_NODE_TYPE,
  EipFlowNode,
  Layout,
} from "../../api/flow"
import { AttributeType } from "../../api/generated/eipComponentDef"
import { EipId } from "../../api/id"
import { newFlowLayout } from "../../components/layout/layouting"
import { AppStore, EipConfig, SerializedFlow } from "./api"
import { useAppStore } from "./appStore"

export const createDroppedNode = (eipId: EipId, position: XYPosition) =>
  useAppStore.setState((state) => {
    const node = newNode(position, state.layout.orientation)
    return {
      nodes: [...state.nodes, node],
      eipConfigs: {
        ...state.eipConfigs,
        [node.id]: { attributes: {}, children: [], eipId },
      },
    }
  })

export const updateNodeLabel = (id: string, label: string) => {
  let error: Error | undefined
  useAppStore.setState((state) => {
    const updatedNodes = state.nodes.map((node) => {
      if (node.id === id) {
        return { ...node, data: { ...node.data, label } }
      } else {
        if (node.data.label === label) {
          error = new Error("Node labels must be unique")
        }
        return node
      }
    })
    return { nodes: error ? state.nodes : updatedNodes }
  })
  return error
}

export const updateNodeDescription = (id: string, description: string) =>
  useAppStore.setState(
    produce((draft: AppStore) => {
      draft.eipConfigs[id].description = description
    })
  )

export const updateEipAttribute = (
  id: string,
  attrName: string,
  value: AttributeType
) =>
  useAppStore.setState(
    produce((draft: AppStore) => {
      draft.eipConfigs[id].attributes[attrName] = value
    })
  )

export const deleteEipAttribute = (id: string, attrName: string) =>
  useAppStore.setState(
    produce((draft: AppStore) => {
      delete draft.eipConfigs[id].attributes[attrName]
    })
  )

// TODO: Ensure no more than one default mapping edge can be set
export const updateDynamicEdgeMapping = (
  edgeId: string,
  mapping: Partial<ChannelMapping>
) =>
  useAppStore.setState((state) => ({
    edges: state.edges.map((edge) => {
      if (edge.id === edgeId) {
        const dynamic = validateDynamicEdgeType(edge)
        return {
          ...dynamic,
          data: {
            ...dynamic.data,
            mapping: { ...dynamic.data?.mapping, ...mapping },
          },
        }
      }
      return edge
    }),
  }))

export const updateContentRouterKey = (
  nodeId: string,
  keyName: string,
  attrName: string,
  value: AttributeType
) =>
  useAppStore.setState(
    produce((draft: AppStore) => {
      const config = draft.eipConfigs[nodeId]
      config.routerKey ??= { name: keyName }
      config.routerKey.name = keyName

      const routerKey = config.routerKey
      routerKey.attributes ??= {}
      routerKey.attributes[attrName] = value
    })
  )

export const enableChild = (parentId: string, childEipId: EipId) =>
  useAppStore.setState((state) => {
    const childConfig: EipConfig = {
      eipId: childEipId,
      attributes: {},
      children: [],
    }

    const childId = nanoid(11)

    return produce(state, (draft: AppStore) => {
      draft.eipConfigs[parentId].children.push(childId)
      draft.eipConfigs[childId] = childConfig
    })
  })

export const disableChild = (parentId: string, childId: string) =>
  useAppStore.setState((state) => {
    const idx = state.eipConfigs[parentId].children.findIndex(
      (id) => id === childId
    )

    if (idx === -1) {
      throw new Error(
        `Node (id: ${parentId}) did not have child with id: ${childId}`
      )
    }

    return produce(state, (draft: AppStore) => {
      draft.eipConfigs[parentId].children.splice(idx, 1)
      delete draft.eipConfigs[childId]
    })
  })

export const updateSelectedChildNode = (childId: string) =>
  useAppStore.setState(() => ({ selectedChildNode: childId }))

export const clearSelectedChildNode = () =>
  useAppStore.setState(() => ({ selectedChildNode: null }))

export const clearFlow = () =>
  useAppStore.setState(() => ({
    nodes: [],
    edges: [],
    eipConfigs: {},
    selectedChildNode: null,
  }))

export const clearDiagramSelections = () =>
  useAppStore.setState((state) => ({
    nodes: state.nodes.map((node) => ({ ...node, selected: false })),
    edges: state.edges.map((edge) => ({ ...edge, selected: false })),
  }))

export const importFlowFromJson = (json: string) => {
  const flow = JSON.parse(json) as SerializedFlow
  importFlowFromObject(flow)
}

// TODO: Should a failed import throw an error on failure instead (for an error pop-up)?
export const importFlowFromObject = (flow: SerializedFlow) => {
  useAppStore.setState(() => {
    if (!isStoreType(flow)) {
      console.error("Failed to import an EIP flow JSON. Malformed input")
      return {}
    }

    // Maintain backwards compatibility with older exported formats
    if (!flow.eipConfigs && !flow.version) {
      return importDeprecatedFlow(flow)
    }

    return {
      nodes: flow.nodes,
      edges: flow.edges,
      eipConfigs: flow.eipConfigs,
    }
  })
}

export const updateLayoutOrientation = (orientation: Layout["orientation"]) =>
  useAppStore.setState((state) => {
    const newLayout: Layout = {
      ...state.layout,
      orientation: orientation,
    }
    const nodes = newFlowLayout(state.nodes, state.edges, newLayout)
    return {
      nodes: nodes,
      layout: newLayout,
    }
  })

export const toggleLayoutDensity = () =>
  useAppStore.setState((state) => {
    const newDensity =
      state.layout.density === "compact" ? "comfortable" : "compact"
    const newLayout: Layout = {
      ...state.layout,
      density: newDensity,
    }
    const nodes = newFlowLayout(state.nodes, state.edges, newLayout)
    return {
      nodes: nodes,
      layout: newLayout,
    }
  })

const newNode = (position: XYPosition, orientation: Layout["orientation"]) => {
  const id = nanoid(10)
  const isHorizontal = orientation === "horizontal"
  const node: EipFlowNode = {
    id: id,
    type: EIP_NODE_TYPE,
    position: position,
    targetPosition: isHorizontal ? Position.Left : Position.Top,
    sourcePosition: isHorizontal ? Position.Right : Position.Bottom,
    data: {},
  }
  return node
}

const validateDynamicEdgeType = (edge: Edge) => {
  if (edge.type !== DYNAMIC_EDGE_TYPE) {
    throw new Error(
      `The provided edge did not have the expected type: "${edge.type}". Should be "${DYNAMIC_EDGE_TYPE}"`
    )
  }
  return edge as DynamicEdge
}

const isStoreType = (state: unknown): state is AppStore => {
  const store = state as SerializedFlow & {
    eipNodeConfigs: Record<string, object>
  }

  const hasOldConfigKey = store.eipNodeConfigs !== undefined
  if (hasOldConfigKey) {
    console.warn(
      "Attempting to import a deprecated EIP Flow format. Attribute configurations will not be preserved."
    )
  }

  return (
    store.nodes !== undefined &&
    store.edges !== undefined &&
    (store.eipConfigs !== undefined || hasOldConfigKey)
  )
}

// Maintains compatibility with older exported formats
const importDeprecatedFlow = (flow: SerializedFlow) => {
  const eipConfigs = {} as AppStore["eipConfigs"]
  const nodes = flow.nodes.map((node) => {
    const { eipId: oldEipId, ...rest } = node.data as { eipId: EipId }
    eipConfigs[node.id] = {
      attributes: {},
      children: [],
      eipId: oldEipId,
    }
    return {
      ...node,
      data: rest,
    }
  })

  return {
    nodes,
    edges: flow.edges,
    eipConfigs,
  }
}
