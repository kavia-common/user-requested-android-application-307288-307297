#!/bin/bash
cd /home/kavia/workspace/code-generation/user-requested-android-application-307288-307297/android_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

