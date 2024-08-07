import { PipelinePromptTemplate, PromptTemplate } from "@langchain/core/prompts"
import { EipId } from "../../../api/id"
import { EIP_SCHEMA } from "../../../singletons/eipDefinitions"

const responseJsonExample = `{
    "nodes": [
      {
        "id": "n1",
        "type": "eipNode",
        "data": {
          "eipId": {
            "namespace": "integration",
            "name": "inbound-channel-adapter"
          }
        }
      },
      {
          "id": "n2",
          "type": "eipNode",
          "data": {
            "eipId": {
              "namespace": "http",
              "name": "outbound-gateway"
            }
          }
        }
    ],
    "edges": [
      {
        "source": "n1",
        "target": "n2",
        "id": "edge_n1_n2"
      }
    ]
  }
  `

const flowExamplePrompt = PromptTemplate.fromTemplate(
  `The response MUST be a JSON that matches the following flow schema:
START_FLOW_SCHEMA
\`\`\`json
{responseFormat}
\`\`\`
END_FLOW_SCHEMA

Don't add any extraneous fields or properties to the response, stick to the JSON keys outlined in the example above.`
)

const eipCatalogPrompt = PromptTemplate.fromTemplate(
  `each node's data.eipId field corresponds to a Spring Integration component and MUST be chosen from the following list of eipIds:
START_EIP_COMPONENT_LIST
{eipIds}
END_EIP_COMPONENT_LIST`
)

const existingFlowPrompt = PromptTemplate.fromTemplate(
  `Use this existing flow JSON as the starting point:
\`\`\`json
{existingFlowJson}
\`\`\``
)

const inputPrompt = PromptTemplate.fromTemplate(
  `Avoid adding any explicit channels to the flow

Taking the above into account, respond to the following request:
{userInput}`
)

// TODO: Do this async instead?
const eipIds = Object.entries(EIP_SCHEMA).reduce((acc, curr) => {
  const [namespace, components] = curr
  const ids = components.map((c) => ({ namespace: namespace, name: c.name }))
  return [...acc, ...ids]
}, [] as EipId[])

const eipIdsJson = JSON.stringify(eipIds)

const createPipeline = [
  { name: "flowExampleJson", prompt: flowExamplePrompt },
  { name: "eipCatalog", prompt: eipCatalogPrompt },
  { name: "input", prompt: inputPrompt },
]

const updatePipeline = [
  ...createPipeline.slice(0, createPipeline.length - 1),
  { name: "existingFlow", prompt: existingFlowPrompt },
  createPipeline[createPipeline.length - 1],
]

const fullCreatePrompt = PromptTemplate.fromTemplate(
  `{flowExampleJson}

{eipCatalog}

{input}`
)

const fullUpdatePrompt = PromptTemplate.fromTemplate(
  `{flowExampleJson}

{eipCatalog}

{existingFlow}

{input}`
)

const composedCreatePrompt = new PipelinePromptTemplate({
  pipelinePrompts: createPipeline,
  finalPrompt: fullCreatePrompt,
})

const composedUpdatePrompt = new PipelinePromptTemplate({
  pipelinePrompts: updatePipeline,
  finalPrompt: fullUpdatePrompt,
})

const flowCreatePrompt = composedCreatePrompt.partial({
  responseFormat: responseJsonExample,
  eipIds: eipIdsJson,
})

const flowUpdatePrompt = composedUpdatePrompt.partial({
  responseFormat: responseJsonExample,
  eipIds: eipIdsJson,
})

// TODO: Refactor creating the prompts. Lots of duplicated logic.

export { flowCreatePrompt, flowUpdatePrompt }
