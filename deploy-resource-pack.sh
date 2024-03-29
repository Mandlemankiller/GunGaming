#!/bin/bash

RESOURCE_PACK_FOLDER="resource-pack"
OUTPUT_FOLDER="target"
HASH_FILE=${OUTPUT_FOLDER}/resource-pack.hash
ZIP_FILE=${OUTPUT_FOLDER}/resource-pack.zip

mkdir ${OUTPUT_FOLDER} >/dev/null 2>&1

rm "${HASH_FILE}" >/dev/null 2>&1
rm "${ZIP_FILE}" >/dev/null 2>&1

cd ${RESOURCE_PACK_FOLDER} && zip -r ../"${ZIP_FILE}" . && cd ..

echo Generating SHA-1...
sha1sum "${ZIP_FILE}" | awk '{ print $1 }' | tr -d '\n' >"${HASH_FILE}"
echo Done!