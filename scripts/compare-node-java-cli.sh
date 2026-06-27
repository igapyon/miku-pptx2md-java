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

mkdir -p "${WORK_DIR}/fixtures" "${WORK_DIR}/node" "${WORK_DIR}/java" "${WORK_DIR}/metadata"

node "${UPSTREAM_DIR}/scripts/miku-pptx2md-cli.mjs" --help > "${WORK_DIR}/metadata/node-help.txt"
java -jar "${JAR_PATH}" --help > "${WORK_DIR}/metadata/java-help.txt"
node "${UPSTREAM_DIR}/scripts/miku-pptx2md-cli.mjs" --version > "${WORK_DIR}/metadata/node-version.txt"
java -jar "${JAR_PATH}" --version > "${WORK_DIR}/metadata/java-version.txt"

if ! diff -u "${WORK_DIR}/metadata/node-help.txt" "${WORK_DIR}/metadata/java-help.txt" > "${WORK_DIR}/metadata/help.diff"; then
  cat > "${WORK_DIR}/metadata/known-help-differences.txt" <<EOF
Node / Java --help output has known intentional differences.

- command examples use Node script execution vs Java jar execution
- versioned Java jar names include the Java runtime version

See:
  ${WORK_DIR}/metadata/help.diff
EOF
fi

if ! diff -u "${WORK_DIR}/metadata/node-version.txt" "${WORK_DIR}/metadata/java-version.txt" > "${WORK_DIR}/metadata/version.diff"; then
  cat > "${WORK_DIR}/metadata/known-version-differences.txt" <<EOF
Node / Java --version output has a known intentional version difference.

Node reports the upstream package version. Java reports the Java runtime version.

See:
  ${WORK_DIR}/metadata/version.diff
EOF
fi

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
  shapeText: fixtureModule.createShapeTextPptx(),
  list: fixtureModule.createListPptx(),
  formatted: fixtureModule.createFormattedTextPptx(),
  hyperlink: fixtureModule.createHyperlinkPptx(),
  table: fixtureModule.createTablePptx(),
  mergedTable: fixtureModule.createMergedTablePptx(),
  chart: fixtureModule.createChartPptx(),
  smartArt: fixtureModule.createSmartArtPptx(),
  notes: fixtureModule.createNotesPptx(),
  video: fixtureModule.createVideoPptx(),
  audio: fixtureModule.createAudioPptx(),
  oleObject: fixtureModule.createOleObjectPptx(),
  comments: fixtureModule.createCommentsPptx(),
  missingImage: fixtureModule.createMissingImagePptx(),
  image: fixtureModule.createImagePptx()
};
for (const [name, bytes] of Object.entries(fixtures)) {
  fs.writeFileSync(path.join(workDir, "fixtures", `${name}.pptx`), bytes);
}
'

for fixture in minimal metadata shapeText list formatted hyperlink table mergedTable chart smartArt notes video audio oleObject comments missingImage image; do
  pptx="${WORK_DIR}/fixtures/${fixture}.pptx"
  node_out="${WORK_DIR}/node/${fixture}.md"
  java_out="${WORK_DIR}/java/${fixture}.md"
  node_summary_json="${WORK_DIR}/node/${fixture}.summary.json"
  java_summary_json="${WORK_DIR}/java/${fixture}.summary.json"

  node "${UPSTREAM_DIR}/scripts/miku-pptx2md-cli.mjs" "${pptx}" --out "${node_out}" --summary-json-out "${node_summary_json}"
  java -jar "${JAR_PATH}" "${pptx}" --out "${java_out}" --summary-json-out "${java_summary_json}"

  if ! diff -u "${node_out}" "${java_out}" > "${WORK_DIR}/${fixture}.diff"; then
    echo "Node / Java Markdown differs for ${fixture}; see ${WORK_DIR}/${fixture}.diff" >&2
    exit 1
  fi
  if ! diff -u "${node_summary_json}" "${java_summary_json}" > "${WORK_DIR}/${fixture}.summary.diff"; then
    echo "Node / Java summary JSON differs for ${fixture}; see ${WORK_DIR}/${fixture}.summary.diff" >&2
    exit 1
  fi
done

pptx="${WORK_DIR}/fixtures/image.pptx"
node_asset_out="${WORK_DIR}/node/image-assets.md"
java_asset_out="${WORK_DIR}/java/image-assets.md"
node_assets_dir="${WORK_DIR}/node/image.assets"
java_assets_dir="${WORK_DIR}/java/image.assets"

rm -rf "${node_assets_dir}" "${java_assets_dir}"
node "${UPSTREAM_DIR}/scripts/miku-pptx2md-cli.mjs" "${pptx}" --out "${node_asset_out}" --assets-dir "${node_assets_dir}"
java -jar "${JAR_PATH}" "${pptx}" --out "${java_asset_out}" --assets-dir "${java_assets_dir}"

if ! diff -u "${node_asset_out}" "${java_asset_out}" > "${WORK_DIR}/image-assets-markdown.diff"; then
  echo "Node / Java asset Markdown differs; see ${WORK_DIR}/image-assets-markdown.diff" >&2
  exit 1
fi
if ! diff -u "${node_assets_dir}/manifest.json" "${java_assets_dir}/manifest.json" > "${WORK_DIR}/image-assets-manifest.diff"; then
  echo "Node / Java asset manifest differs; see ${WORK_DIR}/image-assets-manifest.diff" >&2
  exit 1
fi
if ! cmp -s "${node_assets_dir}/ppt/media/image1.png" "${java_assets_dir}/ppt/media/image1.png"; then
  echo "Node / Java written asset bytes differ for image fixture." >&2
  exit 1
fi

echo "Node / Java CLI comparison passed for checked upstream generated fixtures."
echo "Metadata outputs captured under ${WORK_DIR}/metadata."
