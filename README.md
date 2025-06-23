# M*N Message passing Benchmark

## About

The M*N exercise is a popular test to compare the message passing capability of a platform. Joe Armstrong’s book on Erlang: Software For a Concurrent World, has this exercise to write a benchmark that creates N processes in a ring and sends a message around the ring M times so that a total of N*M messages get sent

This benchmark has been created to test the performance of this exercise in various languages 


## Compiling & Running the Java benchmark

- Navigate to the directory javamsg
- javac -d bin src\javamsg\*.java
- java -Xms256m -Xmx2048m -Djdk.tracePinnedThreads=full -Djdk.trackAllThreads=true -cp  c:\code\mnProcess\javamsg\bin  javamsg.MsgPing n 257  m 3

Note : This tool has been tested using java 21 & requires the JDK to be in your current path

## Compiling & Running the go benchmark 

- Navigate to the directory go-process
- go build . 
- goMnProcess.exe -n 257 -m 3

Note : This tool has been tested using go 1.2.4 and requires the go language compiler to be in your current path

## Results 

Processes = 2,00,000, Times = 30
Hardware  = Intel 12700H 2.3 Ghz

- Java - 1200 MB 5-9% CPU  - 50 - 60 secs
- Go -   1700 MB 5% CPU    - 1.7 secs

