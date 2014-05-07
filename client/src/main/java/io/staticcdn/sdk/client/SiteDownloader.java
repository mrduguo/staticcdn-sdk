package io.staticcdn.sdk.client;

import io.staticcdn.sdk.client.model.MimeType;
import io.staticcdn.sdk.client.model.OptimiseScanRule;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteDownloader {
    private static Logger log = Logger.getLogger(SiteDownloader.class.getName());
    Pattern CACHE_CONTROL_MAX_AGE_PATTERN = Pattern.compile("max-age\\s*=\\s*([\\d]+)");

    private MimeTypeResolver mimeTypeResolver;
    private List<OptimiseScanRule> optimiseScanRules;
    private File workspace;
    private String urlToDownload;
    private String userAgent;
    private HttpClient httpClient;
    private BasicHttpParams httpParams;
    private Map<String, String> urlToFileMapping;

    public SiteDownloader(MimeTypeResolver mimeTypeResolver, List<OptimiseScanRule> optimiseScanRules, File workspace, String urlToDownload, String userAgent) {
        this.mimeTypeResolver = mimeTypeResolver;
        this.optimiseScanRules = optimiseScanRules;
        this.workspace = workspace;
        if(retrieveBaseUrl(urlToDownload).length()>urlToDownload.length()){
            this.urlToDownload=retrieveBaseUrl(urlToDownload);
        }else{
            this.urlToDownload = urlToDownload;
        }
        if (StringUtils.isEmpty(userAgent)) {
            this.userAgent = "Static CDN Site Downloader";
        } else {
            this.userAgent = userAgent;
        }
        httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
        httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 4000);
        httpClient = new DefaultHttpClient(httpParams);
        urlToFileMapping = new HashMap<String, String>();
    }

    /**
     * @return root file path downloaded
     */
    public String download() throws Exception {
        String path = downloadUrl(urlToDownload, true);
        collectEmbedFiles(path, urlToDownload);
        return path;
    }

    void collectEmbedFiles(String selectedPath, String selectedUrl) throws Exception {
        if (urlToFileMapping.size() > 500) {
            throw new RuntimeException("exceed max 500 files referenced on a page");
        }
        String inputExtension = FilenameUtils.getExtension(selectedPath);
        MimeType inputMime = mimeTypeResolver.resolveMimeByExtension(inputExtension);

        if (inputMime.isText()) {
            File inputFile = new File(workspace, selectedPath);
            String fileText = FileUtils.readFileToString(inputFile, "UTF-8");
            for (OptimiseScanRule optimiseScanRule : optimiseScanRules) {
                if (Pattern.compile(optimiseScanRule.getExtensionPattern()).matcher(inputFile.getName()).find()) {
                    StringBuffer localisedReplacedContent = new StringBuffer();
                    Matcher urlMatcher = Pattern.compile(optimiseScanRule.getUrlPattern()).matcher(fileText);
                    while (urlMatcher.find()) {
                        String foundUrl = urlMatcher.group(optimiseScanRule.getUrlGroupIndex());
                        handleFoundUrlMatcher(selectedUrl, inputFile, foundUrl, localisedReplacedContent, urlMatcher);
                    }
                    urlMatcher.appendTail(localisedReplacedContent);
                    fileText = localisedReplacedContent.toString();
                }
            }
            FileUtils.writeStringToFile(inputFile, fileText, "UTF-8");
        }
    }

    public void handleFoundUrlMatcher(String selectedUrl, File inputFile, String foundUrl, StringBuffer localisedReplacedContent, Matcher urlMatcher) {
        String newContent = null;
        if (foundUrl.indexOf("?#") > 0) {
            foundUrl = foundUrl.substring(0, foundUrl.indexOf("?#"));
        }
        try {
            String collectedPath = collectUrl(inputFile, selectedUrl, foundUrl);
            if (collectedPath != null) {
                newContent = urlMatcher.group().replace(foundUrl, collectedPath);
            }
        } catch (Exception ex) {
            log.warning("failed to collect url " + foundUrl + " with selected url " + selectedUrl + ": " + ex.getMessage());
        }
        if (newContent == null) {
            newContent = urlMatcher.group();
        }
        urlMatcher.appendReplacement(localisedReplacedContent, urlMatcher.quoteReplacement(newContent));
    }

    String collectUrl(File hostFile, String hostUrl, String foundUrl) throws Exception {
        if (foundUrl.startsWith("data:")) {
            return null;
        }
        if (foundUrl.indexOf("#") > 0) {
            foundUrl = foundUrl.substring(0, foundUrl.indexOf("#"));
        }
        int domainStartAt = foundUrl.indexOf("//");
        if (domainStartAt == 0) {
            foundUrl = "http:" + foundUrl;
        } else if (domainStartAt < 0) {
            String embedPath = null;
            if (foundUrl.charAt(0) == '/') {
                embedPath = foundUrl.substring(1);
            } else {
                File embedFile = new File(hostFile.getParentFile(), foundUrl);
                embedPath = embedFile.getAbsolutePath().substring(workspace.getAbsolutePath().length()+1);
            }
            embedPath = FilenameUtils.normalize(embedPath);
            foundUrl = retrieveBaseUrl(hostUrl) + embedPath;
        }

        if (urlToFileMapping.containsKey(foundUrl)) {
            return urlToFileMapping.get(foundUrl);
        }

        String collectedPath = downloadUrl(foundUrl, false);
        if (collectedPath != null) {
            urlToFileMapping.put(foundUrl, collectedPath);
            collectEmbedFiles(collectedPath, foundUrl);
        }
        return collectedPath;
    }

    String downloadUrl(String url, boolean isRoot) throws Exception {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "#" + urlToFileMapping.size() + " downloading " + url);
        }
        File downloadedFile = null;
        String localPath = convertUrlToLocalFilePath(url);
        String fileName = localPath.substring(localPath.lastIndexOf('/') + 1);
        MimeType mimeType = mimeTypeResolver.resolveMime(fileName);
        File expireFile = new File(workspace, localPath+"_expire");
        if (expireFile.exists()) {
            if (System.currentTimeMillis() < Long.parseLong(FileUtils.readFileToString(expireFile))) {
                downloadedFile = new File(workspace, localPath);
            }
        }

        if (downloadedFile == null) {
            try {
                HttpResponse resourceResponse = null;
                if (isRoot) {
                    for (int i = 0; i < 5; i++) {
                        HttpGet httpGet = new HttpGet(url);
                        httpGet.setHeader(HttpHeaders.USER_AGENT, userAgent);
                        resourceResponse = httpClient.execute(httpGet);
                        if (resourceResponse.getFirstHeader(HttpHeaders.LOCATION) != null) {
                            url = resourceResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
                            urlToDownload = url;
                            resourceResponse.getEntity().getContent().close();
                            log.fine("follow redirect to url " + url);
                        } else {
                            httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
                            break;
                        }
                    }
                } else {
                    HttpGet httpGet = new HttpGet(url);
                    httpGet.setHeader(HttpHeaders.USER_AGENT, userAgent);
                    resourceResponse = httpClient.execute(httpGet);
                }
                if (resourceResponse.getStatusLine().getStatusCode() == 200) {
                    if (mimeType == null) {
                        mimeType = mimeTypeResolver.resolveMimeByContentType(resourceResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());
                        if (mimeType == null) {
                            throw new RuntimeException("failed to resolve mime for content type " + resourceResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());
                        }
                        localPath += "." + mimeType.getExtension();
                    }
                    downloadedFile = new File(workspace, localPath);

                    if (!downloadedFile.getParentFile().exists()) {
                        downloadedFile.getParentFile().mkdirs();
                    }
                    copyStreamToFile(resourceResponse.getEntity().getContent(),downloadedFile);
                    if (isRoot) {
                        if (mimeType.getExtension().equals("html")) {
                            String downloadedText = FileUtils.readFileToString(downloadedFile);
                            String newHtmlWithBaseHref = downloadedText.replaceFirst("(?im)(<head[^>]*)(>)", "$1><base href=\"" + url.substring(0, url.lastIndexOf('/') + 1) + "\"/>");
                            FileUtils.write(downloadedFile, newHtmlWithBaseHref);
                        }
                    } else {
                        markExpireTime(resourceResponse, downloadedFile);
                    }
                } else {
                    resourceResponse.getEntity().getContent().close();
                    String msg = "Failed to get url " + url + " with status code " + resourceResponse.getStatusLine().getStatusCode();
                    if (isRoot) {
                        throw new RuntimeException(msg);
                    } else {
                        log.warning(msg);
                        return null;
                    }
                }
            } catch (Exception ex) {
                String msg = "Failed to get url " + url + " with exception " + ex.getMessage();
                if (isRoot) {
                    throw new RuntimeException(msg);
                } else {
                    log.warning(msg);
                    return null;
                }

            }
        }
        return localPath;
    }

    protected void copyStreamToFile(InputStream inputStream,File downloadedFile) throws Exception {
        FileUtils.copyInputStreamToFile(inputStream, downloadedFile);
    }


    protected String retrieveBaseUrl(String url) {
        int protocolSplitter = url.indexOf("//");
        int domainSplitterStart = 0;
        if (protocolSplitter > 0) {
            domainSplitterStart = protocolSplitter + 2;
        }
        int domainSplitter = url.indexOf('/', domainSplitterStart);
        if (domainSplitter > 0) {
            return url.substring(0, domainSplitter+1);
        } else {
            return url+"/";
        }
    }

    String convertUrlToLocalFilePath(String url) {
        String baseUrl = retrieveBaseUrl(url);
        String localPath=url.substring(baseUrl.length()-1);
        if (localPath.endsWith("/")) {
            localPath += "index";
        }
        if (localPath.indexOf("?") > 0) {
            int fileNameSplitter = localPath.lastIndexOf('/') + 1;
            localPath = localPath.substring(0, fileNameSplitter) + DigestUtils.md5Hex(localPath.substring(localPath.indexOf("?") + 1)) + "_" + localPath.substring(fileNameSplitter, localPath.indexOf('?'));
        }
        return localPath;
    }

    public void markExpireTime(HttpResponse resourceResponse, File downloadedFile) throws Exception {
        Header cacheControl = resourceResponse.getFirstHeader(HttpHeaders.CACHE_CONTROL);
        if (cacheControl != null) {
            Matcher maxAgeMatcher = CACHE_CONTROL_MAX_AGE_PATTERN.matcher(cacheControl.getValue());
            if (maxAgeMatcher.find()) {
                long maxAge = Long.parseLong(maxAgeMatcher.group(1));
                Header age = resourceResponse.getFirstHeader(HttpHeaders.AGE);
                if (age != null) {
                    maxAge = maxAge - Long.parseLong(age.getValue());
                }
                if (maxAge > 0) {
                    if (maxAge > 86400) { // cache max 1 day
                        maxAge = 86400;
                    }
                    long expire = System.currentTimeMillis() + (maxAge * 1000);
                    FileUtils.write(new File(downloadedFile.getAbsolutePath() + "_expire"), String.valueOf(expire));
                }
            }
        }
    }
}
