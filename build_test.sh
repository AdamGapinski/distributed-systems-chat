./gradlew tar
mkdir unpacked
tar -C ./unpacked -xf build/Gapiński_Adam_1.tar
cd ./unpacked/Gapiński_Adam_1/distributed-chat
./install.sh
