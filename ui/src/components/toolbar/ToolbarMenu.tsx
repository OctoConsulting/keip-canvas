import { IconButton, TableToolbar, TableToolbarContent } from "@carbon/react"
import { MachineLearning, Xml } from "@carbon/react/icons"
import { useState } from "react"
import AssistantChatPanel from "./assistant/AssistantChatPanel"
import { useLlmServerStatus } from "./assistant/llmStatusHook"
import XmlPanel from "./XmlPanel"

interface PanelButtonProps {
  name: string
  icon: React.ReactNode
  disabled: boolean
  selected: boolean
  handleClick: () => void
}

interface PanelRef {
  icon: React.ReactNode
  panel: React.ReactNode
}

const panels: Record<string, PanelRef> = {
  "keip-assistant": {
    icon: <MachineLearning />,
    panel: <AssistantChatPanel />,
  },
  xml: {
    icon: <Xml />,
    panel: <XmlPanel />,
  },
}

const PanelButton = ({
  name,
  icon,
  disabled,
  selected,
  handleClick,
}: PanelButtonProps) => (
  <IconButton
    className={
      selected ? "toolbar-button toolbar-button-selected" : "toolbar-button"
    }
    label={name}
    align="left"
    kind="secondary"
    size="lg"
    onClick={handleClick}
    disabled={disabled}
  >
    {icon}
  </IconButton>
)

const ToolbarMenu = () => {
  const [openPanel, setOpenPanel] = useState("")
  const isLlmServerAvailable = useLlmServerStatus()

  const disabledPanels = new Set<string>()
  !isLlmServerAvailable && disabledPanels.add("keip-assistant")

  const handlePanelButtonClick = (name: string) => () => {
    openPanel === name ? setOpenPanel("") : setOpenPanel(name)
  }

  const display = openPanel ? { height: "30vh" } : { height: "2rem" }

  // TODO: Can probably ditch Carbon's Toolbar
  return (
    <div className="main-toolbar-container" style={display}>
      <TableToolbar size="sm">
        <TableToolbarContent className="toolbar-content">
          {Object.entries(panels).map(([panelName, val]) => (
            <PanelButton
              key={panelName}
              name={panelName}
              icon={val.icon}
              disabled={disabledPanels.has(panelName)}
              selected={panelName === openPanel}
              handleClick={handlePanelButtonClick(panelName)}
            />
          ))}
        </TableToolbarContent>
      </TableToolbar>

      {openPanel && panels[openPanel].panel}
    </div>
  )
}

export default ToolbarMenu
