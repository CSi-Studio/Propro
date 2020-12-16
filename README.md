# Introduction
ProPro is an open-source web-based platform for DIA targeted MS data analysis with high performance and rich visual interface.
You can visit 124.71.137.210 for try.(This is a website just for a try, please do not upload any files to the website).

# How to Install
## Download
You should use the AirdPro client to transfer the vendor files into Aird format.<br/>
You can download the all the dependencies from the FTP server: <br/>
    `server url: ftp://47.254.93.217/ProPro` <br/>
    `username: ftp` <br/>
    `password: 123456` <br/>
If your OS is Windows 7 x64 or higher, Download all the files in the /Windows_1.0.0 package, and all the dependencies are under the package,
and you don't need to download JRE8 and mongodb 4.4.X

If your OS is Linux or Mac. First you need to download the target Java SDK and MongoDB.
We download some common JDK8 in the ftp://47.254.93.217/Java (We prefer you to download the JDK8 and MongoDB from the official site)
Then download the propro.jar and the config directory which contains the customer.properties from the ftp://47.254.93.217/ProPro/

## Start Up ProPro under the Windows
### Step1. Confirm disk location
Before start up the propro, you need to confirm to directory location
1. database location
2. aird file location <br/>
the database location is for mongodb. You'd better allocate 256GB or more SSD disk space for that. Create a directory at the disk(such as "/dbpath") <br/>
the aird file location is for the MS data files, which is obviously a large space requirement. You'd better allocate 5TB or more disk space for that(such as "/data") <br/>
A suggested space rate for database and aird repository is 1:10 <br/>

### Step2. Confirm the required package
For Windows user, mark sure you put the following directory under a same directory
1. Jre8 directory
2. mongo-windows-4.4.1
3. config directory
4. propro.jar
5. start-mongo.bat
6. start-propro.bat <br/>

### Step3. Confirm the properties
open the customer.properties under the config directroy. You will see the following properties:
1. multiple
2. spring.data.mongodb.url
3. repository <br/>
if the CPU of the computer has N core,set the multiple= N/2 <br/>
if the Memory of the computer has M Gigabyte memory that can be used free, set the multiple = M/10
choose the small one for your computer.<br/>
if you have not read any document about mongodb. Don't change the second property.<br/>
the repository means the aird file location which is mentioned in the Step1, fill the property with the location.

### Step4. Confirm MongoDB Properties (Only for first time to start up,Optional Step)
For the first time starting up the mongo server.You should edit the start-mongo.bat file.<br/>
Open the file, you can see the command line like this:

    @echo off
    set dbpath=D:\data\db
    echo Database Path:%dbpath%
    if not exist %dbpath% (
        md %dbpath%
    )
    mongo-windows-4.4.1\bin\mongod --dbpath=%dbpath%

in line 2, change the path for your mongodb path as mentioned in Step1.(here is D:\data\db) <br/>
in the last line, make sure the path of mongod is correct as mentioned in Step2 <br/>
if the whole step4 is ignored. The database directory will be created in the D:\data\db,
and you should make sure the mongodb directory's path is correct or the mongo server would start up failed.

### Step5: Start Mongo Server
Double-click the start-mongo.bat first to start up the mongo server. When you see the log "Waiting for connections" in the console,that means you 
start up the mongo server successful. <br/>

### Step6: Start ProPro Server
at last double-click the start-propro.bat to start up the propro server. (If the windows defender ask for firewall permission,please allow the command)
 When you see the log "Started ProproApplication in XXX seconds", that mean you start up the propro server successful.
 Now open http://localhost on your browser(Firefox, Edge or Chrome).

In fact, if you download the whole package of Windows1.0.0, don't do any change for the properties. Just start up the mongo server and the propro by step5 and step6

## Start Up ProPro under the Linux
If you read this chapter, then we assume you have a basic understanding of Linux operations.
### Step1. Confirm disk location
It is the same as in Windows Chapter

### Step2. Install Java 8
make sure "java -version" is normal

### Step3. Install Mongodb 4.4.X
this may be a hard work for you. Please read the install document at the mongodb offical site.

### Step4. Confirm the properties
Same as Windows Chapters3

### Step5. Run Propro.jar
    nohup java -jar -Dspring.config.location=classpath://application.properties,config/customer.properties propro.jar > log.file 2>&1 &
if you have a target jre(for example:"jdk8/jre/bin/java"),you can replace the "java" with "jdk8/jre/bin/java" <br/>
you can also create a .sh command file to store this start up command.

## Dependencies
### MongoDB 4.4.X
MongoDB is free for use. The officer Website is https://www.mongodb.com. User can download it from the website.<br/>
For Windows OS: https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-4.4.2-signed.msi <br/>
For Mac OS: https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-4.4.2.tgz <br/>
For Ubuntu 20.04 : https://repo.mongodb.org/apt/ubuntu/dists/focal/mongodb-org/4.4/multiverse/binary-amd64/mongodb-org-server_4.4.2_amd64.deb <br/>
More OS? Visit https://www.mongodb.com/try/download/community <br/>

### Java Runtime Environment 1.8
https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html <br/>
Download the proper version match for your Operation System <br/>

# Config
## Config for customer.properties
### multiple  
sample: multiple = 1 <br/>
the number of proccessing threads for analysis.It depends on the number of cores of the CPU and the memory of the server. As a general rule, 2 cores with 10GB-15GB memory can support 1 processing thread.

### spring.data.mongodb.uri
sample: spring.data.mongodb.uri=mongodb://localhost:27017/propro <br/>
the connect url for mongodb, if using the default mongodb in the ProPro package. Do not change this property. </br>
If your mongodb is deploy on the other server. replace the "localhost" with the target server's IP address. </br>
Make sure that the target server is in the same local area network and this computer server can access the target server's mongodb successful

### repository
sample: repository=E:\\ <br/>
Required! The aird file location.

## Config for Java VM Params. 
 - [Optional]   -Xmx10000M

# How to use
## 1. Data Preparing
## 1.1 Create Repository Folders
In your installation step "Confirm disk location", you would set a directory as the Aird file repository. In the repository,
You need to create the following subfolders under this repository folder.
    
    1. /Library/Irt
    2. /Library/Standard
    3. /ProjectName1
    4. /ProjectName2
        ...
    N. /ProjectNameX
The "/Library/Irt" folder is used for storing the irt library files. The "/Library/Stardand" folder is used for storing the assay library files.
You should put all the ".aird" and ".json" format files into it's related project folder. </br>

## 1.2 Setup Library File and Project File
Put your irt library file into the /Library/Irt directory
Put your standard assay library file into the /Library/Standard directory
Create a project folder with a project name
Put all the aird files into the project folder

## 2. Model Introduction
### 2.1 Library Model
#### 2.1.1 Library Initialization
You can upload your library file with "Plus" button or Scan all the library files in the repository with "Scan" button
When scanning or uploading, you can check the progress in the Task model for log information.

#### 2.1.2 Format Supporting

    .tsv
    .csv
    .traML

#### 2.1.3 Decoy Generator
Before or after your uploading, you can select a decoy generator for decoy generation. We provide two method, the default generator method is just a shuffle method.
Another method called Nick method is a less repetitive pseudo-peptide generation algorithm.

#### 2.1.4 Visualization
You can see all the information of the library and all the fragments group by peptide with a table in the browser.

## 2.2 Project Model
### 2.2.1 Create Project
After you create a project directory under the repository. You can click "Plus" button on the Homepage-Project block. Here you need to create a project with the same name as project folder name.

### 2.2.2 Init Project
Before you click "Scan" button in the Project List page, you need to make sure that all the aird files have already put into the project folder. Then the system will scan all the aird files and parsing them into the mongo database.
It will cost you about 1-5 minutes. You can check the progress in the Task Model.

### 2.2.3 Project List
In the project list page. You can check for every experiment by clicking the Project Name Link to jump to the Experiment Model. Or you can operate under the project list for batch operation.
You can do two batch operations. 
1. Batch for the Irt step.
2. Batch for the whole workflow(Irt, Extract, Peak Picking, Peak Scoring, Semi-Supervise Learning with LDA, Report).
Analysis will cost a relatively long time(Generally no more than 15 minutes/experiment, depending on the count of fragments and the CPU of the computer). You can check the progress in the Task Model.

### 2.2.4 Report
You can export the analysis result of every experiment.

### 2.2.4 Analysis Result Visualization
After analysis. You can check all the analysis result of every peptide. ProPro also provide Irt Result for Quality Control.

## 2.3 Experiment Model
### 2.3.1 Experiment Detail and Analysis
Every experiment in the project can be analyzed independently. You can do it in the Experiment List page. User can also see the SWATH window, basic file information in the Experiment Detail page.
Experiment also support iRT analysis and the whole workflow analysis.

### 2.3.2 Reanalyze
User can reanalyze the experiment with different parameters. Every analysis result will be saved independently.

## 2.4 Task Model
The task model is a module for viewing high time-consuming asynchronous operations. Including parsing library file, parsing aird file, irt analysis, whole workflow analysis.

## 2.5 Tools
### 2.5.1 Peptide Clinic
This is an integrated tool for viewing Xic results and peak scoring results. Not only the XIC data that has been analyzed, the tool also supports real-time XIC analysis. It provides a variety of peptide fragmentation strategies, 
so that the researchers can have a targeted view of the identification results of a peptide. This tools is the core tool for experiment analysis result review.

### 2.5.2 Molecular Mass Calculator
This tool is used for compute m/z for any peptide sequence with specific cut parameters. 

### 2.5.3 Cross Overviews Comparison
This tool is used for comparison between multiple analysis results. It is used for peptides in experiments of the same project to see the correlation between different experiments.



    



   

