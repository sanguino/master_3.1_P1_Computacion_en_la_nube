# master_3.1_P1_Computacion_en_la_nube

## Creación de una AMI con la aplicación

Partimos de una instancia de ec2, conectamos por ssh

Instalamos java como en la parte básica:

```bash
$ sudo apt update

$ sudo apt install openjdk-11-jdk
```
Una vez tenemos java instalado creamos un sh que usaremos como script para configurar un servicio systemd:

```#/usr/local/bin/P1_Events_Api.sh```
```sh
#!/bin/sh

SERVICE_NAME=P1_Events_Api
PATH_TO_JAR=/home/ubuntu/p1/app.jar
PID_PATH_NAME=/tmp/P1_Events_Api-pid

startJava() {
  echo "Starting $SERVICE_NAME ..."
  if [ ! -f $PID_PATH_NAME ]; then
    RDS_ENDPOINT="rdsp1.chdypbbyhsde.us-east-1.rds.amazonaws.com" \
    RDS_DATABASE="rdsp1" \
    RDS_USER="rdsp1" \
    RDS_PASS="r2qoxFQR689CA7" \
    BUCKET_NAME="sanguino.cloudapps.urjc" \
    REGION="us-east-1" \
    nohup java -jar $PATH_TO_JAR --spring.profiles.active="production" /tmp 2>> /dev/null >> /dev/null &
    echo $! > $PID_PATH_NAME
    echo "$SERVICE_NAME started ..."
  else
    echo "$SERVICE_NAME is already running ..."
  fi
}

stopJava() {
  if [ -f $PID_PATH_NAME ]; then
   PID=$(cat $PID_PATH_NAME)
   echo "$SERVICE_NAME stoping ..."
   kill $PID
   echo "$SERVICE_NAME stopped ..."
   rm $PID_PATH_NAME
  else
   echo "$SERVICE_NAME is not running ..."
  fi
}

case $1 in
start)
  startJava
  ;;
stop)
  stopJava
  ;;
restart)
  stopJava
  startJava
  ;;
esac
```

Le damos permisos de ejecución
```bash
$ sudo chmod +x /usr/local/bin/P1_Events_Api.sh
```

Creamos el fichero del servicio:

```#/etc/systemd/system/P1_Events_Api.service```
```sh
[Unit]
 Description = Java Service
 After network.target = P1_Events_Api.service
[Service]
 Type = forking
 Restart=always
 RestartSec=1
 SuccessExitStatus=143 
 ExecStart = /usr/local/bin/P1_Events_Api.sh start
 ExecStop = /usr/local/bin/P1_Events_Api.sh stop
 ExecReload = /usr/local/bin/P1_Events_Api.sh reload
[Install]
 WantedBy=multi-user.target
```


Y habilitamos el servicio para que se arranque al inicial el sistema

```bash
$ sudo systemctl daemon-reload
$ sudo systemctl enable P1_Events_Api
```

Despues vamos a la consola, pulsamos boton derecho sobre la instancia, create image. Le damos nombre y descripción y ya tenemos la imagen lista.



