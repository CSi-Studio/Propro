# propro-server
# Java VM Params. 
 - [Optional]   -Xmx10000M 
 - [Required]   -Ddbpath=localhost:27017
 - [Required]   -Dadmin_username=Admin
 - [Required]   -Dadmin_password=propro
 - [Required]   -Dmultiple=1
 - [Required]   -Drepository=\\172.16.55.75\ProproNas\data\
 - [Required]   -Ddingtalk_robot=https://oapi.dingtalk.com/robot/send?access_token=f2fb029431f174e678106b30c2db5fb0e40e921999386a61031bf864f18beb77


# How to start ProPro
``java -jar propro.jar --spring.config.location=./application.properties``
