package io.staticcdn.sdk.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.staticcdn.sdk.client.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticCdnClient {

    public static void main(String[] args) {
        for (Object key : System.getProperties().keySet()) {
            System.out.println(key + ":" + System.getProperty((String) key));
        }
    }

    private static Logger logger = Logger.getLogger(StaticCdnClient.class.getName());

    private HttpResponse lastResponse;
    private String lastResponseTextBody;
    private HttpClient httpClient;
    private ServerConfig serverConfig;
    private List<String> apiServerList;
    private String clientUserAgent;

    public StaticCdnClient() {
        this(null,null);
    }

    public StaticCdnClient(String apiKey, String apiSecret) {
        BasicHttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient(httpParams);
        httpClient = defaultHttpClient;

        setupCredentials(apiKey, apiSecret, defaultHttpClient);
        setupUserAgent();
        setupServerConfig();
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
        if (outputWwwRoot == null) {
            outputWwwRoot = inputWwwRoots.get(0);
        }
        backupExistingOutputFile(outputWwwRoot,filePath,optimisedFileNameRemoveString);

        Map<String, File> path2fileMapping = new HashMap<String, File>();
        OptimiseRequest optimiseRequest = new OptimiseRequestBuilder(path2fileMapping).options(optimiserOptions).collectFiles(serverConfig.getOptimiseScanRules(), inputWwwRoots, filePath).build();
        optimiseRequest.setRetrieveOptimisedAsText(retrieveOptimisedAsText);
        OptimiseResponse optimiseResponse = optimise(inputWwwRoots, optimiseRequest);


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
            try {
                executeRequest(new HttpGet(apiServerUrl + "/v1/config"), true);
                Gson gson = new Gson();
                return gson.fromJson(readTextBody(), ServerConfig.class);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "failed to retrieve server config from " + apiServerUrl + ": " + ex.getMessage());
            }
        }
        throw new RuntimeException("failed to retrieve server config");
    }

    private OptimiseResponse apiCallOptimise(OptimiseRequest optimiseRequest, Gson gson) throws Exception {
        Exception lastException = null;
        for (String apiServerUrl : apiServerList) {
            try {
                HttpPost request = new HttpPost(apiServerUrl + "/v1/optimiser/optimise");
                String requestBody = gson.toJson(optimiseRequest);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("request body: " + requestBody);
                }
                request.setEntity(new StringEntity(requestBody, ContentType.create("application/json", "UTF-8")));
                executeRequest(request, true);
                return gson.fromJson(readTextBody(), OptimiseResponse.class);
            } catch (Exception ex) {
                if (lastResponse != null && lastResponse.getStatusLine().getStatusCode() < 500) {
                    throw ex;
                }
                logger.log(Level.WARNING, "failed to optimise with server " + apiServerUrl + ": " + ex.getMessage());
                lastException = ex;
            }
        }
        throw lastException;
    }


    private FilesInfoResponse apiCallUpload(List<File> inputWwwRoots, Map<String, String> path2keyMapping, List<String> keys, Gson gson) throws Exception {
        Map<String, String> key2pathMapping = new HashMap<String, String>();
        for (Map.Entry<String, String> pathAndKey : path2keyMapping.entrySet()) {
            key2pathMapping.put(pathAndKey.getValue(), pathAndKey.getKey());
        }

        Exception lastException = null;
        for (String apiServerUrl : apiServerList) {
            try {
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
            } catch (Exception ex) {
                if (lastResponse != null && lastResponse.getStatusLine().getStatusCode() < 500) {
                    throw ex;
                }
                logger.log(Level.WARNING, "failed upload to server " + apiServerUrl + ": " + ex.getMessage());
                lastException = ex;
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
        FileUtils.writeStringToFile(outputFile, optimiseResponse.getOptimised(), "UTF-8");
        logger.info("optimised session "+optimiseResponse.getSignature()+" as " + outputFile.getAbsolutePath());
    }


    private void backupExistingOutputFile(File outputWwwRoot, String filePath,String optimisedFileNameRemoveString) throws Exception {
        File outputFile = new File(outputWwwRoot, filePath);
        outputFile=new File(outputFile.getParentFile(),outputFile.getName().replaceAll(optimisedFileNameRemoveString,""));
        if (outputFile.exists()) {
            File backupOriginFile = new File(outputFile.getAbsolutePath()+"_bak_"+System.currentTimeMillis());
            outputFile.renameTo(backupOriginFile);
            logger.warning("back up existing output file as:" + backupOriginFile.getAbsolutePath());
        }
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
        URI uri = request.getURI();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("executing request " + request.getMethod() + ": " + uri);
        }

        AuthCache authCache = new BasicAuthCache();
        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        authCache.put(targetHost, new BasicScheme());
        BasicHttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        request.setHeader(HttpHeaders.USER_AGENT, clientUserAgent);

        lastResponse = httpClient.execute(targetHost, request, localContext);
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
                errorMessage = request.getMethod() + " : " + uri + " failed with status code " + lastResponse.getStatusLine().getStatusCode();
            }
            throw new RuntimeException(errorMessage);

        }
        return lastResponse;
    }

    private void setupCredentials(String apiKey, String apiSecret, DefaultHttpClient defaultHttpClient) {
        if (StringUtils.isEmpty(apiKey)) {
            apiKey = System.getenv("STATIC_CDN_API_KEY");
            if (StringUtils.isEmpty(apiKey)) {
                apiKey = System.getProperty("staticCdnApiKey");
                if (StringUtils.isEmpty(apiKey)) {
                    apiKey = "anonymous";
                }
            }
        }
        if (StringUtils.isEmpty(apiSecret)) {
            apiSecret = System.getenv("STATIC_CDN_API_SECRET");
            if (StringUtils.isEmpty(apiSecret)) {
                apiSecret = System.getProperty("staticCdnApiSecret");
                if (StringUtils.isEmpty(apiSecret)) {
                    apiSecret = "none";
                }
            }
        }
        defaultHttpClient.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(apiKey, apiSecret)
        );
    }


    private void setupUserAgent() {
        String clientVersion = "unknown";
        try {
            for (String line : IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream("META-INF/maven/io.staticcdn.sdk/staticcdn-sdk-client/pom.properties"))) {
                if (line.startsWith("version=")) {
                    clientVersion = line.substring(line.indexOf("=") + 1);
                }
            }
        } catch (Exception ex) {
        }
        clientUserAgent = "staticcdn-sdk-client " + clientVersion + " (" + System.getProperty("os.name") + " " + System.getProperty("os.version") + "/" + System.getProperty("java.vm.name") + " " + System.getProperty("java.runtime.version") + ")";
    }

    private void setupServerConfig() {
        this.apiServerList = new ArrayList<String>();
        if (System.getProperty("staticCdnApiServerBaseUrl") != null) {
            this.apiServerList.add(System.getProperty("staticCdnApiServerBaseUrl"));
            serverConfig = apiCallConfig();
        } else {
            this.apiServerList.add("https://api.staticcdn.io");
            this.apiServerList.add("https://primary-api.staticcdn.io");
            this.apiServerList.add("https://backup-api.staticcdn.io");
            serverConfig = apiCallConfig();
            this.apiServerList = serverConfig.getApiServerList();
        }
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
