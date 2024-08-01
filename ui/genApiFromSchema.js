import $RefParser from "@apidevtools/json-schema-ref-parser"
import { compile } from "json-schema-to-typescript"
import fs from "node:fs"
import path from "node:path"

const schemaUrl =
  "https://raw.githubusercontent.com/OctoConsulting/keip-canvas/6c9e244bd7e77808c81800474c27fe47baa02ab9/schemas/model/json/eipComponentDef.schema.json"

const bundledSchema = await $RefParser.bundle(schemaUrl)

const generatedCode = await compile(bundledSchema, bundledSchema.title, {
  additionalProperties: false,
})

const generatedApiDir = path.resolve("src", "api", "generated")
fs.mkdirSync(generatedApiDir, { recursive: true })
fs.writeFileSync(path.join(generatedApiDir, 'eipComponentDef.ts'), generatedCode)
