import {
  Breadcrumb,
  BreadcrumbItem,
  Button,
  ContainedList,
  ContainedListItem,
  Dropdown,
  Layer,
  Modal,
  Stack,
} from "@carbon/react"

import { Close } from "@carbon/react/icons"
import { ReactNode, useState } from "react"
import { createPortal } from "react-dom"
import { DEFAULT_NAMESPACE } from "../../api/flow"
import {
  EipChildElement,
  EipComponent,
} from "../../api/generated/eipComponentDef"
import { lookupEipComponent } from "../../singletons/eipDefinitions"
import { disableChild, enableChild } from "../../singletons/store/appActions"
import { useGetEnabledChildren } from "../../singletons/store/getterHooks"
import { getEipId } from "../../singletons/store/storeViews"

interface ChildSelectorProps {
  parentId: string
  childOptions: EipChildElement[]
}

interface ChildrenDisplayProps {
  parentId: string
  enabledChildren: string[]
  updatePath: (childId: string) => void
}

interface ChildModalProps {
  rootId: string
  open: boolean
  setOpen: (open: boolean) => void
}

// TODO: Should this be a utility method in the EipComponentDef module?
const getEipDefinition = (rootEipDef: EipComponent, path: string[]) => {
  let children = rootEipDef.childGroup?.children

  if (path.length == 1) {
    return children
  }

  for (const id of path.slice(1)) {
    const name = getEipId(id)?.name
    const child = children?.find((c) => c.name === name)
    children = child?.childGroup?.children
  }
  return children
}

const ChildSelector = ({ parentId, childOptions }: ChildSelectorProps) => (
  <Dropdown
    id={"dropdown-child-selector"}
    label="Select child..."
    items={[null, ...childOptions]}
    itemToString={(child) => child?.name ?? ""}
    onChange={({ selectedItem }) => {
      selectedItem &&
        enableChild(parentId, {
          namespace: DEFAULT_NAMESPACE,
          name: selectedItem.name,
        })
    }}
    selectedItem={null}
    titleText={"Add a child"}
  />
)

const ChildrenDisplay = ({
  parentId,
  enabledChildren,
  updatePath,
}: ChildrenDisplayProps) => (
  <Layer>
    <ContainedList
      className="child-modal__list"
      label="Children"
      kind="on-page"
    >
      {enabledChildren.map((childId) => {
        const eipId = getEipId(childId)
        return (
          <ContainedListItem
            key={childId}
            action={
              <Button
                kind="ghost"
                iconDescription="Delete"
                hasIconOnly
                renderIcon={Close}
                tooltipPosition="left"
                onClick={() => disableChild(parentId, childId)}
              />
            }
            onClick={() => updatePath(childId)}
          >
            <span>{eipId?.name}</span> <span>({childId})</span>
          </ContainedListItem>
        )
      })}
    </ContainedList>
  </Layer>
)

export const ChildManagementModal = ({
  rootId,
  open,
  setOpen,
}: ChildModalProps) => {
  const [path, setPath] = useState([rootId])
  const parentId = path[path.length - 1]
  const enabledChildren = useGetEnabledChildren(parentId)

  const rootEipId = getEipId(rootId)
  const rootEipDef = rootEipId && lookupEipComponent(rootEipId)

  const childOptions = rootEipDef && getEipDefinition(rootEipDef, path)

  let modalContent: ReactNode
  if (childOptions && childOptions.length > 0) {
    modalContent = (
      <>
        <ChildSelector parentId={parentId} childOptions={childOptions} />
        <ChildrenDisplay
          parentId={parentId}
          enabledChildren={enabledChildren}
          updatePath={(childId) => setPath((path) => [...path, childId])}
        />
      </>
    )
  } else {
    modalContent = <p className="child-modal__list-placeholder">No available children</p>
  }

  // TODO: Look into using a ComposedModal rather than relying on 'passiveModal' prop
  // TODO: Capture browser "back" and "forward" clicks and apply to child path navigation
  return createPortal(
    <Modal
      className="child-modal"
      open={open}
      onRequestClose={() => setOpen(false)}
      modalHeading="Update Children"
      modalLabel={rootEipDef?.name}
      passiveModal
      size="md"
    >
      <Stack orientation="vertical" gap={7}>
        <Breadcrumb>
          {path.map((id, idx) => (
            <BreadcrumbItem
              key={idx}
              onClick={() => setPath((prev) => prev.slice(0, idx + 1))}
            >
              {getEipId(id)?.name}
            </BreadcrumbItem>
          ))}
        </Breadcrumb>
        {modalContent}
      </Stack>
    </Modal>,
    document.body
  )
}
