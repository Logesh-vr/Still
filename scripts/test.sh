#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
test_classes="$project_root/build/host-tests"
mkdir -p "$test_classes"

if [[ -n "${JAVA_HOME:-}" ]]; then
    java_bin="$JAVA_HOME/bin/java"
    javac_bin="$JAVA_HOME/bin/javac"
else
    java_bin=java
    javac_bin=javac
fi

"$javac_bin" --release 11 -d "$test_classes" \
    "$project_root/app/src/main/java/app/still/focus/Rules.java" \
    "$project_root/tests/RulesTest.java"
"$java_bin" -cp "$test_classes" app.still.focus.RulesTest
