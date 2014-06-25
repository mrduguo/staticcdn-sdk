
### init project

#### get latest distribution link from
http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.staticcdn.sdk%22%20AND%20a%3A%22staticcdn-sdk-standalone-optimizer%22

```sh

curl -O http://search.maven.org/remotecontent?filepath=io/staticcdn/sdk/staticcdn-sdk-standalone-optimizer/0.1.5/staticcdn-sdk-standalone-optimizer-0.1.5.zip
unzip staticcdn-sdk-standalone-optimizer-0.1.5.zip 
cd staticcdn-sdk-standalone-optimizer-0.1.5 
git init
echo 'build
.*
*.iml
tmp*' > .gitignore
git add .
git config user.name 'Guo Du'
git config user.email gdu@staticcdn.io
git commit -m 'init repo with original optimizer'
git log --pretty=format:"%h - %an, %ar : %s"

```




### publish files to ftp server

Append following code to config.gradle
```groovy

repositories {
    mavenCentral()
}
configurations {
    ftpAntTask
}
dependencies {
    ftpAntTask("org.apache.ant:ant-commons-net:1.9.3") {
        module("commons-net:commons-net:1.4.1") {
            dependencies "oro:oro:2.0.8:jar"
        }
    }
}
task uploadSiteToServer() {
    doLast {
        def serverPassword = System.console().readPassword("\nPlease enter ftp server password: ").toString()
        ant {
            taskdef(name: 'ftp',
                    classname: 'org.apache.tools.ant.taskdefs.optional.net.FTP',
                    classpath: configurations.ftpAntTask.asPath)
            ftp(server: "your-ftp-server-host.com", port: 21, remotedir: '/your/www/root', userid: "your-user-id", password: serverPassword) {
                fileset(dir: "build/optimized-wwwroot/")
            }
        }
    }
    outputs.upToDateWhen { false }
}

```

Then you can publish site to ftp server with command  
```sh

./optimizer clean build uploadSiteToServer

```




