package io.staticcdn.sdk.client;

import io.staticcdn.sdk.client.model.MimeType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MimeTypeResolver {

    private Map<String, MimeType> extensionToMimeMap=new LinkedHashMap<String, MimeType>();
    private Map<String, String> contentTypeToExtensionMap = new HashMap<String, String>();

    public MimeTypeResolver() {
        extensionToMimeMap.put("js",new MimeType("application/javascript",true,"js"));
        extensionToMimeMap.put("json",new MimeType("application/json",true,"json"));
        extensionToMimeMap.put("xml",new MimeType("application/xml",true,"xml"));
        for (MimeType mimeType : extensionToMimeMap.values()) {
            if(!contentTypeToExtensionMap.containsKey(mimeType.getExtension())){
                contentTypeToExtensionMap.put(normalizeContentType(mimeType.getContentType()), mimeType.getExtension());
            }
        }
        loadFromMimeTypeFile();
    }

    private void loadFromMimeTypeFile(){
        try{
            InputStream resourceAsStream = this.getClass().getResourceAsStream("mime.types");
            for (String line : IOUtils.readLines(resourceAsStream)) {
                line = line.trim();
                if (line.length() > 0 && line.indexOf('#') != 0) {
                    String[] lineInfo = line.split("\\s");
                    if(lineInfo.length>0){
                        String contentType=null;
                        for (String segment : lineInfo) {
                            if(segment.length()>0){
                                if(contentType==null){
                                    contentType=segment;
                                }else{
                                    addMimeType(contentType,segment);
                                }
                            }
                        }
                    }
                }
            }
        }catch (RuntimeException ex){
            throw ex;
        }catch (Exception ex){
            throw new RuntimeException("failed to load mime.types file",ex);
        }
    }

    private void addMimeType(String contentType, String extension) {
        contentType = normalizeContentType(contentType);
        if(!contentTypeToExtensionMap.containsKey(contentType)){
            contentTypeToExtensionMap.put(contentType,extension);
        }
        if(!extensionToMimeMap.containsKey(extension)){
            boolean isText=contentType.indexOf("text")==0 || contentType.indexOf("xml")>=0;
            MimeType mimeType=new MimeType(contentType,isText,extension);
            extensionToMimeMap.put(extension,mimeType);
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType.indexOf(";") > 0) {
            contentType = contentType.substring(0, contentType.indexOf(";"));
        }
        contentType = contentType.toLowerCase().trim();
        return contentType;
    }

    public MimeType resolveMime(String fileName) {
        return resolveMimeByExtension(FilenameUtils.getExtension(fileName));
    }

    public MimeType resolveMimeByExtension(String fileExtension) {
        return extensionToMimeMap.get(fileExtension.toLowerCase());
    }

    public MimeType resolveMimeByContentType(String contentType) {
        String ext = contentTypeToExtensionMap.get(normalizeContentType(contentType));
        if(ext!=null){
            return extensionToMimeMap.get(ext);
        }
        return null;
    }
}
