package io.staticcdn.sdk.gradle;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class S3Task extends DefaultTask {

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String bucketName;
    private String bucketPath = "";
    private List<String> inputFolders;
    private List<String> inputFilePathPatterns;
    private String cacheControlHeader = "public, max-age=3600";
    private boolean allowPublicAccess = true;
    private Map<String, String> textFileMimeTypeMapping;

    public S3Task() {
        textFileMimeTypeMapping = new HashMap<String, String>();
        textFileMimeTypeMapping.put("htm", "text/html;charset=UTF-8");
        textFileMimeTypeMapping.put("html", "text/html;charset=UTF-8");
        textFileMimeTypeMapping.put("js", "application/javascript;charset=UTF-8");
        textFileMimeTypeMapping.put("css", "text/css;charset=UTF-8");
        textFileMimeTypeMapping.put("json", "application/json;charset=UTF-8");
    }

    @TaskAction
    public void uploadToS3() throws Exception {
        Map<String, File> filesToUpload = new LinkedHashMap<String, File>();
        for (String path : inputFolders) {
            File inputRootFolder;
            if(path.startsWith("/") || path.indexOf(":")>0){
                inputRootFolder = new File(path);
            }else{
                inputRootFolder = new File(getProject().getProjectDir(), path);
            }

            for (File foundFile : FileUtils.listFiles(inputRootFolder, null, true)) {
                String relativePath = foundFile.getAbsolutePath().substring(inputRootFolder.getAbsolutePath().length());
                relativePath = relativePath.replaceAll("\\\\", "/");
                for (String filePathPattern : inputFilePathPatterns) {
                    if (relativePath.matches(filePathPattern) && !filesToUpload.containsKey(relativePath)) {
                        filesToUpload.put(relativePath, foundFile);
                    }
                }
            }
        }
        getLogger().info("uploading " + filesToUpload.size() + " files to " + bucketName +"/"+ bucketPath + " ...");

        AmazonS3Client amazonS3Client;
        if (awsAccessKeyId != null) {
            amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey));
        } else {
            amazonS3Client = new AmazonS3Client();
        }

        for (Map.Entry<String, File> pair : filesToUpload.entrySet()) {
            String filePath = pair.getKey();
            filePath=filePath.substring(1);
            File fileToUpload = pair.getValue();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl(cacheControlHeader);
            InputStream contentStream;
            if (textFileMimeTypeMapping.containsKey(FilenameUtils.getExtension(filePath))) {
                FileInputStream fis = new FileInputStream(fileToUpload);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream gzipOS = new GZIPOutputStream(bos);
                IOUtils.copy(fis, gzipOS);
                gzipOS.close();
                fis.close();
                contentStream = new ByteArrayInputStream(bos.toByteArray());
                objectMetadata.setContentLength(bos.size());
                objectMetadata.setContentEncoding("gzip");
                objectMetadata.setContentType(textFileMimeTypeMapping.get(FilenameUtils.getExtension(filePath)));
            } else {
                objectMetadata.setContentLength(fileToUpload.length());
                contentStream = new FileInputStream(fileToUpload);
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, bucketPath + filePath, contentStream, objectMetadata);
            if (allowPublicAccess) {
                putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
            }
            amazonS3Client.putObject(putObjectRequest);
            getLogger().info("uploaded " + fileToUpload.getAbsolutePath());

        }
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBucketPath(String bucketPath) {
        this.bucketPath = bucketPath;
    }

    public void setInputFolders(List<String> inputFolders) {
        this.inputFolders = inputFolders;
    }

    public void setInputFilePathPatterns(List<String> inputFilePathPatterns) {
        this.inputFilePathPatterns = inputFilePathPatterns;
    }

    public void setCacheControlHeader(String cacheControlHeader) {
        this.cacheControlHeader = cacheControlHeader;
    }

    public void setAllowPublicAccess(boolean allowPublicAccess) {
        this.allowPublicAccess = allowPublicAccess;
    }

    public void setTextFileMimeTypeMapping(Map<String, String> textFileMimeTypeMapping) {
        this.textFileMimeTypeMapping = textFileMimeTypeMapping;
    }
}
