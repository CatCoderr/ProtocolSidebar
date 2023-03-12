echo "Compiling latest ProtocolSidebar version..."
mvn clean install

echo "Compiling test plugin..."
cd test-plugin/ && mvn clean package && cd ..

echo "Downloading ProtocolLib..."
wget https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar -O server/data/plugins/ProtocolLib.jar

echo "Downloading ViaVersion..."
wget https://github.com/ViaVersion/ViaVersion/releases/download/4.5.1/ViaVersion-4.5.1.jar -O server/data/plugins/ViaVersion.jar

echo "Copying test plugin..."
cp test-plugin/target/bukkit-sidebar-test-plugin-4.0.0.jar server/data/plugins/BukkitSidebarTestPlugin.jar

echo "Starting server..."
cd server && docker-compose up -d && docker attach server-mc-1