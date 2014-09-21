
About staticcdn-sdk-samples-jsp-maven
-------------------------------------
This sample project have samples for following technologies:
* maven
* jsp
* css
* lesscss
* javascript




### Run the web server for local development with original css and javascript


```sh

# cd staticcdn-sdk/samples/jsp-maven
mvn -DrunWebServerAtPort=8081 -DskipOptimize=true

```

Then you may access the server with original css and javascript for development at:
[http://localhost:8081/](http://localhost:8081/)

You may press `ctrl+c` to stop the web server.


###  Build the war file with optimized static resources for server

```sh

# cd staticcdn-sdk/samples/jsp-maven
mvn

```

Preview the page with optimized resources:
[http://www.statico.io/preview/610bfe5c95d5dcd47b211953ad14cec4.html](http://www.statico.io/preview/610bfe5c95d5dcd47b211953ad14cec4.html)
