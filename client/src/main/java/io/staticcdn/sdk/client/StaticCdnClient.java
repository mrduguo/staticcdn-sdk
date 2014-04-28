package io.staticcdn.sdk.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.staticcdn.sdk.client.model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticCdnClient {
    private static Logger logger = Logger.getLogger(StaticCdnClient.class.getName());

    private HttpResponse lastResponse;
    private String lastResponseTextBody;
    private HttpClient httpClient;
    private ServerConfig serverConfig;
    private List<String> apiServerList;

    public StaticCdnClient() {
        BasicHttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        httpClient = new DefaultHttpClient(httpParams);
        this.apiServerList =new ArrayList<String>();
        if(System.getProperty("staticCdnApiServerBaseUrl")!=null){
            this.apiServerList.add(System.getProperty("staticCdnApiServerBaseUrl"));
        }else{
            this.apiServerList.add("https://api.staticcdn.io");
            this.apiServerList.add("https://primary-api.staticcdn.io");
            this.apiServerList.add("https://backup-api.staticcdn.io");
        }
        serverConfig = apiCallConfig();
        this.apiServerList =serverConfig.getApiServerList();
    }

    public OptimiseResponse optimise(
            List<File> inputWwwRoots,
            File outputWwwRoot,
            String filePath,
            OptimiserOptions optimiserOptions,
            boolean retrieveOptimisedAsText,
            String optimisedFileNamePrefix,
            String optimisedFileNameSuffix,
            String optimisedFileNameRemoveString
    ) throws Exception {

        OptimiseRequest optimiseRequest = new OptimiseRequest();
        optimiseRequest.setOptimiserOptions(optimiserOptions);
        optimiseRequest.setRetrieveOptimisedAsText(retrieveOptimisedAsText);
        collectSingleFile(inputWwwRoots, optimiseRequest, filePath, true);
        OptimiseResponse optimiseResponse = optimise(inputWwwRoots, optimiseRequest);


        if (outputWwwRoot == null) {
            outputWwwRoot = inputWwwRoots.get(0);
        }
        writeOptimisedResultToFile(outputWwwRoot, filePath, optimiseResponse, optimisedFileNamePrefix, optimisedFileNameSuffix, optimisedFileNameRemoveString);

        return optimiseResponse;
    }

    public OptimiseResponse optimise(List<File> inputWwwRoots, OptimiseRequest optimiseRequest) throws Exception {
        long startTimestamp = System.currentTimeMillis();
        OptimiseResponse optimiseResponse;
        try {
            optimiseResponse = performOptimise(inputWwwRoots, optimiseRequest);
        } catch (Throwable ex) {
            if (ex.getMessage() == null || ex instanceof JsonSyntaxException) {
                logger.severe("last response was " + lastResponse.getStatusLine().getReasonPhrase() + " body: " + readTextBody());
            }
            throw new Exception("Failed to optimise files: " + ex.getMessage(), ex);
        }
        logger.info("optimised " + optimiseRequest.getPaths().keySet().iterator().next() + " in " + ((System.currentTimeMillis() - startTimestamp) / 1000.0) + " seconds");
        return optimiseResponse;
    }

    private ServerConfig apiCallConfig() {
        for (String apiServerUrl : apiServerList) {
            try{
                executeRequest(new HttpGet(apiServerUrl+ "/v1/config"), true);
                Gson gson = new Gson();
                return gson.fromJson(readTextBody(), ServerConfig.class);
            }catch (Exception ex){
                logger.log(Level.WARNING, "failed to retrieve server config from " + apiServerUrl +": "+ex.getMessage());
            }
        }
        throw new RuntimeException("failed to retrieve server config");
    }

    private OptimiseResponse apiCallOptimise(OptimiseRequest optimiseRequest, Gson gson) throws Exception {
        Exception lastException=null;
        for (String apiServerUrl : apiServerList) {
            try{
                HttpPost request = new HttpPost(apiServerUrl + "/v1/optimiser/optimise");
                request.setEntity(new StringEntity(gson.toJson(optimiseRequest), ContentType.create("application/json", "UTF-8")));
                executeRequest(request, true);
                return gson.fromJson(readTextBody(), OptimiseResponse.class);
            }catch (Exception ex){
                if(lastResponse!=null && lastResponse.getStatusLine().getStatusCode()<500){
                    throw ex;
                }
                logger.log(Level.WARNING, "failed to optimise with server " + apiServerUrl +": "+ex.getMessage());
                lastException=ex;
            }
        }
        throw lastException;
    }


    private FilesInfoResponse apiCallUpload(List<File> inputWwwRoots, Map<String, String> path2keyMapping, List<String> keys, Gson gson) throws Exception {
        Map<String, String> key2pathMapping = new HashMap<String, String>();
        for (Map.Entry<String, String> pathAndKey : path2keyMapping.entrySet()) {
            key2pathMapping.put(pathAndKey.getValue(), pathAndKey.getKey());
        }

        Exception lastException=null;
        for (String apiServerUrl : apiServerList) {
            try{
                HttpPost request = new HttpPost(apiServerUrl + "/v1/files/upload");
                MultipartEntity multipartEntity = new MultipartEntity();
                List<String> missingPaths = new ArrayList<String>();
                for (String missingKey : keys) {
                    for (File inputWwwRoot : inputWwwRoots) {
                        String filePath = key2pathMapping.get(missingKey);
                        File localFile = new File(inputWwwRoot, filePath);
                        if (localFile.isFile()) {
                            missingPaths.add(filePath);
                            multipartEntity.addPart("file", new FileBody(localFile));
                            multipartEntity.addPart("key", new StringBody(missingKey));
                            break;
                        }
                    }
                }
                request.setEntity(multipartEntity);
                executeRequest(request, true);
                logger.info("uploaded " + StringUtils.join(missingPaths, ','));
                return gson.fromJson(readTextBody(), FilesInfoResponse.class);
            }catch (Exception ex){
                if(lastResponse!=null && lastResponse.getStatusLine().getStatusCode()<500){
                    throw ex;
                }
                logger.log(Level.WARNING, "failed upload to server " + apiServerUrl +": "+ex.getMessage());
                lastException=ex;
            }
        }
        throw lastException;

    }


    private OptimiseResponse performOptimise(List<File> inputWwwRoots, OptimiseRequest optimiseRequest) throws Exception {
        Gson gson = new Gson();
        OptimiseResponse optimiseResponse = apiCallOptimise(optimiseRequest, gson);
        if (optimiseResponse.getCreatedAt() == null) {
            if (optimiseResponse.getMissingKeys() != null) {
                apiCallUpload(inputWwwRoots, optimiseRequest.getPaths(), optimiseResponse.getMissingKeys(), gson);
                optimiseResponse = apiCallOptimise(optimiseRequest, gson);
            }
            if (optimiseResponse.getCreatedAt() == null) {
                throw new RuntimeException(optimiseResponse.getMessage());
            }
        }
        return optimiseResponse;
    }


    private void collectSingleFile(List<File> inputWwwRoots, OptimiseRequest optimiseRequest, String filePath, boolean isConfiguredFile) throws Exception {
        if (filePath.indexOf('?') > 0) {
            filePath = filePath.substring(0, filePath.indexOf('?'));
        }
        if (filePath.indexOf('#') > 0) {
            filePath = filePath.substring(0, filePath.indexOf('#'));
        }
        filePath = filePath.replaceAll("\\\\", "/");

        if (optimiseRequest.getPaths().containsKey(filePath)) {
            return;
        }

        for (File inputWwwRoot : inputWwwRoots) {
            File inputFile = new File(inputWwwRoot, filePath);
            if (inputFile.isFile()) {
                String key = DigestUtils.md5Hex(new FileInputStream(inputFile)) + "." + FilenameUtils.getExtension(inputFile.getName());
                optimiseRequest.addPath(filePath, key);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("collected file " + filePath + " : " + key);
                }

                for (OptimiseScanRule optimiseScanRule : serverConfig.getOptimiseScanRules()) {
                    if (Pattern.compile(optimiseScanRule.getExtensionPattern()).matcher(inputFile.getName()).find()) {
                        String fileText = FileUtils.readFileToString(inputFile, "UTF-8");
                        Matcher urlMatcher = Pattern.compile(optimiseScanRule.getUrlPattern()).matcher(fileText);
                        while (urlMatcher.find()) {
                            collectFoundUrl(inputWwwRoots, optimiseRequest, inputWwwRoot, inputFile, urlMatcher.group(optimiseScanRule.getUrlGroupIndex()));
                        }
                    }
                }
                return;
            }
        }
        if (isConfiguredFile) {
            throw new IllegalArgumentException("cannot find file: " + filePath);
        } else {
            logger.warning("file " + filePath + " not found for " + optimiseRequest.getPaths().keySet().iterator().next());
        }
    }

    private void collectFoundUrl(List<File> inputWwwRoots, OptimiseRequest optimiseRequest, File inputWwwRoot, File inputFile, String foundUrl) throws Exception {
        if (!foundUrl.startsWith("data:") && foundUrl.indexOf("//") < 0) {
            String embedPath;
            if (foundUrl.charAt(0) == '/') {
                embedPath = foundUrl;
            } else {
                File embedFile = new File(inputFile.getParentFile(), foundUrl);
                embedPath = embedFile.getAbsolutePath().substring(inputWwwRoot.getAbsolutePath().length());
                embedPath = FilenameUtils.normalize(embedPath);
            }
            collectSingleFile(inputWwwRoots, optimiseRequest, embedPath, false);
        }
    }


    private void writeOptimisedResultToFile(File outputWwwRoot, String filePath, OptimiseResponse optimiseResponse, String optimisedFileNamePrefix, String optimisedFileNameSuffix, String optimisedFileNameRemoveString) throws Exception {
        String fileExtension = FilenameUtils.getExtension(filePath);
        String fileBaseName = FilenameUtils.getBaseName(filePath);
        if (optimisedFileNamePrefix != null) {
            fileBaseName = optimisedFileNamePrefix + fileBaseName;
        }
        if (optimisedFileNameSuffix != null) {
            fileExtension = fileExtension + optimisedFileNameSuffix;
        }
        File outputParentFile = new File(outputWwwRoot, filePath).getParentFile();
        File outputFile = new File(outputParentFile, fileBaseName + "." + fileExtension);
        if (outputFile.exists()) {
            File backupOriginFile = new File(outputFile.getParentFile(), fileBaseName + optimisedFileNameRemoveString + "." + fileExtension);
            FileUtils.copyFile(outputFile, backupOriginFile);
        }
        FileUtils.writeStringToFile(outputFile, optimiseResponse.getOptimised(), "UTF-8");
        logger.info("optimised file as " + outputFile.getAbsolutePath());
    }


    public synchronized HttpResponse executeGet(String path) throws Exception {
        return executeRequest(new HttpGet(apiServerList.get(0) + path), false);
    }

    public synchronized HttpResponse executeRequest(HttpUriRequest request, boolean verifyStatusOk) throws Exception {
        if (lastResponse != null) {
            if (lastResponseTextBody == null) {
                try {
                    lastResponse.getEntity().getContent().close();
                } catch (Exception ex) {
                    logger.warning("close previous connection failed with message: " + ex.getMessage());
                }
            }
            lastResponse = null;
        }
        lastResponseTextBody = null;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("executing request " + request.getMethod() + ": " + request.getURI());
        }
        lastResponse = httpClient.execute(request);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("executed request with response " + lastResponse.getStatusLine().getReasonPhrase() + " body: " + readTextBody());
        }
        if (verifyStatusOk && lastResponse.getStatusLine().getStatusCode() != 200) {
            String errorMessage = null;
            try {
                ErrorResponse errorResponse = new Gson().fromJson(readTextBody(), ErrorResponse.class);
                if (errorResponse.getMessage() != null) {
                    errorMessage = errorResponse.getMessage();
                }
            } catch (Throwable ignore) {
            }
            if (errorMessage == null) {
                errorMessage = request.getMethod() + " : " + request.getURI() + " failed with status code " + lastResponse.getStatusLine().getStatusCode();
            }
            throw new RuntimeException(errorMessage);

        }
        return lastResponse;
    }

    public HttpResponse getLastResponse() {
        return lastResponse;
    }

    public List<String> getApiServerList() {
        return apiServerList;
    }

    public String readTextBody() throws Exception {
        if (lastResponseTextBody == null) {
            lastResponseTextBody = EntityUtils.toString(lastResponse.getEntity());
        }
        return lastResponseTextBody;
    }
}
