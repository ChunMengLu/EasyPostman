package com.laker.postman.service.har;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.laker.postman.model.HttpRequestItem;
import com.laker.postman.model.RequestGroup;

import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * HAR (HTTP Archive) 格式导出器
 * 负责将内部数据结构导出为 HAR 1.2 格式
 */
public class HarExporter {

    private static final String GROUP = "group";
    private static final String REQUEST = "request";
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));

    /**
     * 私有构造函数，防止实例化
     */
    private HarExporter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 从树节点构建 HAR 1.2 JSON
     *
     * @param groupNode 分组节点
     * @param groupName 分组名（如果根节点有 RequestGroup，会使用其名称）
     * @return HAR 1.2 JSONObject
     */
    public static JSONObject buildHarFromTreeNode(DefaultMutableTreeNode groupNode, String groupName) {
        JSONObject har = new JSONObject();
        JSONObject log = new JSONObject();
        
        log.put("version", "1.2");
        
        // Creator 信息
        JSONObject creator = new JSONObject();
        creator.put("name", "EasyPostman");
        creator.put("version", "1.0");
        log.put("creator", creator);
        
        // 检查根节点是否有 RequestGroup 对象
        Object userObj = groupNode.getUserObject();
        if (userObj instanceof Object[] obj && GROUP.equals(obj[0])) {
            Object groupData = obj[1];
            if (groupData instanceof RequestGroup group) {
                groupName = group.getName();
            }
        }
        
        // 收集所有请求
        List<HttpRequestItem> requests = collectRequestsFromNode(groupNode);
        
        // 构建 entries
        JSONArray entries = new JSONArray();
        String now = ISO_DATE_TIME_FORMATTER.format(Instant.now());
        
        for (HttpRequestItem request : requests) {
            JSONObject entry = buildHarEntry(request, now);
            entries.add(entry);
        }
        
        log.put("entries", entries);
        har.put("log", log);
        
        return har;
    }

    /**
     * 递归收集树节点中的所有请求
     */
    private static List<HttpRequestItem> collectRequestsFromNode(DefaultMutableTreeNode node) {
        List<HttpRequestItem> requests = new ArrayList<>();
        
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            Object userObj = child.getUserObject();
            
            if (userObj instanceof Object[] obj) {
                if (REQUEST.equals(obj[0]) && obj[1] instanceof HttpRequestItem request) {
                    requests.add(request);
                } else if (GROUP.equals(obj[0])) {
                    // 递归处理子分组
                    requests.addAll(collectRequestsFromNode(child));
                }
            }
        }
        
        return requests;
    }

    /**
     * 构建单个 HAR entry
     */
    private static JSONObject buildHarEntry(HttpRequestItem request, String startedDateTime) {
        JSONObject entry = new JSONObject();
        entry.put("startedDateTime", startedDateTime);
        entry.put("time", 0); // 默认时间
        
        // Request
        JSONObject harRequest = new JSONObject();
        harRequest.put("method", request.getMethod());
        harRequest.put("url", request.getUrl());
        harRequest.put("httpVersion", "HTTP/1.1");
        
        // Headers
        JSONArray headers = new JSONArray();
        if (request.getHeadersList() != null) {
            for (com.laker.postman.model.HttpHeader header : request.getHeadersList()) {
                if (header.isEnabled()) {
                    JSONObject h = new JSONObject();
                    h.put("name", header.getKey());
                    h.put("value", header.getValue());
                    headers.add(h);
                }
            }
        }
        harRequest.put("headers", headers);
        
        // Query String
        JSONArray queryString = new JSONArray();
        if (request.getParamsList() != null) {
            for (com.laker.postman.model.HttpParam param : request.getParamsList()) {
                if (param.isEnabled()) {
                    JSONObject q = new JSONObject();
                    q.put("name", param.getKey());
                    q.put("value", param.getValue());
                    queryString.add(q);
                }
            }
        }
        harRequest.put("queryString", queryString);
        
        // Post Data
        JSONObject postData = null;
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            postData = new JSONObject();
            postData.put("mimeType", "application/json");
            postData.put("text", request.getBody());
        } else if (request.getUrlencodedList() != null && !request.getUrlencodedList().isEmpty()) {
            postData = new JSONObject();
            postData.put("mimeType", "application/x-www-form-urlencoded");
            JSONArray params = new JSONArray();
            StringBuilder textBuilder = new StringBuilder();
            for (com.laker.postman.model.HttpFormUrlencoded item : request.getUrlencodedList()) {
                if (item.isEnabled()) {
                    JSONObject p = new JSONObject();
                    p.put("name", item.getKey());
                    p.put("value", item.getValue());
                    params.add(p);
                    
                    if (textBuilder.length() > 0) {
                        textBuilder.append("&");
                    }
                    textBuilder.append(URLEncoder.encode(item.getKey(), StandardCharsets.UTF_8))
                               .append("=")
                               .append(URLEncoder.encode(item.getValue(), StandardCharsets.UTF_8));
                }
            }
            postData.put("params", params);
            postData.put("text", textBuilder.toString());
        } else if (request.getFormDataList() != null && !request.getFormDataList().isEmpty()) {
            postData = new JSONObject();
            postData.put("mimeType", "multipart/form-data");
            JSONArray params = new JSONArray();
            for (com.laker.postman.model.HttpFormData item : request.getFormDataList()) {
                if (item.isEnabled()) {
                    JSONObject p = new JSONObject();
                    p.put("name", item.getKey());
                    if (item.isFile()) {
                        p.put("fileName", item.getValue());
                    } else {
                        p.put("value", item.getValue());
                    }
                    params.add(p);
                }
            }
            postData.put("params", params);
        }
        
        if (postData != null) {
            harRequest.put("postData", postData);
        }
        
        harRequest.put("headersSize", -1);
        harRequest.put("bodySize", -1);
        entry.put("request", harRequest);
        
        // Response (可选，这里创建一个空的响应)
        JSONObject response = new JSONObject();
        response.put("status", 0);
        response.put("statusText", "");
        response.put("httpVersion", "HTTP/1.1");
        response.put("headers", new JSONArray());
        response.put("content", new JSONObject());
        response.put("redirectURL", "");
        response.put("headersSize", -1);
        response.put("bodySize", -1);
        entry.put("response", response);
        
        // Cache
        entry.put("cache", new JSONObject());
        
        // Timings
        JSONObject timings = new JSONObject();
        timings.put("blocked", -1);
        timings.put("dns", -1);
        timings.put("connect", -1);
        timings.put("send", 0);
        timings.put("wait", 0);
        timings.put("receive", 0);
        timings.put("ssl", -1);
        entry.put("timings", timings);
        
        // Comment (使用请求名称)
        if (request.getName() != null && !request.getName().isEmpty()) {
            entry.put("comment", request.getName());
        }
        
        return entry;
    }
}

