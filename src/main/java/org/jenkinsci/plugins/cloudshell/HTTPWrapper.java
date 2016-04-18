package org.jenkinsci.plugins.cloudshell;

import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class HTTPWrapper {


    public class RestResponse
    {
        private String content;
        private int exitCode;
        private String message;

        public RestResponse(String content, int exitCode, String message) {
            this.content = content;
            this.exitCode = exitCode;
            this.message = message;
        }
    }

    public static String ExecuteGet(String url, String token)
    {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Basic " + token);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        String out = "";
        try {
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
                out += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static String ExecutePost(String url, String token, String name, String duration)
    {
        duration = "PT" + duration + "M";
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        request.addHeader("Authorization", "Basic " + token);
        request.setHeader("Content-type", "application/json");
        StringEntity params = null;
        try {
            params = new StringEntity("{\"name\":\""+ name +"\",\"duration\":\""+ duration +"\"}");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        request.setEntity(params);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        String out = "";
        try {
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
                out += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static String InvokeLogin(String url, String user, String password, String domain) {
        url = url + "/Login";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        StringBuilder result = new StringBuilder();
        try {
            HttpPut putRequest = new HttpPut(url);
            putRequest.addHeader("Content-Type", "application/json");
            putRequest.addHeader("Accept", "application/json");
            JSONObject keyArg = new JSONObject();
            keyArg.put("username", user);
            keyArg.put("password", password);
            keyArg.put("domain", domain);
            StringEntity input;
            try {
                input = new StringEntity(keyArg.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "success";
            }
            putRequest.setEntity(input);
            HttpResponse response = httpClient.execute(putRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));
            String output;
            while ((output = br.readLine()) != null) {
                result.append(output);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString().replace("\"","");
    }
}
