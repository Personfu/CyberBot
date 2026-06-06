#!/usr/bin/env bash
#
# LOCAL TYPE-CHECK build using the bundled DreamBot API stubs.
#
# This does NOT need the real DreamBot client.jar. It compiles the whole
# project against minimal stub signatures in stubs/ so you (and CI) can verify
# the code type-checks. The produced classes are for verification only - the
# runnable script jar must be built with build.sh against the real client.jar.
#
set -euo pipefail
cd "$(dirname "$0")"

STUB_OUT="build/stubs"
PROJ_OUT="build/local-classes"
RUNEGUARD="lib/runeguardjavaclient-0.1.0.jar"

rm -rf build/stubs build/local-classes
mkdir -p "$STUB_OUT" "$PROJ_OUT"

echo "[1/2] Compiling DreamBot API stubs..."
find stubs -name '*.java' > build/stub-sources.txt
javac -d "$STUB_OUT" @build/stub-sources.txt
( cd "$STUB_OUT" && jar cf ../dreambot-stubs.jar . )

echo "[2/2] Compiling project against stubs + RuneGuard..."
find src -name '*.java' > build/sources.txt
javac -cp "build/dreambot-stubs.jar:$RUNEGUARD" -d "$PROJ_OUT" @build/sources.txt

echo "OK - project type-checks. ($(find "$PROJ_OUT" -name '*.class' | wc -l) classes)"
echo "Run ./build.sh with the real client.jar to produce a runnable script jar."
