set Xmx=10000M
jre8\bin\java -Xmx%Xmx% -jar -Dspring.config.location=classpath:/application.properties,application.properties propro.jar
pause