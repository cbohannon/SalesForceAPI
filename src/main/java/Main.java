import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

public class Main {
    private static final String USERNAME = "yourusername";
    private static final String PASSWORD = "yourpassword";
    private static final String LOGINURL = "https://login.salesforce.com";
    private static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
    private static final String CONSUMERKEY = "yourconsumerkey";
    private static final String CONSUMERSECRET = "yourconsumersecret";

    public static void main(String args[]) {
        HttpClient httpclient = HttpClientBuilder.create().build();

        // Assemble the login request URL
        String loginURL = LOGINURL + GRANTSERVICE + "&client_id="
                                   + CONSUMERKEY + "&client_secret="
                                   + CONSUMERSECRET + "&username="
                                   + USERNAME + "&password="
                                   + PASSWORD;

        // Login requests must be a POST method
        HttpPost httpPost = new HttpPost(loginURL);
        HttpResponse response = null;

        try {
            // Execute the login POST request
            response = httpclient.execute(httpPost);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // Verify the response is an HTTP OK
        int statusCode = 0;
        if (response != null) {
            statusCode = response.getStatusLine().getStatusCode();
        }
        if (statusCode != HttpStatus.SC_OK) {
            System.out.println("Error authenticating to Force.com: "+statusCode);
            return;
        }

        String getResult = null;
        try {
            getResult = EntityUtils.toString(response.getEntity());
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

        System.out.println(response.getStatusLine());
        System.out.println("Successful login");
        System.out.println("  instance URL: "+loginInstanceUrl);
        System.out.println("  access token/session ID: "+loginAccessToken);

        // Release the connection
        httpPost.releaseConnection();

        System.exit(0);
    }
}