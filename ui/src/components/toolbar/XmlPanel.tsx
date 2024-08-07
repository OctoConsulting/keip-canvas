import { Button, CodeSnippet } from "@carbon/react"
import { useState } from "react"
import { EipFlow } from "../../api/generated/eipFlow"
import { getEipFlow } from "../../singletons/store"

interface FlowTranslationResponse {
  data?: string
  error?: {
    message: string
    type: string
    details: object[]
  }
}

// TODO: Add client-side caching
const fetchXmlTranslation = async (flow: EipFlow) => {
  const response = await fetch("http://localhost:8080", {
    method: "POST",
    body: JSON.stringify(flow),
    headers: {
      "Content-Type": "application/json",
    },
  })

  const { data, error } = (await response.json()) as FlowTranslationResponse

  if (!response.ok) {
    throw new Error(
      `Failed to convert diagram to XML: ${JSON.stringify(error)}`
    )
  }

  return data!
}

const XmlPanel = () => {
  const [content, setContent] = useState("")

  return (
    <div>
      <CodeSnippet type="multi">{content}</CodeSnippet>
      <Button
        onClick={() => {
          fetchXmlTranslation(getEipFlow())
            .then((data) => setContent(data))
            .catch((err) => console.error(err))
        }}
      >
        Send
      </Button>
    </div>
  )
}

export default XmlPanel
