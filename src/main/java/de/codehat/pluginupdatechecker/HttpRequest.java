package de.codehat.pluginupdatechecker;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

final class HttpRequest {

    private static final String USER_AGENT = "Plugin-Updater by CodeHat";

    static enum HttpMethod {
      GET, POST
    }

    /**
     * Prepares the HTTP connection by setting user-agent, method type and SSL support if required.
     *
     * @param url the URL the connection is opened to
     * @param method the HTTP method. E.g GET or POST
     * @return the opened HTTP(s) connection ready to get the response
     * @throws IOException thrown if given URL isn't reached
     */
    private static URLConnection prepareConnections(URL url, HttpMethod method) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(method.toString());
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setReadTimeout(3000);
        connection.setConnectTimeout(3000);
        return connection;
    }

    static String get(String url) throws IOException {
        return get(new URL(url));
    }

    static String get(URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) prepareConnections(url, HttpMethod.GET);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String input;
        StringBuilder responseBuilder = new StringBuilder();
        while ((input = in.readLine()) != null) {
            responseBuilder.append(input);
        }
        in.close();
        return responseBuilder.toString();
    }

}
