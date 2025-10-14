package me.kavin.piped.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import rocks.kavin.reqwest4j.ReqwestUtils;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DownloaderImpl extends Downloader {

    private static HttpCookie saved_cookie;
    private static long cookie_received;
    private static final Object cookie_lock = new Object();

    /**
     * Executes a request with HTTP/2.
     */
    @Override
    public Response execute(Request request) throws IOException, ReCaptchaException {

        String url = request.url();
        
        // Check if this is a YouTube request, and if so, route through our custom proxy
        if (url.contains("youtube.com") || url.contains("googlevideo.com")) {
            return executeWithProxy(request);
        } else {
            // Use the normal downloader for non-YouTube requests
            return executeNormal(request);
        }
    }
    
    private Response executeWithProxy(Request request) throws IOException, ReCaptchaException {
        String originalUrl = request.url();
        String proxyUrl = ProxyUtils.getNextProxyUrl();
        String modifiedUrl = proxyUrl + "/" + originalUrl.replace("https://", "").replace("http://", "");

        // Prepare headers
        var bytes = request.dataToSend();
        Map<String, String> headers = new java.util.HashMap<>();

        if (saved_cookie != null && !saved_cookie.hasExpired())
            headers.put("Cookie", saved_cookie.getName() + "=" + saved_cookie.getValue());

        request.headers().forEach((name, values) -> {
            for (String value : values) {
                headers.put(name, value);
            }
        });

        // Add user agent if not present
        if (!headers.containsKey("User-Agent")) {
            headers.put("User-Agent", Constants.USER_AGENT);
        }

        var future = ReqwestUtils.fetch(modifiedUrl, request.httpMethod(), bytes, headers);

        var responseFuture = future.thenApplyAsync(resp -> {
            Map<String, List<String>> headerMap = resp.headers().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            e -> List.of(e.getValue())
                    ));

            return new Response(resp.status(), null, headerMap, new String(resp.body()),
                    resp.finalUrl());
        }, Multithreading.getCachedExecutor());

        try {
            return responseFuture.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException(e);
        }
    }

    private Response executeNormal(Request request) throws IOException, ReCaptchaException {
        // Use the same code as original DownloaderImpl for non-YouTube requests
        var bytes = request.dataToSend();
        Map<String, String> headers = new Object2ObjectOpenHashMap<>();

        if (saved_cookie != null && !saved_cookie.hasExpired())
            headers.put("Cookie", saved_cookie.getName() + "=" + saved_cookie.getValue());

        request.headers().forEach((name, values) -> values.forEach(value -> headers.put(name, value)));

        var future = ReqwestUtils.fetch(request.url(), request.httpMethod(), bytes, headers);

        var responseFuture = future.thenApplyAsync(resp -> {
            Map<String, List<String>> headerMap = resp.headers().entrySet().stream()
                    .collect(Object2ObjectOpenHashMap::new, (m, e) -> m.put(e.getKey(), List.of(e.getValue())), Map::putAll);

            return new Response(resp.status(), null, headerMap, new String(resp.body()),
                    resp.finalUrl());
        }, Multithreading.getCachedExecutor());

        try {
            return responseFuture.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException(e);
        }
    }
}
