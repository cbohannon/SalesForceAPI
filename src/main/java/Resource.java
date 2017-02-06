import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Resource {
    int exitValue = 0;

    private static final String REST_ENDPOINT = "/services/data" ;
    private static final String API_VERSION = "/v32.0" ;

    private String baseUri;
    private String leadId ;
    private String leadFirstName;
    private String leadLastName;
    private String leadCompany;

    private Header oauthHeader;
    private Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");

    private Logger logger = LoggerFactory.getLogger(Resource.class);

    int Login(ProtocolBuilder protocolBuilder) throws IOException {
        // Execute the login POST request
        protocolBuilder.httpResponse = protocolBuilder.httpclient.execute(protocolBuilder.httpPost);

        // Verify the response is an HTTP OK
        int statusCode = 0;
        if (protocolBuilder.httpResponse != null) {
            statusCode = protocolBuilder.httpResponse.getStatusLine().getStatusCode();
        }
        if (statusCode != HttpStatus.SC_OK) {
            logger.info("Error authenticating to Force.com: "+statusCode);
            return exitValue = -1;
        }

        String getResult = EntityUtils.toString(protocolBuilder.httpResponse.getEntity());

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

        baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION ;
        oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken) ;

        logger.info("oauthHeader1: " + oauthHeader);
        logger.info(protocolBuilder.httpResponse.getStatusLine().toString());
        logger.info("Successful login");
        logger.info("instance URL: "+loginInstanceUrl);
        logger.info("access token/session ID: "+loginAccessToken);
        logger.info("baseUri: "+ baseUri);

        return exitValue;
    }

    // Query Leads using REST HttpGet
    int queryLeads() {
        logger.info("\n_______________ Lead QUERY _______________");

        try {
            // Setup the HTTP objects needed to make the request
            HttpClient httpClient = HttpClientBuilder.create().build();

            // The base limit for this query is five and sorted in ascending order by Id
            String uri = baseUri + "/query?q=Select+Id+,+FirstName+,+LastName+,+Company+From+Lead+Limit+5";
            logger.info("Query URL: " + uri);
            HttpGet httpGet = new HttpGet(uri);
            logger.info("oauthHeader2: " + oauthHeader);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            // Now make the GET request
            HttpResponse httpResponse = httpClient.execute(httpGet);

            // Process the result
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(httpResponse.getEntity());
                try {
                    JSONObject jsonObject = new JSONObject(response_string);
                    logger.info("JSON result of Query:\n" + jsonObject.toString(1));
                    JSONArray jsonArray = jsonObject.getJSONArray("records");
                    for (int i = 0; i < jsonArray.length(); i ++){
                        leadId = jsonObject.getJSONArray("records").getJSONObject(i).getString("Id");
                        leadFirstName = jsonObject.getJSONArray("records").getJSONObject(i).getString("FirstName");
                        leadLastName = jsonObject.getJSONArray("records").getJSONObject(i).getString("LastName");
                        leadCompany = jsonObject.getJSONArray("records").getJSONObject(i).getString("Company");
                        logger.info("Lead record is: " + i + ". " + leadId + " " + leadFirstName + " " + leadLastName + "(" + leadCompany + ")");
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                logger.info("Query was unsuccessful. Status code returned is " + statusCode);
                logger.info("An error has occurred. Http status: " + httpResponse.getStatusLine().getStatusCode());
                logger.info(getBody(httpResponse.getEntity().getContent()));
                return exitValue = -1;
            }
        } catch (IOException | NullPointerException ioe) {
            ioe.printStackTrace();
        }

        return exitValue;
    }

    private static String getBody(InputStream inputStream) {
        String result = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                result += inputLine;
                result += "\n";
            }
            bufferedReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }
}