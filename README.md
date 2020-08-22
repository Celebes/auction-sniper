# auction-sniper
Example app from book "Growing Object-Oriented Software, Guided by Tests".

Openfire server is dockerized with openfire.xml, that contains <autosetup> that will setup admin and users accounts, in a custom image created by me, hosted here: https://hub.docker.com/repository/docker/celebez/openfire).

Manual installation of XMPP server and going through setup wizard after that is not necessary. Thanks to that e2e tests can be run anywhere, the only prerequisite is having Docker installed on your machine.

Just run **mvn clean install -U**, the app will be built and the tests will run, the docker container with XMPP server will start automatically thanks to **Docker Test Containers**.