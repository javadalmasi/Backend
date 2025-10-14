package me.kavin.piped.utils;

import me.kavin.piped.consts.Constants;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyUtils {

    // List of custom proxy URLs
    private static final List<String> PROXY_URLS = Arrays.asList(
        "https://worker-lively-heart-a8a3.tejan22105.workers.dev",
        "https://worker-wandering-frost-8f12.xoniw30989.workers.dev",
        "https://worker-flat-base-9473.jadifac309.workers.dev",
        "https://worker-proud-violet-2004.koceve9024.workers.dev",
        "https://worker-flat-paper-cf70.dokisej110.workers.dev",
        "https://worker-ancient-fire-3243.ponafa3752.workers.dev",
        "https://worker-steep-shadow-12a4.bapekir223.workers.dev",
        "https://worker-red-dream-39af.mewehen205.workers.dev",
        "https://worker-plain-pine-4adf.pamihaf390.workers.dev",
        "https://worker-purple-art-5796.tixoy98599.workers.dev",
        "https://worker-falling-sun-8f77.muspesalmo.workers.dev",
        "https://worker-steep-lake-3776.gespenipsa.workers.dev",
        "https://worker-cold-disk-a003.pobobig957.workers.dev",
        "https://worker-spring-smoke-f0e9.sicak55682.workers.dev",
        "https://worker-shy-grass-6252.fikap64022.workers.dev",
        "https://worker-cold-union-c283.gomece9692.workers.dev",
        "https://hello-world-divine-art-0a0b.labiyo8696.workers.dev",
        "https://hello-world-odd-dew-9f6f.dorara3171.workers.dev",
        "https://hello-world-white-darkness-6608.nomab71887.workers.dev",
        "https://hello-world-aged-star-f695.falebi9213.workers.dev",
        "https://hello-world-rapid-sun-10c6.vajohaf962.workers.dev",
        "https://hello-world-yellow-wave-e92b.yatef53281.workers.dev",
        "https://hello-world-young-bird-21e9.jamec80658.workers.dev",
        "https://hello-world-gentle-king-b780.tasis34415.workers.dev",
        "https://hello-world-frosty-mouse-f9b1.jobec14909.workers.dev",
        "https://hello-world-snowy-wind-29f1.dopisax783.workers.dev",
        "https://hello-world-weathered-wind-8fda.cinij34275.workers.dev",
        "https://hello-world-icy-cell-dee8.xipig97563.workers.dev"
    );

    // Counter for round-robin proxy selection
    private static final AtomicInteger proxyCounter = new AtomicInteger(0);

    /**
     * Get the next proxy URL in round-robin fashion
     */
    public static String getNextProxyUrl() {
        int index = proxyCounter.getAndIncrement() % PROXY_URLS.size();
        return PROXY_URLS.get(index);
    }

    /**
     * Create a request that goes through the custom proxy
     */
    public static Response makeRequestThroughProxy(String originalUrl) throws IOException {
        String proxyUrl = getNextProxyUrl();
        String finalUrl = proxyUrl + "/" + originalUrl.replace("https://", "").replace("http://", "");

        Request request = new Request.Builder()
                .url(finalUrl)
                .header("User-Agent", Constants.USER_AGENT)
                .build();

        return Constants.h2client.newCall(request).execute();
    }
}