Compiling & Running the Java benchmark

Navigate to the directory javamsg
javac -d bin src\javamsg\*.java
java -Xms256m -Xmx2048m -Djdk.tracePinnedThreads=full -Djdk.trackAllThreads=true -cp  c:\code\mnProcess\javamsg\bin  javamsg.MsgPing n 257  m 3

Compiling & Running the go benchmark 

Navigate to the directory go-process
go build . 
