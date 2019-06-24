Bridge Node Anchoring Service
======================

### License

Hdac private platform is licensed under the [MIT License](http://opensource.org/licenses/MIT).

Copyright (c) 2018-2019 Hdac Technology AG


### Execution environment 

About source code
>- Java maven application

About docker image
>- Centos
>- Crontab


### Related docker image 

You can download docker image in [Bridgenode docker hub](https://hub.docker.com/r/hdac/bridgenode).
>1. anchoring_v09
>2. nfsserver_v09


### How to update docker image

Anchoring service module is a JAVA application operated periodically through CRONTAB.  
CRONTAB operate main.java including this source code as jar package.

>1. Do maven build of project, except directory sh
>2. We need jar package file on target directory 
>3. All library files are managed by nfsserver, copy the jar file to the nfsserver docker container.   
>- $ docker cp Anchoring-0.0.1.jar nfsserver:/opt/shareUtil/lib (After running nfsserver container)


### How to operate service

>1. Operate nfsserver docker container
>2. Operate anchoring docker container
>3. Checking share library inside anchoring container  
>- $ docker exec -it anchoring bash  
>- $ cd anchor/shareLib  
>4. configure database config file and CRONTAB shedule (anchor/config, crontab -e)
>5. After configuration, start crontab serivce  
>- $ systemctl start crond  










