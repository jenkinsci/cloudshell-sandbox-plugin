package org.jenkinsci.plugins.cloudshell;

public class RestResponse
{
    private String content;
    private int httpCode;

    public RestResponse(String content, int exitCode) {
        this.content = content;
        this.httpCode = exitCode;
    }

    public String getContent() {
        return content;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
