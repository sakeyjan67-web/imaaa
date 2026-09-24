#!/usr/bin/env python3
import os
import shutil
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent
WRAPPER_DIR = ROOT / 'gradle' / 'wrapper'


def write_text(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding='utf-8')


def ensure_wrapper_jar():
    jar_path = WRAPPER_DIR / 'gradle-wrapper.jar'
    if jar_path.exists():
        return jar_path

    url = 'https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar'
    print(f'Downloading Gradle wrapper jar from {url}')
    with urllib.request.urlopen(url, timeout=30) as response:
        data = response.read()
    jar_path.parent.mkdir(parents=True, exist_ok=True)
    jar_path.write_bytes(data)
    return jar_path


def write_wrapper_files():
    write_text(
        WRAPPER_DIR / 'gradle-wrapper.properties',
        """distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"""
    )

    write_text(
        ROOT / 'gradlew',
        """#!/usr/bin/env sh
set -eu
APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAVACMD=${JAVA_HOME:+$JAVA_HOME/bin/java}
if [ -z "${JAVACMD:-}" ]; then
  JAVACMD=java
fi
exec "$JAVACMD" ${JAVA_OPTS:-} -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
"""
    )

    write_text(
        ROOT / 'gradlew.bat',
        """@echo off
setlocal
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
set JAVA_EXE=java
if defined JAVA_HOME set JAVA_EXE=%JAVA_HOME%\bin\java.exe
"%JAVA_EXE%" %JAVA_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
"""
    )

    ensure_wrapper_jar()


def main():
    write_wrapper_files()
    print('Gradle wrapper files generated successfully.')


if __name__ == '__main__':
    main()
