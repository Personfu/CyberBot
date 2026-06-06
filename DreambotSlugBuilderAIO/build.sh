#!/usr/bin/env bash
#
# Local build for Slug Builder AIO Premium v1.2
#
# Requirements on the build machine:
#   - JDK 11+ (DreamBot runs on JRE 11; release 11 keeps us compatible)
#   - The DreamBot client jar. The launcher downloads it after login to:
#       Windows : %USERPROFILE%\DreamBot\BotData\client.jar
#       macOS   : ~/DreamBot/BotData/client.jar
#       Linux   : ~/DreamBot/BotData/client.jar
#   - lib/runeguardjavaclient-0.1.0.jar (committed in this folder)
#
# Usage:
#   ./build.sh                         # auto-detects ~/DreamBot/BotData/client.jar
#   DREAMBOT_CLIENT=/path/client.jar ./build.sh
#
set -euo pipefail
cd "$(dirname "$0")"

CLIENT="${DREAMBOT_CLIENT:-$HOME/DreamBot/BotData/client.jar}"
RUNEGUARD="lib/runeguardjavaclient-0.1.0.jar"

if [[ ! -f "$CLIENT" ]]; then
  echo "ERROR: DreamBot client.jar not found at: $CLIENT"
  echo "Set DREAMBOT_CLIENT=/full/path/to/client.jar and re-run."
  exit 1
fi
if [[ ! -f "$RUNEGUARD" ]]; then
  echo "ERROR: $RUNEGUARD missing."
  exit 1
fi

OUT="build/classes"
JAR_OUT="build/SlugBuilderAIO.jar"
rm -rf build && mkdir -p "$OUT"

echo "Compiling against:"
echo "  client    = $CLIENT"
echo "  runeguard = $RUNEGUARD"

# Compile. RuneGuard is compile-time only here; it must also be on DreamBot's
# classpath at runtime (drop the jar into DreamBot's Scripts folder or shade it).
find src -name '*.java' > build/sources.txt
javac --release 11 -cp "$CLIENT:$RUNEGUARD" -d "$OUT" @build/sources.txt

# Bundle the script classes + the runeguard client into a single script jar so
# DreamBot can load RuneGuard at runtime.
mkdir -p build/merged
( cd build/merged && jar xf "../../$RUNEGUARD" )
cp -r "$OUT"/* build/merged/
( cd build/merged && jar cf "../../$JAR_OUT" . )

echo "Build OK -> $JAR_OUT"
echo "Copy it to your DreamBot Scripts folder to load it in the client."
