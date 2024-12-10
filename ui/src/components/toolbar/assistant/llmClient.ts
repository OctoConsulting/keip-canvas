import { Ollama } from "@langchain/ollama"
import { Edge, Node } from "reactflow"
import { EipId } from "../../../api/generated/eipFlow"
import { EipConfig, SerializedFlow } from "../../../singletons/store/api"
import { EXPORTED_FLOW_VERSION } from "../../../singletons/store/appStore"
import {
  getEdgesView,
  getEipId,
  getLayoutView,
  getNodesView,
} from "../../../singletons/store/storeViews"
import fetchWithTimeout from "../../../utils/fetch/fetchWithTimeout"
import { newFlowLayout } from "../../layout/layouting"
import { fuzzyEipMatch } from "./fuzzyEipIdMatch"
import { flowCreatePrompt, flowUpdatePrompt } from "./prompt"

interface GenNodeData {
  eipId: EipId
}

interface ModelFlowResponse {
  nodes: Node<GenNodeData>[]
  edges: Edge[]
  eipConfigs: Record<string, EipConfig>
}

interface PromptResponse {
  raw: string
  success: boolean
  cause?: Error | "aborted"
  processedFlow?: SerializedFlow
}

class LlmClient {
  private llm
  private abortCtrl
  public serverBaseUrl = "http://localhost:11434"

  constructor() {
    this.llm = new Ollama({
      baseUrl: this.serverBaseUrl,
      maxRetries: 3,
      model: "mistral",
      format: "json",
      numCtx: 4096,
      temperature: 0,
    })

    this.abortCtrl = new AbortController()
  }

  public async prompt(
    userInput: string,
    streamCallback: (data: string) => void
  ): Promise<PromptResponse> {
    let rawResponse = ""
    try {
      const prompt = await this.generatePrompt()
      const chain = prompt.pipe(this.llm)
      const responseStream = await chain.stream({
        userInput: userInput,
      })

      for await (const chunk of responseStream) {
        if (this.abortCtrl.signal.aborted) {
          this.abortCtrl = new AbortController()
          return { raw: rawResponse, success: false, cause: "aborted" }
        }

        rawResponse += chunk
        streamCallback(rawResponse)
      }

      return {
        raw: rawResponse,
        processedFlow: this.parseResponse(rawResponse),
        success: true,
      }
    } catch (err) {
      console.error(err)
      return {
        raw: rawResponse,
        success: false,
        cause: err as Error,
      }
    }
  }

  public abortPrompt(): void {
    this.abortCtrl.abort()
  }

  public ping(): { success: Promise<boolean>; abort: () => void } {
    const abortCtrl = new AbortController()
    const success = fetchWithTimeout(this.serverBaseUrl, { abortCtrl })
      .then((res) => res.ok)
      .catch(() => false)
    return { success, abort: () => abortCtrl.abort() }
  }

  private async generatePrompt() {
    const nodes = getNodesView()
    if (nodes && nodes.length > 0) {
      const currFlow = JSON.stringify({
        nodes: nodes.map((n) => ({
          id: n.id,
          type: n.type,
          data: { eipId: getEipId(n.id) },
        })),
        edges: getEdgesView(),
      })
      const prompt = await flowUpdatePrompt
      return await prompt.partial({
        existingFlowJson: currFlow,
      })
    } else {
      return await flowCreatePrompt
    }
  }

  // TODO: Use Langchain custom output parser
  private parseResponse(jsonResponse: string) {
    const response = JSON.parse(jsonResponse) as ModelFlowResponse
    if (!response.nodes) {
      throw new Error("No nodes provided in model response: " + jsonResponse)
    }

    if (!response.edges) {
      if (response.nodes.length === 1) {
        response.edges = []
      } else {
        throw new Error("No edges provided in model response: " + jsonResponse)
      }
    }

    response.eipConfigs = {}
    collectEipIds(response)
    const eipFlow = convertToSerializedFlow(response)

    const layout = getLayoutView()
    eipFlow.nodes = newFlowLayout(eipFlow.nodes, eipFlow.edges, layout)

    return eipFlow
  }
}

const convertToSerializedFlow = (
  response: ModelFlowResponse
): SerializedFlow => ({
  nodes: response.nodes.map((node) => ({ ...node, data: {} })),
  edges: response.edges,
  eipConfigs: response.eipConfigs,
  version: EXPORTED_FLOW_VERSION,
})

const collectEipIds = (response: ModelFlowResponse) =>
  response.nodes.forEach((node) => {
    const eipId = fuzzyEipMatch(node.data.eipId)
    response.eipConfigs[node.id] = { eipId, attributes: {}, children: [] }
  })

export const llmClientInstance = new LlmClient()
