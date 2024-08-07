import { useEffect, useState } from "react"

import { llmClientInstance as llmClient } from "./llmClient"

const logKeipAssistantStatus = (available: boolean) => {
  if (available) {
    console.log(
      `Enable Keip Assistant: An LLM server is available at ${llmClient.serverBaseUrl}`
    )
  } else {
    console.log(
      `Disable Keip Assistant: Did not find an LLM server at ${llmClient.serverBaseUrl}`
    )
  }
}

export const useLlmServerStatus = () => {
  const [isAvailable, setIsAvailable] = useState(false)

  useEffect(() => {
    let abortPing: () => void
    void (async () => {
      const result = llmClient.ping()
      abortPing = result.abort
      const success = await result.success
      logKeipAssistantStatus(success)
      setIsAvailable(success)
    })()

    return () => {
      abortPing && abortPing()
    }
  }, [])

  return isAvailable
}
