# 6502a-compiler

This repository contains code I worked on during my design of compilers course at Marist College. This was a semester long project where we designed a compiler to convert a grammar with similar syntax to C, into a instruction set based on opcodes for a 6502 microprocessor. 

More details about the grammar or machine language instruction set can be found in their respective PDF's 

### Instructions
  * Either compile the source code manually using javac, or utilize the precompiled java files I have provided
  * Use redirects, to input a file of code into the driver file 

```
java driver < test1.txt
```
  * Operating systems capable of running the machine code which has been generated are available on my professor's website. http://labouseur.com/courses/compilers/
    * A list of OS's provided by other students is available at the bottom of the project's and labs section. 

### JDK Installation
##### On Windows
https://docs.oracle.com/javase/8/docs/technotes/guides/install/windows_jdk_install.html#CHDEBCCJ

##### On Linux

Debian, Ubuntu, etc.

On the command line, type:

    $ sudo apt-get install openjdk-8-jdk

Fedora, Oracle Linux, Red Hat Enterprise Linux, etc.

On the command line, type:

    $ su -c "yum install java-1.8.0-openjdk-devel"

Arch, etc.

    $ sudo pacman -S jdk-openjdk 
