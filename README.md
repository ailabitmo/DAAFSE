**DAAFSE**: smart metering platform
===

Deployment with [Docker](http://docker.com/)
---

We use Docker to deploy the platform and publish pre-build Docker images. To deploy the latest stable version, run the following commands:

```
docker pull kolchinmax/rabbitmq
docker pull kolchinmax/fuseki
docker pull kolchinmax/stream-publisher
docker pull kolchinmax/alert-service
docker run -i -t -p 5672:5672 -p 15672:15672 -p 15674:15674 kolchinmax/rabbitmq
docker run -i -t -p 3030:3030 --name fuseki kolchinmax/fuseki
docker run -t -t --link fuseki:fuseki kolchinmax/stream-publisher
docker run -i -t -p 8080:8080 kolchinmax/alert-service
```

Contacts
---
Maxim Kolchin (kolchinmax@gmail.com)
