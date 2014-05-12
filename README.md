Static CDN SDK
==============



Documents
------------------
* [API](https://github.com/mrduguo/staticcdn-sdk/blob/master/docs/API.md)
* [Optimiser](https://github.com/mrduguo/staticcdn-sdk/blob/master/docs/Optimiser.md)


Maven Sample
------------------

```xml
<plugin>
    <groupId>io.staticcdn.sdk</groupId>
    <artifactId>staticcdn-sdk-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>optimise</goal>
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
        classpath 'io.staticcdn.sdk:staticcdn-sdk-gradle-plugin:0.0.20-SNAPSHOT'
    }
}
apply plugin: 'groovy'
apply plugin: 'staticcdn'
staticcdn {
    inputWwwRoots=['../']
    outputWwwRoot='build/wwwroot'
    inputFilePathPatterns=[
            '^\\/index.html$',
            '^((?!build).)*index.html$',
    ]
    optimiserOptions=new io.staticcdn.sdk.client.model.OptimiserOptions(
            autoEmbedCss:true,
            autoDataUrlMaxFileSize:1000
    )
}
```

SDK Build Commands
------------------

```sh

cd /opt/staticcdn/src/staticcdn-sdk
mvn clean install

cd /opt/staticcdn/src/staticcdn-sdk
mvn  --batch-mode release:prepare release:perform && mvn clean install

```
