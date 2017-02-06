import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private static String userName;
    private static String password;
    private static String loginUrl;
    private static String grantService;
    private static String consumerKey;
    private static String consumerSecret;

    public static void main(String args[]) throws IOException {
        ProtocolBuilder protocolBuilder;

        if (args.length > 0) {
            protocolBuilder = new ProtocolBuilder(args[0], args[1], args[2], args[3], args[4], args[5]);
        } else {
            getProperties();
            protocolBuilder = new ProtocolBuilder(loginUrl, grantService, consumerKey, consumerSecret, userName, password);
        }

        Resource resource = new Resource();
        resource.Login(protocolBuilder);

        if (resource.exitValue == 0) {
            // Query (REST GET) some leads
            resource.queryLeads();
        }

        // Release the connection
        protocolBuilder.httpPost.releaseConnection();

        System.exit(resource.exitValue);
    }

    private static void getProperties() throws IOException {
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();

        try {
            properties.load(inputStream);
            userName = properties.getProperty("USERNAME");
            password = properties.getProperty("PASSWORD");
            loginUrl = properties.getProperty("LOGINURL");
            grantService = properties.getProperty("GRANTSERVICE");
            consumerKey = properties.getProperty("CONSUMERKEY");
            consumerSecret = properties.getProperty("CONSUMERSECRET");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}