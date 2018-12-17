import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

class Resource {
    int exitValue = 0;

    private static final String REST_ENDPOINT = "/services/data" ;
    private static final String API_VERSION = "/v32.0" ;

    private String baseUri;
    private String leadId ;
    private String loginAccessToken;
    private String loginInstanceUrl;

    private Header oauthHeader;
    private final Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");

    private final Logger logger = LoggerFactory.getLogger(Resource.class);

    int login(ProtocolBuilder protocolBuilder) throws IOException {
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
        logger.info("instance URL: " + loginInstanceUrl);
        logger.info("access token/session ID: " + loginAccessToken);
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
                        String leadFirstName = jsonObject.getJSONArray("records").getJSONObject(i).getString("FirstName");
                        String leadLastName = jsonObject.getJSONArray("records").getJSONObject(i).getString("LastName");
                        String leadCompany = jsonObject.getJSONArray("records").getJSONObject(i).getString("Company");
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

    // Create lead using REST HttpPost
    int createNewLead() {
        String uri = baseUri + "/sobjects/Lead/";

        try {
            // Create the JSON object containing the new lead details.
            JSONObject leadJsonObject = new JSONObject();
            leadJsonObject.put("FirstName", "Test First");
            leadJsonObject.put("LastName", "Test Last");
            leadJsonObject.put("Company", "Test Company");
            leadJsonObject.put("Email", "test@test.com");
            leadJsonObject.put("State", "TX");

            logger.info("JSON for lead record to be inserted:\n" + leadJsonObject.toString(1));

            HttpResponse httpResponse = createOrUpdate(true, uri, leadJsonObject);

            // Process the results
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 201) {
                String responseString = EntityUtils.toString(httpResponse.getEntity());
                JSONObject jsonResponseString = new JSONObject(responseString);
                // Store the retrieved lead id to use when we update the lead
                leadId = jsonResponseString.getString("id");
                logger.info("New Lead id from response: " + leadId);
            } else {
                logger.info("Insertion unsuccessful. Status code returned is " + statusCode);
                return exitValue = -1;
            }
        } catch (JSONException e) {
            logger.info("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException | NullPointerException ioe) {
            ioe.printStackTrace();
        }

        return exitValue;
    }

    // Update lead using REST httpPatch
    int updateLead() {
        logger.info("\n_______________ Lead UPDATE _______________");

        // The id for the record to update is part of the URI and not part of the JSON
        String uri = baseUri + "/sobjects/Lead/" + leadId;
        try {
            // Create the JSON object containing the updated lead last name and the id of the lead we are updating.
            JSONObject leadJsonObject = new JSONObject();
            leadJsonObject.put("LastName", "Lead --UPDATED");
            logger.info("JSON for update of lead record:\n" + leadJsonObject.toString(1));

            HttpResponse httpResponse = createOrUpdate(false, uri, leadJsonObject);

            // Process the response
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                logger.info("Updated the lead successfully.");
            } else {
                logger.info("Lead update NOT successfully. Status code is " + statusCode);
                return exitValue = -1;
            }
        } catch (JSONException e) {
            logger.info("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (NullPointerException | IOException ioe) {
            ioe.printStackTrace();
        }

        return exitValue;
    }

    // Delete lead using REST httpDelete
    int deleteLead() {
        logger.info("\n_______________ Lead DELETE _______________");

        // The id for the record to update is part of the URI and not part of the JSON
        String uri = baseUri + "/sobjects/Lead/" + leadId;
        try {
            // Set up the objects necessary to make the request.
            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpDelete httpDelete = new HttpDelete(uri);
            httpDelete.addHeader(oauthHeader);
            httpDelete.addHeader(prettyPrintHeader);

            // Make the request
            HttpResponse httpResponse = httpClient.execute(httpDelete);

            // Process the response
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                logger.info("Deleted the lead successfully.");
            } else {
                logger.info("Lead delete NOT successful. Status code is " + statusCode);
                return exitValue = -1;
            }
        } catch (JSONException e) {
            logger.info("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException | NullPointerException ioe) {
            ioe.printStackTrace();
        }

        return exitValue;
    }

    int listAvailableResources() {
        logger.info("\n_______________ Available REST Resources _______________");

        // String uri = baseUri;
        // String uri = "https://na75.salesforce.com/services/data/v37.0/sobjects";
        String uri = "https://na75.salesforce.com/services/data/v37.0/sobjects/Account/0011K0000220GrrQAE";

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            // Now make the GET request
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(httpResponse.getEntity());
                try {
                    JSONObject jsonObject = new JSONObject(response_string);
                    logger.info("JSON result of available resources query:\n" + jsonObject.toString(1));
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                logger.info("Listing available resources was unsuccessful. Status code returned is " + statusCode);
                logger.info("An error has occurred. Http status: " + httpResponse.getStatusLine().getStatusCode());
                logger.info(getBody(httpResponse.getEntity().getContent()));
                return exitValue = -1;
            }
        } catch (JSONException e) {
            logger.info("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException | NullPointerException ioe) {
            ioe.printStackTrace();
        }

        return exitValue;
    }

    int logout() throws IOException {
        // Setup the HTTP objects needed to make the request
        HttpClient httpClient = HttpClientBuilder.create().build();

        String uri = "https://login.salesforce.com/services/oauth2/revoke?token=" + loginAccessToken;
        HttpGet httpGet = new HttpGet(uri);

        // Make the request
        HttpResponse httpResponse = httpClient.execute(httpGet);

        // Process the response
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            logger.info("Logout has been successful.");
        } else {
            logger.info("Logout was NOT successful. Status code is " + statusCode);
            return exitValue = -1;
        }

        return exitValue;
    }

    private String getBody(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                result.append(inputLine);
                result.append("\n");
            }
            bufferedReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result.toString();
    }

    private HttpResponse createOrUpdate(boolean isPost, String uri, JSONObject leadJsonObject) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse httpResponse;

        StringEntity body = null;
        try {
            body = new StringEntity(leadJsonObject.toString(1));
            body.setContentType("application/json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (isPost) {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader(oauthHeader);
            httpPost.addHeader(prettyPrintHeader);
            httpPost.setEntity(body);
            httpResponse = httpClient.execute(httpPost);
        } else {
            HttpPatch httpPatch = new HttpPatch(uri);
            httpPatch.addHeader(oauthHeader);
            httpPatch.addHeader(prettyPrintHeader);
            httpPatch.setEntity(body);
            httpResponse = httpClient.execute(httpPatch);
        }

        return httpResponse;
    }
}