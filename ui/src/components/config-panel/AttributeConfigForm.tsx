import { Information } from "@carbon/icons-react"
import {
  Accordion,
  AccordionItem,
  Form,
  FormLabel,
  Select,
  SelectItem,
  Stack,
  TextInput,
  Toggle,
  Tooltip,
} from "@carbon/react"
import {
  ChangeEvent,
  MouseEventHandler,
  ReactNode,
  ReactSVGElement,
  useMemo,
} from "react"
import { Attribute } from "../../api/generated/eipComponentDef"
import { useAppActions, useGetEipAttribute } from "../../singletons/store"
import debounce from "../../utils/debounce"

interface DescriptionWrapperProps {
  id: string
  description: string | undefined
  children: ReactNode
}

interface AttributeInputFactoryProps {
  attr: Attribute
  id: string
  parentId: string
}

interface AttributeInputProps<T> {
  id: string
  parentId: string
  attr: Attribute
  attrValue: T
}

interface AttributeFormProps {
  attrs: Attribute[]
  id: string
  parentId: string
}

const AttributeSelectInput = ({
  attr,
  attrValue,
  id,
  parentId,
}: AttributeInputProps<string>) => {
  const { updateEipAttribute } = useAppActions()
  const emptySelect = ""
  const options = useMemo(
    () =>
      attr.default
        ? attr.restriction!.values!
        : [emptySelect, ...attr.restriction!.values!],
    [attr.restriction, attr.default]
  )

  const handleSelect = (ev: ChangeEvent<HTMLSelectElement>) =>
    updateEipAttribute(id, parentId, attr.name, ev.target.value)

  return (
    <DescriptionTooltipWrapper id={attr.name} description={attr.description}>
      <Select
        id={attr.name}
        labelText={attr.name}
        defaultValue={attrValue ?? attr.default}
        hideLabel={Boolean(attr.description)}
        onChange={handleSelect}
      >
        {options.map((item) => (
          <SelectItem key={item} value={item} text={item} />
        ))}
      </Select>
    </DescriptionTooltipWrapper>
  )
}

const AttributeBoolInput = ({
  attr,
  attrValue,
  id,
  parentId,
}: AttributeInputProps<boolean>) => {
  const { updateEipAttribute } = useAppActions()

  const handleToggle = (checked: boolean) =>
    updateEipAttribute(id, parentId, attr.name, checked)

  return (
    <DescriptionTooltipWrapper id={attr.name} description={attr.description}>
      <div style={{ display: "block" }}>
        <Toggle
          id={attr.name}
          labelText={attr.description ? "" : attr.name}
          labelA=""
          labelB=""
          defaultToggled={attrValue ?? Boolean(attr.default)}
          hideLabel={Boolean(attr.description)}
          onToggle={handleToggle}
        />
      </div>
    </DescriptionTooltipWrapper>
  )
}

const getDefaultStr = (attr: Attribute) =>
  attr.default ? String(attr.default) : ""

const AttributeTextInput = ({
  attr,
  attrValue,
  id,
  parentId,
}: AttributeInputProps<string>) => {
  const { updateEipAttribute } = useAppActions()

  const handleTextUpdates = useMemo(
    () =>
      debounce(
        (ev: ChangeEvent<HTMLInputElement>) =>
          updateEipAttribute(id, parentId, attr.name, ev.target.value),
        1000
      ),
    [id, parentId, attr.name, updateEipAttribute]
  )

  return (
    <DescriptionTooltipWrapper id={attr.name} description={attr.description}>
      <TextInput
        id={attr.name}
        labelText={attr.name}
        defaultValue={attrValue ?? getDefaultStr(attr)}
        hideLabel={Boolean(attr.description)}
        onChange={handleTextUpdates}
      />
    </DescriptionTooltipWrapper>
  )
}

const DescriptionTooltipWrapper = (props: DescriptionWrapperProps) => {
  if (!props.description) {
    return props.children
  }

  const tooltipDivId = `tooltip-hack-${props.id}`

  // Workaround for tooltip popover not 'escaping' the side panel boundary.
  // The side panel requires vertical scrolling and so this does not allow overflow-x to be set to visible.
  // This workaround detachesuses JS to position the popover dynamically.
  // Long-term we would be better served by implementing our own Tooltip based on the Popover component,
  // to have greater control over the underlying DOM elements.
  const tooltipWorkaroundHandler: MouseEventHandler<ReactSVGElement> = (e) => {
    const icon = e.target as Element
    const tooltipParent = document.getElementById(tooltipDivId)

    const tooltip = tooltipParent?.getElementsByClassName(
      "cds--tooltip-content"
    )[0] as HTMLElement

    const rect = icon.getBoundingClientRect()
    tooltip.style.left = `${rect.left}px`
    tooltip.style.top = `${rect.top}px`
    tooltip.style.position = "fixed"
  }

  return (
    <div id={tooltipDivId}>
      <FormLabel className="form-input-label">{props.id}</FormLabel>
      <Tooltip align="top" label={props.description}>
        <Information onMouseEnter={tooltipWorkaroundHandler} />
      </Tooltip>
      {props.children}
    </div>
  )
}

const AttributeInput = (props: AttributeInputFactoryProps) => {
  const attrValue = useGetEipAttribute(
    props.id,
    props.parentId,
    props.attr.name
  )

  switch (props.attr.type) {
    // TODO: Handle number types with more specific input components (e.g. NumberInput, Slider).
    case "number":
    case "string":
      if (props.attr.restriction?.values) {
        return (
          <AttributeSelectInput {...props} attrValue={attrValue as string} />
        )
      }
      return <AttributeTextInput {...props} attrValue={attrValue as string} />

    case "boolean":
      return <AttributeBoolInput {...props} attrValue={attrValue as boolean} />
  }
}

const AttributeConfigForm = (props: AttributeFormProps) => {
  const required = props.attrs
    .filter((attr) => attr.required)
    .sort((a, b) => a.name.localeCompare(b.name))
  const optional = props.attrs
    .filter((attr) => !attr.required)
    .sort((a, b) => a.name.localeCompare(b.name))

  const addPadding = "side-panel-padded-container"

  return (
    <Form onSubmit={(e) => e.preventDefault()}>
      <Accordion isFlush size="lg">
        {required.length ? (
          <AccordionItem title="Required" open>
            <Stack gap={6} className={addPadding}>
              {required.map((attr) => (
                <AttributeInput {...props} key={attr.name} attr={attr} />
              ))}
            </Stack>
          </AccordionItem>
        ) : null}
        {optional.length ? (
          <AccordionItem title="Optional">
            <Stack gap={6} className={addPadding}>
              {optional.map((attr) => (
                <AttributeInput {...props} key={attr.name} attr={attr} />
              ))}
            </Stack>
          </AccordionItem>
        ) : null}
      </Accordion>
    </Form>
  )
}

export default AttributeConfigForm
