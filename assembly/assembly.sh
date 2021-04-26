#!/bin/bash
rm -rf src/main/resources/
rm -rf src/main/scala
rm -rf target/
mkdir -p src/main/resources/
mkdir -p src/main/scala
cp -r ../src/test/resources/* src/main/resources/
cp -r ../src/test/scala/* src/main/scala
cp -r ../src/main/scala/* src/main/scala

mvn clean package



#java -cp assembly-1.0-SNAPSHOT.jar io.gatling.app.Gatling -s test.KafkaTest