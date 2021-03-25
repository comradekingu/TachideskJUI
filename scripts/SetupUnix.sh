#!/bin/bash

if [ "$(basename "$(pwd)")" = "scripts" ]; then
  cd ..
fi

mkdir -p "tmp"

echo "Getting latest Tachidesk build files"
TARBALL_LINK="$(curl -s "https://api.github.com/repos/Suwayomi/Tachidesk/releases/latest" | grep -o "https.*tarball\/[a-zA-Z0-9.]*")"

curl -L "$TARBALL_LINK" -o tmp/Tachidesk.tar

tar -xvf tmp/Tachidesk.tar -C tmp

TACHIDESK_FOLDER=$(find tmp -type d -regex ".*Tachidesk-[a-z0-9]*")

pushd "$TACHIDESK_FOLDER" || exit

echo "Setting up android.jar"
AndroidCompat/getAndroid.sh

echo "Building Tachidesk.jar"
./gradlew :server:shadowJar -x :webUI:copyBuild

TACHIDESK_JAR=$(find server/build -type f -regex ".*\.jar")

popd || exit

echo "Copying Tachidesk.jar to resources folder..."
mv "$TACHIDESK_FOLDER/$TACHIDESK_JAR" src/main/resources/Tachidesk.jar

echo "Cleaning up..."
rm -rf "tmp"

echo "Done!"