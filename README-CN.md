# 下载
您需要使用AirdPro客户端将供应商文件传输为Aird格式。<br/>  
您可以从以下的FTP服务器下载所有依赖项: <br/>
    `server url: ftp://47.254.93.217/ProPro` <br/>
    `username: ftp` <br/>
    `password: 123456` <br/>
如果您的操作系统是Windows 7 x64或更高版本，请下载/Windows\u 1.0.0包中的所有文件,并且所有依赖项都在该包下,你不需要额外去下载JRE8和mongodb4.4.X

如果你的操作系统是Linux或Mac。首先需要下载目标javasdk和MongoDB。
我们在ftp://47.254.93.217/Java上下载一些常见的JDK8（我们更希望您从官方网站下载JDK8和MongoDB）
然后从ftp://47.254.93.217/ProPro/下载propro.jar文件以及包含客户的配置目录

# Windows下安装ProPro
## 第1步 确认磁盘位置
在启动propro之前，需要确认目录位置
1. 数据库存储位置
2. aird文件仓库位置  
数据库位置是针对mongodb的。你最好分配256GB或更多的SSD磁盘空间给mongodb。在磁盘上创建一个目录(例如“/dbpath”)<br/>  
aird文件位置用于质谱数据文件，这显然是一个很大的空间需求。您最好为其分配5TB或更多的磁盘空间(如“/data”) <br/>
数据库和aird存储库的建议空间比率为1:10 <br/>

## 第2步 确认所需的程序包
对于Windows用户，请确保将以下文件放在同一目录下
1. Jre8  
2. mongo-windows-4.4.1 
3. config
4. propro.jar
5. start-mongo.bat
6. start-propro.bat <br/>

## 第3步 确认配置项属性
在配置目录下打开用户属性.您将看到以下属性：
1. multiple
2. spring.data.mongodb.url
3. repository <br/>
如果计算机的CPU有N个核心，则将multiple设为N/2 <br/>
如果计算机的内存有M千兆字节的内存，可以自由使用，设置multiple= M/10 <br/>
当N/2不等于M/10的时候,选择其中小的那个,以防另外一个达到性能瓶颈 <br/>

如果您没有阅读任何关于mongodb的文档.那么请不要更改spring.data.mongodb.url属性,保持其默认值 <br/>
存储库是指步骤1中提到的aird文件位置，用该位置填充属性。

## 第4步确认MongoDB属性（仅首次启动，可选步骤） 
第一次启动mongo服务.你应该编辑start-mongo.bat文件 <br/>
打开文件，可以看到如下命令行：

    @echo off
    set dbpath=D:\data\db
    echo Database Path:%dbpath%
    if not exist %dbpath% (
        md %dbpath%
    )
    mongo-windows-4.4.1\bin\mongod --dbpath=%dbpath%

在第2行中，按照步骤1所述更改mongodb的路径。 <br/>
在最后一行中，确保mongod的路径是正确的，如步骤2所述 <br/>
如果忽略整个步骤4。数据库目录将在D:\data\db中创建,
您应该确保mongodb目录的路径是正确的，否则mongo服务器将无法启动。 <br/>

## 第5步 启动Mongo服务器
首先双击start-mongo.bat来启动mongo服务器.当您在控制台中看到日志“Waiting for connections”时,这意味着您mongo服务器启动成功。

## 第6步 启动ProPro服务器
最后双击start-propro.bat 来启动propro服务器(如果windows defender请求防火墙权限，请允许该命令) <br/>
当您看到日志“在XXX秒内启动ProproApplication”时，表示您成功启动了propro服务器。 <br/>
现在打开您的浏览器（Firefox、Edge或Chrome），使用http://localhost。

事实上，如果你下载了Windows1.0.0的整个软件包，不要对属性做任何更改。只需按步骤5和步骤6启动mongo服务器和propro

# Linux下安装ProPro
如果您阅读了本章，然后我们假设您对Linux操作有基本的了解。
## 第1步 确认磁盘位置
与Windows章节1相同

## 第2步 安装Java 8
确保“java-version”正常

## 第3步 安装Mongodb 4.4.X
这对你来说可能是一项艰苦的工作.请在mongodb官方站点阅读安装文档.

## 第4步 确认属性
与Windows第3章相同

## 步骤5 运行Propro.jar文件
    nohup java -jar -Dspring.config.location=classpath://application.properties,config/customer.properties propro.jar > log.file 2>&1 &
如果你有一个target jre(例如：“jdk8/jre/bin/java”),您可以将“java”替换为“jdk8/jre/bin/java” <br/>
您也可以创建一个.sh命令文件来存储这个启动命令。

# 依赖关系
## MongoDB 4.4.X
MongoDB是免费使用的。官方网站是https://www.mongodb.com。用户可以从网站上下载  <br/>  
For Windows OS: https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-4.4.2-signed.msi <br/>  
For Mac OS: https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-4.4.2.tgz <br/>
For Ubuntu 20.04 : https://repo.mongodb.org/apt/ubuntu/dists/focal/mongodb-org/4.4/multiverse/binary-amd64/mongodb-org-server_4.4.2_amd64.deb <br/>
More OS? Visit: https://www.mongodb.com/try/download/community <br/>

## Java Runtime Environment 1.8

    https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html
下载与您的操作系统匹配的正确版本

# Config
## Config for customer.properties
### multiple  
例如: 

    multiple = 1
用于分析的处理线程数。它取决于CPU的核数和服务器的内存。一般来说，2个具有10GB-15GB内存的核可以支持1个处理线程。

### spring.data.mongodb.uri
例如: 

    spring.data.mongodb.uri=mongodb://localhost:27017/propro <br/>  
如果使用ProPro包中的默认mongodb，则为mongodb连接url。不要更改此属性
如果你的mongodb部署在另一台服务器上。用目标服务器的IP地址替换“localhost”
确保目标服务器在同一个局域网中，并且该计算机服务器能够成功访问目标服务器的mongodb

### repository  存储库
例如:

    repository=E:\\
这是一个必选项,Aird文件仓库的存储位置

## Config for Java VM Params. 
 - [Optional]   -Xmx10000M      分配10GB内存给到ProPro

