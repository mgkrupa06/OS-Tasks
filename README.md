# OS Tasks - Multithreading Assignment

This repository contains the implementations of two Operating Systems
multithreading tasks using Java.

## Tasks

### Task 1: Producer-Consumer Problem

**Question:**  
Implement the Producer-Consumer problem using threads in the Java framework.

The program demonstrates communication between a Producer and a Consumer
using a shared bounded circular buffer.

The Producer creates parcels and places them into the conveyor, while the
Consumer removes and processes them.

#### Concepts Used

- Java Threads
- Producer-Consumer model
- Bounded circular buffer
- Thread synchronization
- `synchronized`
- `wait()`
- `notifyAll()`
- `join()`
- Shared resource management

The conveyor has a capacity of 4 parcels. The Producer generates 15 parcels,
and the Consumer processes all 15 parcels.

#### File

SmartConveyor.java
Task 2: Matrix Multiplication Using Threads

Question:
Implement Matrix multiplication of 2 matrices using Threads. Minimum 100 rows
& 100 columns. Every multiplication operation must be on a thread. Use any
framework - TensorFlow specified. Along with this, demonstrate the working
with an animation.

The program performs multiplication of two 100 × 100 matrices using
Java Virtual Threads.

For every result cell, 100 individual scalar multiplication operations are
performed. A separate Virtual Thread is created for each multiplication
operation.

Therefore, the complete calculation consists of:

100 × 100 result cells
×
100 multiplication operations per cell
=
1,000,000 multiplication operations

The partial results are synchronized before being added to the corresponding
result cell.

Concepts Used
Java Virtual Threads
Multithreading
Matrix multiplication
Thread synchronization
AtomicInteger
TensorFlow Java
Java Swing
Progress tracking
Result verification
Animation

A Swing-based animation demonstrates the matrix multiplication visually.

The animation displays:

Matrix A       Matrix B       Matrix C (building live)

Matrix C is progressively built while the threaded multiplication is taking
place.

The animation also shows:

Current processing row
Current processing column
Completed cells
Completed multiplication operations
Progress bars
Calculation status
TensorFlow Verification

After the threaded matrix multiplication is completed, TensorFlow Java is
used to independently calculate the matrix multiplication result.

The Java threaded result is compared with the TensorFlow result.

The program displays:

TensorFlow verification: PASSED

when both results are equal.

Files
MatrixMultiplication.java
pom.xml
Technologies Used
Java 21
Java Virtual Threads
Java Swing
TensorFlow Java
Maven
Multithreading
Synchronization
Project Structure
OS_Tasks/
│
├── src/
│   └── main/
│       └── java/
│           ├── SmartConveyor.java
│           └── MatrixMultiplication.java
│
├── pom.xml
└── README.md
Requirements

Before running the project, make sure the following are installed:

Java 21
Maven

TensorFlow Java is automatically handled through the Maven dependency
specified in pom.xml.

How to Run
Task 1

From the project root:

javac src/main/java/SmartConveyor.java

Then run:

java -cp src/main/java SmartConveyor

The Producer and Consumer threads will start and communicate through the
shared conveyor.

Task 2

Task 2 uses Maven because TensorFlow Java is required.

From the project root, compile the project:

mvn clean compile

After successful compilation, run:

mvn exec:java

A Swing animation window will open and show the matrix multiplication
progress.

Expected Task 2 Information

The program uses:

Matrix A              : 100 x 100
Matrix B              : 100 x 100
Result Matrix         : 100 x 100
Thread Technology     : Java Virtual Threads
Framework             : TensorFlow
Total Multiplications : 1,000,000

After the threaded computation, the program reports the number of completed
operations and cells and performs TensorFlow verification.

Learning Outcomes

These tasks demonstrate practical concepts of multithreading and
synchronization, including:

Creating and managing threads
Producer-Consumer synchronization
Shared resource handling
Bounded buffers
wait() and notifyAll()
Thread coordination
Java Virtual Threads
Synchronization of shared results
Atomic counters
Matrix multiplication using threads
TensorFlow-based result verification
Visualizing computation using Java Swing

Author
M G Krupa
B.Tech - Information Science and Engineering
NMAM Institute of Technology, Nitte
