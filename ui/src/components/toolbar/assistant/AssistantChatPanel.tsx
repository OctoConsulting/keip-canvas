import { magenta50 } from "@carbon/colors"
import {
  Button,
  IconButton,
  InlineLoading,
  Stack,
  TextArea,
  Tile,
} from "@carbon/react"
import { CloseOutline, Send } from "@carbon/react/icons"
import { interactive } from "@carbon/themes"
import { forwardRef, useEffect, useRef, useState } from "react"
import { useAppActions } from "../../../singletons/store"
import { llmClientInstance as llmClient } from "./llmClient"

type ChatEntrySource = "user" | "AI"

interface ChatEntry {
  message: string
  source: ChatEntrySource
}

interface ChatHistoryProps {
  entries: ChatEntry[]
  streamingResponse?: string
}

interface ChatInputProps {
  handleInput: (input: string) => Promise<void>
}

const ChatInput = ({ handleInput }: ChatInputProps) => {
  const [content, setContent] = useState("")
  const [isWaiting, setWaiting] = useState(false)

  const submit = () => {
    if (content.length > 0) {
      setWaiting(true)
      handleInput(content)
        .then(() => setContent(""))
        .catch((err) => console.error(err))
        .finally(() => setWaiting(false))
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      submit()
    }
  }

  return (
    <div className="chat-input-container">
      <TextArea
        id="chat-input"
        labelText="chat-input"
        hideLabel
        placeholder="Enter Prompt..."
        rows={1}
        value={content}
        onChange={(e) => setContent(e.target.value)}
        onKeyDown={handleKeyDown}
        disabled={isWaiting}
      />

      {isWaiting ? (
        <>
          <InlineLoading className="chat-input-waiting" status="active" />
          <Button
            style={{ paddingBlockStart: "11px" }}
            size="lg"
            kind="danger--tertiary"
            hasIconOnly
            iconDescription="cancel"
            renderIcon={() => <CloseOutline size={24} />}
            onClick={() => llmClient.abortPrompt()}
          />
        </>
      ) : (
        <IconButton
          label="submit"
          size="lg"
          disabled={content.length === 0}
          onClick={submit}
        >
          <Send />
        </IconButton>
      )}
    </div>
  )
}

const getEntryColor = (source: ChatEntrySource) => {
  return {
    borderColor: source === "AI" ? magenta50 : interactive,
  }
}

const ChatHistory = forwardRef<HTMLParagraphElement, ChatHistoryProps>(
  (props, ref) => {
    const allEntries = props.streamingResponse
      ? [
          ...props.entries,
          { message: props.streamingResponse, source: "AI" } as ChatEntry,
        ]
      : props.entries

    return (
      <Tile className="chat-history">
        <Stack gap={5}>
          {allEntries.map((entry, idx) => (
            <span
              key={idx}
              className="chat-history-entry"
              style={getEntryColor(entry.source)}
            >
              <p>{entry.message}</p>
            </span>
          ))}
        </Stack>
        {/* Dummy element to enable scrolling to bottom of history*/}
        <p ref={ref} />
      </Tile>
    )
  }
)

ChatHistory.displayName = "ChatHistory"

const AssistantChatPanel = () => {
  const chatHistoryEndRef = useRef<HTMLParagraphElement | null>(null)
  // TODO: Create a new action to set nodes/edges directly from object instead of JSON string.
  const { importFlowFromJson } = useAppActions()
  const [chatEntries, setChatEntries] = useState<ChatEntry[]>([])
  const [streamingResponse, setStreamingResponse] = useState("")

  const handleStreamUpdate = (chunk: string) =>
    setStreamingResponse((prev) => prev + chunk)

  const sendPrompt = async (input: string) => {
    const response = await llmClient.prompt(input, handleStreamUpdate)
    if (response.success) {
      importFlowFromJson(response.data)
    }
    setChatEntries((prev) => [
      ...prev,
      { message: input, source: "user" },
      { message: response.data, source: "AI" },
    ])
    setStreamingResponse("")
  }

  useEffect(() => {
    chatHistoryEndRef.current?.scrollIntoView()
  }, [streamingResponse])

  // TODO: Display an error pop-up if LLM prompt fails
  return (
    <>
      <ChatHistory
        entries={chatEntries}
        streamingResponse={streamingResponse}
        ref={chatHistoryEndRef}
      />
      <ChatInput handleInput={sendPrompt} />
    </>
  )
}

export default AssistantChatPanel
