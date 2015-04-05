package io.staticcdn.sdk.client;

import io.staticcdn.sdk.client.model.OptimizeResponse;
import io.staticcdn.sdk.client.model.OptimizerOptions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class RefsDownloader {

    StaticCdnClient staticCdnClient;
    File outputWwwRoot;
    String filePath;
    OptimizerOptions optimizerOptions;
    OptimizeResponse optimizeResponse;
    String refsFileNameSuffix;

    public RefsDownloader(StaticCdnClient staticCdnClient, File outputWwwRoot, String filePath, OptimizerOptions optimizerOptions, OptimizeResponse optimizeResponse, String refsFileNameSuffix) {
        this.staticCdnClient = staticCdnClient;
        this.outputWwwRoot = outputWwwRoot;
        this.filePath = filePath;
        this.optimizerOptions = optimizerOptions;
        this.optimizeResponse = optimizeResponse;
        this.refsFileNameSuffix = refsFileNameSuffix;
    }


    public void execute() throws Exception {
        File outputFile = staticCdnClient.buildOutputFile(outputWwwRoot, filePath);

        writeRefIds(outputFile);

        if(needDownloadRefs()){
            File staticoCacheFolderFile=null;
            String staticoCacheFolder = System.getProperty("staticoCacheFolder");
            if(StringUtils.isEmpty(staticoCacheFolder)){
                staticoCacheFolderFile = new File(System.getProperty("user.home"), ".statico/cache");
            }else{
                staticoCacheFolderFile = new File(staticoCacheFolder);
            }

            String relativeOutputPath=optimizerOptions.getCdnBaseUrl();
            if(relativeOutputPath==null){
                relativeOutputPath="/";
            }else if(relativeOutputPath.indexOf("//")>=0){
                relativeOutputPath=relativeOutputPath.substring(relativeOutputPath.indexOf("/",relativeOutputPath.indexOf("//")+2));
            }
            File downloadTarget=new File(outputWwwRoot,relativeOutputPath);

            for(String fileName:optimizeResponse.getReferences().values()){
                downloadFileToCacheIfNotExist(staticoCacheFolderFile,fileName);
                copyFileFromCacheToTarget(staticoCacheFolderFile, fileName, downloadTarget);
            }
        }

        FileUtils.writeStringToFile(outputFile, optimizeResponse.getOptimized(), "UTF-8");
        System.out.println("optimized session " + optimizeResponse.getSignature() + " to " + outputFile.getCanonicalPath());
    }

    private void downloadFileToCacheIfNotExist(File staticoCacheFolderFile, String fileName) throws Exception{
        File targetFile=new File(staticoCacheFolderFile, fileName);
        if(!targetFile.exists()){
            File tmpFile=new File(targetFile.getAbsoluteFile()+".tmp."+System.currentTimeMillis());
            staticCdnClient.apiCallDownload(fileName, tmpFile);
            if(!targetFile.exists()) {
                tmpFile.renameTo(targetFile);
            }else{ // for parallel build race condition
                tmpFile.delete();
            }
        }
    }

    private void copyFileFromCacheToTarget(File staticoCacheFolderFile, String fileName, File downloadTarget) throws Exception{
        FileUtils.copyFile(
                new File(staticoCacheFolderFile,fileName),
                new File(downloadTarget,fileName)
        );
    }

    protected File staticoConfigBaseFolder(){
        String staticoConfigFolder = System.getProperty("staticoConfigFolder");
        if(StringUtils.isEmpty(staticoConfigFolder)){
            return new File(System.getProperty("user.home"), ".statico");
        }else{
            return new File(staticoConfigFolder);
        }

    }

    private void writeRefIds(File outputFile) throws IOException {
        if(!refsFileNameSuffix.equals("skip")){
            StringBuilder refText = new StringBuilder();
            refText.append("session=" + optimizeResponse.getSignature() + "\n");
            if (optimizeResponse.getReferences() != null) {
                for (String referenceKey : optimizeResponse.getReferences().keySet()) {
                    refText.append(referenceKey + "=" + optimizeResponse.getReferences().get(referenceKey) + "\n");
                }
            }
            File refOutputFile = new File(outputFile.getAbsolutePath() +refsFileNameSuffix);
            FileUtils.writeStringToFile(refOutputFile, refText.toString());
        }
    }

    private boolean needDownloadRefs() {
        return optimizerOptions!=null &&
                (       Boolean.TRUE.equals(optimizerOptions.getDownloadRefs()) ||
                        (optimizerOptions.getCdnBaseUrl()!=null && optimizerOptions.getCdnBaseUrl().indexOf("//")<0)
                );
    }

}
