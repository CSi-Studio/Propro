# Dependencies
## MongoDB 4.4.2
MongoDB is free for use. The officer Website is https://www.mongodb.com. User can download it from the website.<br/>
For Windows OS: https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-4.4.2-signed.msi <br/>
For Mac OS: https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-4.4.2.tgz <br/>
For Ubuntu 20.04 : https://repo.mongodb.org/apt/ubuntu/dists/focal/mongodb-org/4.4/multiverse/binary-amd64/mongodb-org-server_4.4.2_amd64.deb <br/>
More OS? Visit https://www.mongodb.com/try/download/community <br/>

## Java Runtime Environment 1.8
https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html <br/>
Download the proper version match for your Operation System <br/>

# Config
## Config for MongoDB Data Directory
Make sure which disk you would set as a database disk. 1TB is better. In the mongodb disk. Create an empty directory called "data", and in the "data" directory, create a directory called "db"

## Config for Java Runtime Environment
After downloading the JDK1.8, unzip the jdk package to a directory. Better in an SSD disk.

## Config for propro.jar
jre8\bin\java -Xmx10000M -jar -Dspring.config.location=classpath:/application.properties,customer.properties propro.jar <br/>
The customer.properties file should in the same directory as propro.jar

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


# Java VM Params. 
 - [Optional]   -Xmx10000M

