#! /bin/sh
name="propro"
Xmx=100000M
Xms=20000M
pid=${name}".pid"
jarName=`ls | grep -e "^\${name}.*jar$" | sort -r | head -n 1`

#开始方法
start(){
	if [ -f "$pid" ]
	then
		echo "$jarName is running !"
		exit 0;
	else
		echo -n  "start ${jarName} ..."
		nohup java 	-Xmx${Xmx} -Xms${Xms} \
		    -Ddbpath=${dbpath} \
		    -Dadmin_username=${admin_username} \
		    -Dadmin_password=${admin_password} \
		    -Ddingtalk_robot=${dingtalk_robot} \
			-XX:+UseParNewGC \
			-XX:+UseConcMarkSweepGC \
			-XX:CMSFullGCsBeforeCompaction=3 \
			-XX:CMSInitiatingOccupancyFraction=60 -jar ${jarName} >/dev/null 2>&1 &   #注意：必须有&让其后台执行，否则没有pid生成
		[ $? -eq 0 ] && echo   "[ok]"
		echo $! > ${pid}   # 将jar包启动对应的pid写入文件中，为停止时提供pi
      fi
}

#停止方法
stop(){
	echo -n "stop $name ..."
 	if [ -f "$pid" ]
	then
		PID=$(cat ${pid})
	        kill -9 $PID
		[ $? -eq 0 ] && echo  "[ok]"
		rm -fr $pid
	else
		echo  "[ok]"
	fi
}

case "$1" in
start)
  start
  ;;
stop)
  stop
  ;;
restart)
  stop
  start
  ;;
*)
  printf 'Usage: server.sh { start|stop|restart}\n'
  exit 1
  ;;
esac