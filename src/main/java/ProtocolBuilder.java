import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

class ProtocolBuilder {
    HttpClient httpclient = null;
    HttpPost httpPost = null;
    HttpResponse httpResponse = null;

    ProtocolBuilder(String loginURL, String grantService, String consumerKey,
                    String consumerSecret, String userName, String password) {
        // Assemble the login request URL
        String uri = loginURL + grantService + "&client_id="
                              + consumerKey + "&client_secret="
                              + consumerSecret + "&username="
                              + userName + "&password="
                              + password;

        // Login requests must be a POST method
        httpclient = HttpClientBuilder.create().build();
        httpPost = new HttpPost(uri);
        httpResponse = null;
    }
}