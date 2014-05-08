package io.staticcdn.sdk.client;

import io.staticcdn.sdk.client.model.OptimiseRequest;
import io.staticcdn.sdk.client.model.OptimiseScanRule;
import io.staticcdn.sdk.client.model.OptimiserOptions;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptimiseRequestBuilder {
    private static Logger logger = Logger.getLogger(OptimiseRequestBuilder.class.getName());
    OptimiseRequest optimiseRequest = new OptimiseRequest();
    Map<String,File> path2fileMapping;
    public OptimiseRequestBuilder(Map<String,File> path2fileMapping) {
        this.path2fileMapping=path2fileMapping;
    }

    public OptimiseRequest build(){
        return optimiseRequest;
    }

    public OptimiseRequestBuilder options(OptimiserOptions optimiserOptions) {
        optimiseRequest.setOptimiserOptions(optimiserOptions);
        return this;
    }

    public OptimiseRequestBuilder paths(Map<String,String> paths) {
        optimiseRequest.setPaths(paths);
        return this;
    }

    public OptimiseRequestBuilder collectFiles(List<OptimiseScanRule> optimiseScanRules,List<File> inputWwwRoots, String filePath )  throws Exception {
        collectSingleFile(optimiseScanRules, inputWwwRoots, filePath, true);
        return this;
    }


    private void collectSingleFile(List<OptimiseScanRule> optimiseScanRules,List<File> inputWwwRoots, String filePath, boolean isConfiguredFile) throws Exception {
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
                String key = DigestUtils.md5Hex(checkForUtf8BOMAndDiscardIfAny(new FileInputStream(inputFile))) + "." + FilenameUtils.getExtension(inputFile.getName());
                optimiseRequest.addPath(filePath, key);
                path2fileMapping.put(filePath,inputFile);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("collected file " + filePath + " : " + key);
                }

                for (OptimiseScanRule optimiseScanRule :  optimiseScanRules) {
                    if (Pattern.compile(optimiseScanRule.getExtensionPattern()).matcher(inputFile.getName()).find()) {
                        String fileText = FileUtils.readFileToString(inputFile, "UTF-8");
                        Matcher urlMatcher = Pattern.compile(optimiseScanRule.getUrlPattern()).matcher(fileText);
                        while (urlMatcher.find()) {
                            collectFoundUrl( optimiseScanRules,inputWwwRoots, inputWwwRoot, inputFile, urlMatcher.group(optimiseScanRule.getUrlGroupIndex()));
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

    private void collectFoundUrl(List<OptimiseScanRule> optimiseScanRules,List<File> inputWwwRoots,  File inputWwwRoot, File inputFile, String foundUrl) throws Exception {
        if (!foundUrl.startsWith("data:") && foundUrl.indexOf("//") < 0) {
            String embedPath;
            if (foundUrl.charAt(0) == '/') {
                embedPath = foundUrl;
            } else {
                File embedFile = new File(inputFile.getParentFile(), foundUrl);
                embedPath = embedFile.getAbsolutePath().substring(inputWwwRoot.getAbsolutePath().length());
                embedPath = FilenameUtils.normalize(embedPath);
            }
            collectSingleFile( optimiseScanRules,inputWwwRoots, embedPath, false);
        }
    }

    public static InputStream checkForUtf8BOMAndDiscardIfAny(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
        byte[] bom = new byte[3];
        if (pushbackInputStream.read(bom) != -1) {
            if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
                pushbackInputStream.unread(bom);
            }
        }
        return pushbackInputStream;
    }

}
