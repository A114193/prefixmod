#!/bin/sh
# Gradle start up script for UN*X

# Resolve the real directory of this script
PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="$(dirname "$PRG")/$link"
  fi
done
APP_HOME="$(cd "$(dirname "$PRG")" && pwd -P)"
APP_BASE_NAME=$(basename "$0")

# Locate Java
if [ -z "$JAVA_HOME" ]; then
  if [ -d "/usr/lib/jvm/temurin-21-jdk-amd64" ]; then
    JAVA_HOME="/usr/lib/jvm/temurin-21-jdk-amd64"
  elif command -v java >/dev/null 2>&1; then
    JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
  fi
fi

JAVA_EXE="java"
if [ -n "$JAVA_HOME" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVA_EXE" \
  -Xmx64m -Xms64m \
  $JAVA_OPTS $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
