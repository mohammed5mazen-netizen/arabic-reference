package com.mrsoft.arabicreference.tools.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tools")
public class ToolsProperties {

    private int rateLimitPerMinute = 90;
    private Duration cacheTtl = Duration.ofMinutes(10);
    private int graphDepth = 2;
    private int graphNodes = 50;
    private int candidateCap = 8;

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public int getGraphDepth() {
        return graphDepth;
    }

    public void setGraphDepth(int graphDepth) {
        this.graphDepth = Math.min(graphDepth, 2);
    }

    public int getGraphNodes() {
        return graphNodes;
    }

    public void setGraphNodes(int graphNodes) {
        this.graphNodes = Math.min(graphNodes, 50);
    }

    public int getCandidateCap() {
        return candidateCap;
    }

    public void setCandidateCap(int candidateCap) {
        this.candidateCap = Math.min(candidateCap, 20);
    }
}
