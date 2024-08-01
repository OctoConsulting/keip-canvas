import {
  EipComponent,
  EipComponentDefinition,
} from "../api/generated/eipComponentDef"
import { EipId } from "../api/id"
import eipDefintion from "../json/springIntegrationEipComponents.json"

// TODO: Validate that the parsed JSON matches the schema type
export const EIP_SCHEMA: Readonly<EipComponentDefinition> =
  eipDefintion as EipComponentDefinition

const getFlatMap = (schema: EipComponentDefinition) => {
  const map = new Map<string, EipComponent>()
  for (const [namespace, componentList] of Object.entries(schema)) {
    componentList.forEach((c) => map.set(`${namespace}.${c.name}`, c))
  }
  return map
}

const componentFlatMap = getFlatMap(EIP_SCHEMA)

export const lookupEipComponent = (eipId: EipId) => {
  const component = componentFlatMap.get(`${eipId.namespace}.${eipId.name}`)
  if (component === undefined) {
    console.warn(`Did not find component with id: ${JSON.stringify(eipId)}`)
  }
  return component
}
