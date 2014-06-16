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
    <version>0.1.4</version>
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
        classpath 'io.staticcdn.sdk:staticcdn-sdk-gradle-plugin:0.1.4'
    }
}
apply plugin: 'groovy'
apply plugin: 'staticcdn'
staticcdn {
    apiKey='***'
    apiSecret='***'
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
mvn release:clean && mvn release:prepare && mvn release:perform

```

SOSSR
https://oss.sonatype.org/content/groups/public
https://oss.sonatype.org/content/groups/staging
