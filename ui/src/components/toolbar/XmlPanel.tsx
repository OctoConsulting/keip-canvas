import { Button, CopyButton } from "@carbon/react"
import hljs from "highlight.js/lib/core"
import xml from "highlight.js/lib/languages/xml"
import { useState } from "react"
import Editor from "react-simple-code-editor"
import { EipFlow } from "../../api/generated/eipFlow"
import { getEipFlow } from "../../singletons/store"

// highlight.js theme
import "highlight.js/styles/intellij-light.css"

hljs.registerLanguage("xml", xml)

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
  const queryStr = new URLSearchParams({ prettyPrint: "true" }).toString()
  const response = await fetch("http://localhost:8080?" + queryStr, {
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
    <>
      <Editor
        className="xml-editor-container"
        value={content}
        onValueChange={(code) => code}
        readOnly
        highlight={(code) => hljs.highlight(code, { language: "xml" }).value}
        padding={16}
      />

      {/* TODO: Remove */}
      <div style={{ display: "flex", gap: "16px" }}>
        <Button
          onClick={() => {
            fetchXmlTranslation(getEipFlow())
              .then((data) => setContent(data))
              .catch((err) => console.error(err))
          }}
        >
          Send
        </Button>
        <CopyButton
          align="top"
          onClick={() => navigator.clipboard.writeText(content)}
        />
      </div>
    </>
  )
}

export default XmlPanel
