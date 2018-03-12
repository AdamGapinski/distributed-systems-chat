./gradlew clean build
tar -xf ./server/build/distributions/server-1.0.tar
tar -xf ./client/build/distributions/client-1.0.tar
ln -s ./server-1.0/bin/server server.sh
ln -s ./client-1.0/bin/client client.sh
