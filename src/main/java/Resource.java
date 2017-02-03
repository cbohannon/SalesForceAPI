import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

class Resource {
    int exitValue = 0;

    int Login(ProtocolBuilder protocolBuilder) {
        try {
            // Execute the login POST request
            protocolBuilder.httpResponse = protocolBuilder.httpclient.execute(protocolBuilder.httpPost);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // Verify the response is an HTTP OK
        int statusCode = 0;
        if (protocolBuilder.httpResponse != null) {
            statusCode = protocolBuilder.httpResponse.getStatusLine().getStatusCode();
        }
        if (statusCode != HttpStatus.SC_OK) {
            System.out.println("Error authenticating to Force.com: "+statusCode);
            return exitValue = -1;
        }

        String getResult = null;
        try {
            getResult = EntityUtils.toString(protocolBuilder.httpResponse.getEntity());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        String loginAccessToken = null;
        String loginInstanceUrl = null;
        try {
            if (getResult != null) {
                jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            }
            loginAccessToken = jsonObject.getString("access_token");
            loginInstanceUrl = jsonObject.getString("instance_url");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }

        System.out.println(protocolBuilder.httpResponse.getStatusLine());
        System.out.println("Successful login");
        System.out.println("  instance URL: "+loginInstanceUrl);
        System.out.println("  access token/session ID: "+loginAccessToken);

        // Release the connection
        protocolBuilder.httpPost.releaseConnection();
        return 0;
    }
}