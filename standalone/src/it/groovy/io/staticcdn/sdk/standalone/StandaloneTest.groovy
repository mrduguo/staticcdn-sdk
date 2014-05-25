import org.apache.commons.io.FileUtils

if(project.version.indexOf('SNAPSHOT')<0){
    println('skip test during release')
    return
}
def outputFolder=new File(project.basedir,"target")
def zipFile=new File(project.basedir,"target/$project.artifactId-${project.version}.zip")
exec("unzip $zipFile.absolutePath",outputFolder)

new File(outputFolder,"$project.artifactId-$project.version/config.gradle").delete()
FileUtils.copyFile(new File(project.basedir,"target/test-classes/config.gradle"),new File(outputFolder,"$project.artifactId-$project.version/config.gradle"))

def optimizerCommand=new File(outputFolder,"$project.artifactId-$project.version/optimizer").absolutePath
exec(optimizerCommand,outputFolder)
def optimisedFile = new File(project.basedir, "target/test-classes/wwwroot/index.html")
assertFileOptimised(optimisedFile)
optimisedFile.write('foo')
exec(optimizerCommand,outputFolder)
assertFileOptimised(optimisedFile)


def assertFileOptimised(File expectedOptimisedFile){
    if(!expectedOptimisedFile.text.equals('<html> <head> <title>test</title> </head> <body> test </body> </html>')){
        throw new RuntimeException("unexpected optimised content $expectedOptimisedFile.text")
    }
}


def exec(String cmd,File dir){
    println("${new Date()} executing $cmd")
    def sout = new StringBuffer()
    def serr = new StringBuffer()
    def process = cmd.execute((String[]) null, dir)
    process.consumeProcessOutput(sout, serr)
    process.waitFor()
    if(sout.length()>0){
        println(sout.toString())
    }
    if(serr.length()>0){
        println(serr.toString())
    }
    if(process.exitValue()!=0){
        throw new RuntimeException("failed to run $cmd with exit status ${process.exitValue()}")
    }
}