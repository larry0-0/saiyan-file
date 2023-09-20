#!/bin/bash
appname=$1
port=$2
workspace=$(cd $(dirname $0);pwd)
startup_log=$workspace/startup.log
check_url=127.0.0.1:$port/checkPreload
success_kw=success

checkHealth(){
	result=$(curl -s ${check_url} | grep $success_kw)
	if [ "$result" != "" ]; then
		return "1"
	else
		return "0"
	fi
}


mkdir -p $workspace || exit 1
mkdir -p logs
mkdir -p backup
if [ -e $workspace/$appname-*.jar ]; then
  mv -f $appname.jar backup/$appname.$(date +%Y%m%d%H%M%S).jar
  mv -f $appname-*.jar $appname.jar
fi

process_id=$(ps aux | grep -v grep | grep $appname.jar | awk '{print $2}')
if [ "$process_id" != "" ]; then
    echo "Java进程"$appname"正在运行，将被杀死。"
    kill -9 $process_id
else
    echo "Java进程"$appname"未找到，无需杀死。"
fi

echo ">>>>>>>>>>> 服务"$appname" 端口"$port" 启动开始"
JAVA_OPTS="$JAVA_OPTS \
-Xms512m -Xmx1024m -Xmn256m \
-Xloggc:logs/gc.log \
-XX:+UseCompressedOops \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseG1GC \
-XX:+UseCGroupMemoryLimitForHeap"

echo "nohup java "$JAVA_OPTS" -Dserver.port="$port" -classpath "$appname".jar org.springframework.boot.loader.JarLauncher"

nohup java $JAVA_OPTS -Dserver.port=$port -Duser.timezone=Asia/Shanghai -classpath $appname.jar org.springframework.boot.loader.JarLauncher > $startup_log 2>&1 &

echo [INFO] ">>>>>>>>>>> 日志所在位置"$startup_log" 开始输出日志<<<<<<<<<<"

# 判断是否启动成功
duration=0
timeout=600
while [ $duration -lt $timeout ]
do
    sleep 1
    checkHealth
    if [ $? -eq "1" ]; then
	    echo ">>>>>>>>>> 服务启动成功 耗时"$duration"秒 <<<<<<<<<<<"
	    break
    fi
    duration=$((duration + 1))
done

if [[ $duraton -gt $timeout ]]; then
	echo [ERROR] ">>>>>>>>>> 部署等待时间超时 耗时"$duration"秒,退出部署 <<<<<<<<<<<"
  pid=$(ps aux | grep -v grep | grep $appname | awk '{print $2}')
  kill -9 $pid
  exit 1
fi