import $RefParser from "@apidevtools/json-schema-ref-parser"
import { compile } from "json-schema-to-typescript"
import fs from "node:fs"
import { writeFile } from "node:fs/promises"
import path from "node:path"

// TODO: Look into avoiding duplication of generated types when multiple top-level schemas 
// reference the same common schemas.

const COMMIT_HASH = "3963da5fd645cfa674e1654d75b9d680d2953544"

const SCHEMAS = ["eipComponentDef.schema.json", "eipFlow.schema.json"]

const SCHEMAS_BASE_URL = "https://raw.githubusercontent.com"

const GENERATED_SOURCE_DIR = path.resolve("src", "api", "generated")

const buildUrl = (schemaFileName) => (new URL(`OctoConsulting/keip-canvas/${COMMIT_HASH}/schemas/model/json/${schemaFileName}`, SCHEMAS_BASE_URL)).toString()

const generateSources = async (schemaFileName) => {
  const bundledSchema = await $RefParser.bundle(buildUrl(schemaFileName))

  const generatedCode = await compile(bundledSchema, bundledSchema.title, {
    additionalProperties: false,
  })

  const outputFile = `${schemaFileName.split('.')[0]}.ts`
  await writeFile(path.join(GENERATED_SOURCE_DIR, outputFile), generatedCode)
}


fs.mkdirSync(GENERATED_SOURCE_DIR, { recursive: true })
await Promise.all(SCHEMAS.map(generateSources))
