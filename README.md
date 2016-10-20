# JumpCloud  

**Requirements**  
Java 8
Maven +3.0

**Installation**  
From the command line: 
  
1. git clone https://github.com/RockChalkJay/JumpCloud.git  
  
2. mvn clean package --> This will compile the program and run all unit tests  

**Running**  
From the command line:  
  
1. java ./target/password-hash-1.0-SNAPSHOT.jar --server.port=8080 --> --server.port can be set to any port you would like the server to run on.  
  
2. To run in debug mode: java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar ./target/password-hash-1.0-SNAPSHOT.jar --server.port=8082

