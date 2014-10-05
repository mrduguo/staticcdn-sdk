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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
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
        this(null, null);
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

    public OptimizeResponse optimize(
            List<File> inputWwwRoots,
            File outputWwwRoot,
            String filePath,
            OptimizerOptions optimizerOptions,
            String originalFileNameSuffix, String refsFileNameSuffix
    ) throws Exception {
        if (outputWwwRoot == null) {
            outputWwwRoot = inputWwwRoots.get(0);
        }
        backupExistingInputFile(inputWwwRoots, outputWwwRoot, filePath, originalFileNameSuffix);

        Map<String, File> path2fileMapping = new HashMap<String, File>();
        OptimizeRequest optimizeRequest = new OptimizeRequestBuilder(path2fileMapping).options(optimizerOptions).collectFiles(serverConfig.getOptimizeScanRules(), inputWwwRoots, filePath).build();
        OptimizeResponse optimizeResponse = optimize(inputWwwRoots, optimizeRequest);


        writeOptimizedResultToFile(outputWwwRoot, filePath, optimizeResponse,refsFileNameSuffix);

        return optimizeResponse;
    }

    public OptimizeResponse optimize(List<File> inputWwwRoots, OptimizeRequest optimizeRequest) throws Exception {
        long startTimestamp = System.currentTimeMillis();
        OptimizeResponse optimizeResponse;
        try {
            optimizeResponse = performOptimize(inputWwwRoots, optimizeRequest);
        } catch (Throwable ex) {
            if (ex.getMessage() == null || ex instanceof JsonSyntaxException) {
                logger.severe("last response was " + lastResponse.getStatusLine().getReasonPhrase() + " body: " + readTextBody());
            }
            throw new Exception("Failed to optimize files: " + ex.getMessage(), ex);
        }
        logger.info("optimized " + optimizeRequest.getPaths().keySet().iterator().next() + " in " + ((System.currentTimeMillis() - startTimestamp) / 1000.0) + " seconds");
        return optimizeResponse;
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

    private OptimizeResponse apiCallOptimize(OptimizeRequest optimizeRequest, Gson gson) throws Exception {
        Exception lastException = null;
        for (String apiServerUrl : apiServerList) {
            try {
                HttpPost request = new HttpPost(apiServerUrl + "/v1/optimizer/optimize");
                String requestBody = gson.toJson(optimizeRequest);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("request body: " + requestBody);
                }
                request.setEntity(new StringEntity(requestBody, ContentType.create("application/json", "UTF-8")));
                executeRequest(request, true);
                return gson.fromJson(readTextBody(), OptimizeResponse.class);
            } catch (Exception ex) {
                if (lastResponse != null && lastResponse.getStatusLine().getStatusCode() < 500) {
                    throw ex;
                }
                logger.log(Level.WARNING, "failed to optimize with server " + apiServerUrl + ": " + ex.getMessage());
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


    private OptimizeResponse performOptimize(List<File> inputWwwRoots, OptimizeRequest optimizeRequest) throws Exception {
        Gson gson = new Gson();
        OptimizeResponse optimizeResponse = apiCallOptimize(optimizeRequest, gson);
        if (optimizeResponse.getCreatedAt() == null) {
            if (optimizeResponse.getMissingKeys() != null) {
                apiCallUpload(inputWwwRoots, optimizeRequest.getPaths(), optimizeResponse.getMissingKeys(), gson);
                optimizeResponse = apiCallOptimize(optimizeRequest, gson);
            }
            if (optimizeResponse.getCreatedAt() == null) {
                throw new RuntimeException(optimizeResponse.getMessage());
            }
        }
        return optimizeResponse;
    }


    private void writeOptimizedResultToFile(File outputWwwRoot, String filePath, OptimizeResponse optimizeResponse,String refsFileNameSuffix) throws Exception {
        String fileExtension = FilenameUtils.getExtension(filePath);
        String fileBaseName = FilenameUtils.getBaseName(filePath);
        File outputFile = buildOutputFile(outputWwwRoot, filePath);
        FileUtils.writeStringToFile(outputFile, optimizeResponse.getOptimized(), "UTF-8");
        logger.info("optimized session " + optimizeResponse.getSignature() + " to " + outputFile.getAbsolutePath());
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


    private void backupExistingInputFile(List<File> inputWwwRoots, File outputWwwRoot, String filePath, String originalFileNameSuffix) throws Exception {
        if (!originalFileNameSuffix.equals("skip")) {
            for (File inputWwwRoot : inputWwwRoots) {
                File inputFile = new File(inputWwwRoot, filePath);
                if (inputFile.exists()) {
                    File outputFile = buildOutputFile(outputWwwRoot, filePath);
                    File backupOriginFile = new File(outputFile.getAbsolutePath() + originalFileNameSuffix);
                    if (!backupOriginFile.exists()) {
                        FileUtils.copyFile(inputFile, backupOriginFile);
                        logger.fine("back up existing output file as:" + backupOriginFile.getAbsolutePath());
                    }
                    return;
                }
            }
        }
    }


    private File buildOutputFile(File outputWwwRoot, String filePath) throws Exception {
        File outputFile = new File(outputWwwRoot, filePath);
        return outputFile;
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
            apiKey = System.getenv("STATICO_API_KEY");
            if (StringUtils.isEmpty(apiKey)) {
                apiKey = System.getProperty("staticoApiKey");
                if (StringUtils.isEmpty(apiKey)) {
                    Properties prop = new Properties();
                    File credentialFile = new File(System.getProperty("user.home"), ".statico/credentials");
                    if(credentialFile.isFile()){
                        InputStream input = null;
                        try {
                            input = new FileInputStream(credentialFile);
                            prop.load(input);
                            apiKey=prop.getProperty("apiKey");
                            apiSecret=prop.getProperty("apiSecret");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            if (input != null) {
                                try {
                                    input.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (StringUtils.isEmpty(apiKey)) {
                        apiKey = "anonymous";
                    }
                }
            }
        }
        if (StringUtils.isEmpty(apiSecret)) {
            apiSecret = System.getenv("STATICO_API_SECRET");
            if (StringUtils.isEmpty(apiSecret)) {
                apiSecret = System.getProperty("staticoApiSecret");
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
