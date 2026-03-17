cd "$(dirname "$0")"
cd ..

mvn clean package
cp target/io.openliberty.sample.daytrader8.war  scripts/io.openliberty.sample.daytrader8.war

cd scripts
./switchToWF.sh
cd ..
mvn clean package
cp target/io.openliberty.sample.daytrader8.war  scripts/io.openliberty.sample.daytrader8-WF.war
cd scripts
./switchFromWF.sh

./switchToPayara.sh
cd ..
mvn clean package
cp target/io.openliberty.sample.daytrader8.war  scripts/io.openliberty.sample.daytrader8-Payara.war
cd scripts
./switchFromPayara.sh
