cd ui
npm install
npm run compile && npm run bundle
mkdir -p ../server/ui ../server/src/main/resources/ui/
cp -rf index.html dist ../server/ui/
cp -rf index.html dist ../server/src/main/resources/ui/
cd ../server
./gradlew clean run

