Bridge Node Anchoring Service
======================

### License

Hdac private platform is licensed under the [MIT License](http://opensource.org/licenses/MIT).

Copyright (c) 2018-2019 Hdac Technology AG


### Execution environment 

About source code
>- JavaSE 1.8 optimization
>- Using Eclipse Oxygen.1a Release (4.7.1a)
>- Use HdacJavaLib.jar,
>- This jar file must be included in the project.

About docker image
>- Centos
>- Crontab


### Related docker image 

You can download docker image in [Bridgenode docker hub](https://hub.docker.com/r/hdac/bridgenode).
>1. anchoring_v09 (docker-compose: [anchoring-docker-compose](docker/docker-compose/anchoring))
>2. nfsserver_v09 (docker-compose: [nfsserver-docker-compose](docker/docker-compose/nfsserver))


### How to build source code

>1. Download the source code and add the project through Eclipse.
>2. File > Import > Maven | Existing Maven Projects
>3. Choose the folder where the source is located, check pom.xml, and complete the import.
>4. After Project> Clean, Run Build Project.


### How to update docker image

Anchoring service module is a JAVA application operated periodically through CRONTAB.  
CRONTAB operate main.java(docker/main/Main.java) including this source code as jar package.

>1. Do maven build of project, except directory docker
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










