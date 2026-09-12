#!/usr/bin/env bash
set -euo pipefail

BUILD_DIR="build/classes"
OUT_DIR="out/compiled"
LIBS="libs/json-20230618.jar:libs/jsoup-1.17.2.jar"

echo "Cleaning build directory..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"
mkdir -p "$OUT_DIR"

echo "Collecting source files..."
find "src/de/hellbz/forge" -name "*.java" -not -path "*/NotInUse/*" -not -path "*/test/*" > sources.txt

echo "Compiling..."
javac -encoding UTF-8 -cp "$LIBS" -d "$BUILD_DIR" "@sources.txt"

echo "Copying resources..."
mkdir -p "$BUILD_DIR/res"
cp -r res/* "$BUILD_DIR/res/" 2>/dev/null || true

echo "Extracting libraries..."
cd "$BUILD_DIR"
jar xf "../../libs/json-20230618.jar"
jar xf "../../libs/jsoup-1.17.2.jar"
rm -rf "META-INF/maven" 2>/dev/null || true
cp "../../src/META-INF/MANIFEST.MF" "META-INF/MANIFEST.MF"
cd "../.."

echo "Building JAR..."
jar cfm "$OUT_DIR/minecraft_server.jar" "$BUILD_DIR/META-INF/MANIFEST.MF" -C "$BUILD_DIR" .

rm -f sources.txt
echo "Done. Output: $OUT_DIR/minecraft_server.jar"