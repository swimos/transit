rm -rf dist/
mkdir -p dist/

cd ui
npm install
npm run compile && npm run bundle
mkdir -p ../dist/ui
cp -rf index.html dist ../dist/ui/

cd ../server
./gradlew build

cd ../
tar -xf server/build/distributions/swim-transit-3.11.0-SNAPSHOT.tar -C dist/


# sudo docker build ./ -f ./java.Dockerfile -t swimdatafabric/transit:1.0
