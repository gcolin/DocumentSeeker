# Document Seeker

This is a tools to search for a file locally with optical character recognition.

It is useful if
* you want index image or PDF file with optical character recognition (OCR)
* you do not want to share your data with a third party
* you do not have an Internet connection
* you want a full control of when the software is running or not

How it works
* files are reviewed with Apache Tika
* Apache Tika uses Tesseract as OCR
* The extracted data is stored in a Lucene database
* Lucene database gives results very quickly

## Supported operating system

### Windows 10

There are 2 versions:
* the [installer version](https://github.com/gcolin/DocumentSeeker/releases/download/docseeker-1.0/DocumentSeeker-1.0.exe)
* the [portable version](https://github.com/gcolin/DocumentSeeker/releases/download/docseeker-1.0/docseeker-1.0-Windows.zip)

All is included nothing to install.

### Linux

It works on Linux but you need to install things. Here the [binary](https://github.com/gcolin/DocumentSeeker/releases/download/docseeker-1.0/docseeker-1.0-Linux.zip).

Here the commands for Ubuntu,

    sudo apt install openjdk-11-jre tesseract-ocr tesseract-ocr-fra
    
If you use a Linux with KDE desktop such as Kubuntu, you can also install

    sudo apt install libgnome2-0 gvfs


## Supported languages

* English
* French
* Spanish

## How to build it in Windows

First you need some app installed:
* Java 11 jdk or Java 8 jdk
* Maven
* Ant
* Visual Studio C++ Community 2019
* Inno Setup

First build the executable with Visual Studio in *startexe/ConsoleApplication1.sln*.
Then check/update the variable *innocompiler* in the ant file.

In the directory of the readme, run the ant file.

    ant

You may have issues:
* maven or ant are not in the PATH
* JAVA_HOME is not set

It produces *target/docseeker.zip* and *target/DocumentSeeker.exe*

## How to build it in Linux (Ubuntu)

First you need some app installed:
* Java 11 jdk (openjdk-11-jdk) or Java 8 jdk
* Maven
* Ant

In the directory of the readme, run the ant file.

    ant

It produces *target/docseeker.zip*
