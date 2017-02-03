public class Main {
    private static final String USERNAME = "yourusername";
    private static final String PASSWORD = "yourpassword";
    private static final String LOGINURL = "https://login.salesforce.com";
    private static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
    private static final String CONSUMERKEY = "yourconsumerkey";
    private static final String CONSUMERSECRET = "yourconsumersecret";

    public static void main(String args[]) {
        ProtocolBuilder protocolBuilder = new ProtocolBuilder(LOGINURL, GRANTSERVICE, CONSUMERKEY,
                                                              CONSUMERSECRET, USERNAME, PASSWORD);

        Resource resource = new Resource();
        resource.Login(protocolBuilder);

        System.exit(resource.exitValue);
    }
}