Static CDN SDK
==============



Documentation
------------------
* [Optimizer configuration](https://github.com/mrduguo/staticcdn-sdk/blob/master/docs/Optimizer.md)
* [Standalone optimizer](https://github.com/mrduguo/staticcdn-sdk/blob/master/docs/StandaloneOptimizer.md)
* Sample projects
  * [html with standalone optimizer](https://github.com/mrduguo/staticcdn-sdk/tree/master/samples/html-standalone) 
  * [jsp with maven build system](https://github.com/mrduguo/staticcdn-sdk/tree/master/samples/jsp-maven) 


Maven Sample
------------------

```xml
<plugin>
    <groupId>io.staticcdn.sdk</groupId>
    <artifactId>staticcdn-sdk-maven-plugin</artifactId>
    <version>0.1.13</version>
    <executions>
        <execution>
            <goals>
                <goal>optimize</goal>
            </goals>
            <configuration>
                <apiKey>***</apiKey>
                <apiSecret>***</apiSecret>
                <inputWwwRoots>
                    <inputWwwRoot>${project.basedir}/target/generated-resource</inputWwwRoot>
                    <inputWwwRoot>${project.basedir}</inputWwwRoot>
                </inputWwwRoots>
                <outputWwwRoot>${project.basedir}/target/classes</outputWwwRoot>
                <inputFilePathPatterns>
                    <inputFilePathPattern>/static/include/css/.*inc</inputFilePathPattern>
                    <inputFilePathPattern>/static/include/js/.*inc</inputFilePathPattern>
                </inputFilePathPatterns>
            </configuration>
        </execution>
    </executions>
</plugin>
```


Gradle Sample
------------------

```Groovy
buildscript {
    dependencies {
        classpath 'io.staticcdn.sdk:staticcdn-sdk-gradle-plugin:0.1.13'
    }
}
apply plugin: 'groovy'
apply plugin: 'staticcdn'
staticcdn {
    apiKey='***'
    apiSecret='***'
    inputWwwRoots=['wwwroot/']
    outputWwwRoot='build/optimized-wwwroot'
    inputFilePathPatterns=[
            '^.*html$',
    ]
    optimizerOptions=[
            autoEmbedCss:true,
            autoDataUrlMaxFileSize:1000
    ]
}
```

SDK Build Commands
------------------

```sh

(cd /opt/staticcdn/src/staticcdn-sdk && mvn clean install)

(cd /opt/staticcdn/src/staticcdn-sdk && mvn release:clean && mvn release:prepare && mvn release:perform)

```
