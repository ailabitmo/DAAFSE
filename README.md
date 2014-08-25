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
docker run -d -p 5672:5672 -p 15672:15672 -p 15674:15674 kolchinmax/rabbitmq
docker run -d -p 3030:3030 kolchinmax/fuseki
docker run -d kolchinmax/stream-publisher
docker run -d -p 80:8080 kolchinmax/alert-service
```

Contacts
---
Maxim Kolchin (kolchinmax@gmail.com)
