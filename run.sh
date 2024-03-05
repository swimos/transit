cd ui
npm install
npm run compile && npm run bundle
mkdir -p ../server/src/main/resources/ui/
cp -rf dist/* ../server/src/main/resources/ui/
cd ../server
./gradlew clean run

