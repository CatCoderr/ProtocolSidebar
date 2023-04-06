echo "Compiling latest ProtocolSidebar version..."
./gradlew clean shadowJar

VIA_VERSION=4.5.1

mkdir -p server/data/plugins

echo "Copying ProtocolSidebar..."
cp bin/ProtocolSidebar-*.jar server/data/plugins/ProtocolSidebar.jar

echo "Downloading ViaVersion..."
wget https://github.com/ViaVersion/ViaVersion/releases/download/$VIA_VERSION/ViaVersion-$VIA_VERSION.jar -O server/data/plugins/ViaVersion.jar

echo "Starting server..."
cd server && docker-compose up -d && docker attach server-mc-1