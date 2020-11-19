@echo off
set dbpath=D:\data\db
echo Database Path:%dbpath%
if not exist %dbpath% (
    md %dbpath%
)
mongo-windows-4.4.1\bin\mongod --dbpath=%dbpath%

