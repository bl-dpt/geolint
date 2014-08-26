# GeoLint

Validate Ordnance Survey GB & NI (OS/OSGB/OSNI) GML and NTF geospatial data files

### What does GeoLint do?

This tool...

### What are the benefits for the end user?

A list of benefits GeoLint brings to the end user:

* GML data files can be validated against Ordnance Survey Schema (http://www.ordnancesurvey.co.uk/xml/schema/)
* NTF data files are opened and parsed to ensure that they can be read successfully, using the GDAL/OGR library

### Who is the intended audience?

GeoLint is for:

* Those who wish to validate NTF/GML files, and checksum others
 
## Features and roadmap

### Version 0.0.1

* Validate 

## How to install and use

### Requirements

To install you need:

* Xerces Java 2.11 XML Schema 1.1 beta
* GDAL/OGR library with JNI jar. Target architecture for GDAL/OGR  must match your JVM (i.e. Windows/Linux & 32/64-bit)
* OS schema from here: http://www.ordnancesurvey.co.uk/xml/schema/ (tested with v7 & v8)(extract zip into "src/main/resources/schema")

You will need to build your own GDAL/OGR libraries. Installation instructions for these files are contained within the pom.xml file.

Read the instructions contained within the directories under ./src/main/resources/ for information on what to place in there.

### Use

To use the tool, you can try the following command:
```bash
$ java -jar binary.jar --help
Synopsys: java -jar binary.jar ...
...
```

## More information

### Licence

GeoLint is released under [Apache version 2.0 license](LICENSE.txt).

It includes a file from GDAL with the following license:

```/******************************************************************************
 * $Id: ogrinfo.java 23704 2012-01-06 10:27:08Z rouault $
 *
 * Name:     ogrinfo.java
 * Project:  GDAL SWIG Interface
 * Purpose:  Java port of ogrinfo application, simple client for viewing OGR driver data.
 * Author:   Even Rouault, <even dot rouault at mines dash paris dot org>
 *
 * Port from ogrinfo.cpp by Frank Warmerdam
 *
 ******************************************************************************
 * Copyright (c) 2009, Even Rouault
 * Copyright (c) 1999, Frank Warmerdam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ****************************************************************************/
 ```

### Acknowledgements

Part of this work was supported by the European Union in the 7th Framework Program, IST, through the SCAPE project, Contract 270137.

## Develop

Patches welcome

### Requirements

To build you require:

* Git client
* Apache Maven
* Java Developers Kit (e.g. OpenJDK 6)

Additionally, to build the GDAL/OGR dependency you need:

* Microsoft Visual C/C++
* SWIG (http://www.swig.org/)
* Apache Ant

Build instructions are here: http://trac.osgeo.org/gdal/wiki/GdalOgrInJavaBuildInstructions

Some additional tips for building GDAL/OGR are:

 * Ensure no spaces or trailing slashes in environment variables you set
 * Ensure no spaces are contained in in JAVA_HOME 
 * ANT_HOME is not the bin/ directory, use the parent of the bin/ directory
 * "Program Files\Visual Studio\VC\bin\vsvars32.bat" sets up MSVC for 32-bit builds on Windows - ensure architecture matches the JVM!
 * "Program Files\Visual Studio\VC\vcvarsall.bat amd64" sets up MSVC for amd64 builds on Windows - ensure architecture matches the JVM!

### Build

To compile go to the sources folder and execute the command:

```bash
$ mvn clean install
```

After successful compile the binary will be available at `target/binary.jar`.

### Deploy

To deploy do ...

### Contribute

1. [Fork the GitHub project](https://help.github.com/articles/fork-a-repo)
2. Change the code and push into the forked project
3. [Submit a pull request](https://help.github.com/articles/using-pull-requests)

To increase the changes of you code being accepted and merged into the official source here's a checklist of things to go over before submitting a contribution. For example:

* Has unit tests (that covers at least 80% of the code)
* Has documentation (at least 80% of public API)
* Agrees to contributor license agreement, certifying that any contributed code is original work and that the copyright is turned over to the project
