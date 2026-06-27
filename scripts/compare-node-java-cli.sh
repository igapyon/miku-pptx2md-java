#!/bin/sh
set -eu

REPO_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
UPSTREAM_DIR="${MIKU_PPTX2MD_UPSTREAM_DIR:-$(CDPATH= cd -- "${REPO_DIR}/../miku-pptx2md" && pwd)}"
WORK_DIR="${MIKU_PPTX2MD_COMPARE_DIR:-${REPO_DIR}/workplace/node-java-cli-compare}"

VERSION="$(awk 'match($0, /<version>[^<]+<\/version>/) { value=$0; sub(/^.*<version>/, "", value); sub(/<\/version>.*$/, "", value); print value; exit }' "${REPO_DIR}/pom.xml")"
JAR_PATH="${REPO_DIR}/target/miku-pptx2md-${VERSION}.jar"

if [ ! -f "${JAR_PATH}" ]; then
  echo "Missing Java runtime jar: ${JAR_PATH}" >&2
  echo "Run: mvn package" >&2
  exit 1
fi

mkdir -p "${WORK_DIR}/fixtures" "${WORK_DIR}/node" "${WORK_DIR}/java"

UPSTREAM_DIR="${UPSTREAM_DIR}" WORK_DIR="${WORK_DIR}" node --input-type=module -e '
import fs from "node:fs";
import path from "node:path";
import { pathToFileURL } from "node:url";

const upstreamDir = process.env.UPSTREAM_DIR;
const workDir = process.env.WORK_DIR;
const fixtureModule = await import(pathToFileURL(path.join(upstreamDir, "tests/pptx-fixture.mjs")));
const fixtures = {
  minimal: fixtureModule.createMinimalPptx(),
  metadata: fixtureModule.createMetadataPptx(),
  list: fixtureModule.createListPptx(),
  formatted: fixtureModule.createFormattedTextPptx(),
  hyperlink: fixtureModule.createHyperlinkPptx(),
  table: fixtureModule.createTablePptx(),
  notes: fixtureModule.createNotesPptx(),
  missingImage: fixtureModule.createMissingImagePptx()
};
for (const [name, bytes] of Object.entries(fixtures)) {
  fs.writeFileSync(path.join(workDir, "fixtures", `${name}.pptx`), bytes);
}
'

for fixture in minimal metadata list formatted hyperlink table notes missingImage; do
  pptx="${WORK_DIR}/fixtures/${fixture}.pptx"
  node_out="${WORK_DIR}/node/${fixture}.md"
  java_out="${WORK_DIR}/java/${fixture}.md"

  node "${UPSTREAM_DIR}/scripts/miku-pptx2md-cli.mjs" "${pptx}" --out "${node_out}"
  java -jar "${JAR_PATH}" "${pptx}" --out "${java_out}"

  if ! diff -u "${node_out}" "${java_out}" > "${WORK_DIR}/${fixture}.diff"; then
    echo "Node / Java Markdown differs for ${fixture}; see ${WORK_DIR}/${fixture}.diff" >&2
    exit 1
  fi
done

echo "Node / Java CLI comparison passed for representative fixtures."
