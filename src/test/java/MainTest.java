import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.InputStream;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;

public class MainTest {
    private static String testUserName;
    private static String testPassword;
    private static String testLoginUrl;
    private static String testGrantService;
    private static String testConsumerKey;
    private static String testConsumerSecret;

    private InputStream inputStream = null;
    private Resource resource = null;
    private ProtocolBuilder protocolBuilder;

    // TODO: This is a fairly clunky way to test and there is only one negative test but it's good enough for now

    @Before
    public void setUp() throws Exception {
        inputStream = Main.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();

        properties.load(inputStream);
        testUserName = properties.getProperty("TESTUSERNAME");
        testPassword = properties.getProperty("TESTPASSWORD");
        testLoginUrl = properties.getProperty("TESTLOGINURL");
        testGrantService = properties.getProperty("TESTGRANTSERVICE");
        testConsumerKey = properties.getProperty("TESTCONSUMERKEY");
        testConsumerSecret = properties.getProperty("TESTCONSUMERSECRET");

        resource = new Resource();
        protocolBuilder = new ProtocolBuilder(testLoginUrl, testGrantService, testConsumerKey, testConsumerSecret, testUserName, testPassword);
    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void mainWithSystemExitZero() throws Exception {
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> Assert.assertTrue(true));

        String[] testArgs = {};
        Main.main(testArgs);
    }

    @Test
    public void mainWithSystemExitNegativeOne() throws Exception {
        exit.expectSystemExitWithStatus(-1);
        exit.checkAssertionAfterwards(() -> Assert.assertTrue(true));

        // Use the strings declared above with a bad password for this test
        Main.main(new String[]{testLoginUrl, testGrantService, testConsumerKey, testConsumerSecret, testUserName, testPassword.toUpperCase()});
    }

    // TODO: Break this test into individual resource tests
    @Test
    public void successfulResourceTest () throws Exception {
        Assert.assertThat(resource.login(protocolBuilder), is(0));

        Assert.assertThat(resource.queryLeads(), is(0));
        Assert.assertThat(resource.createNewLead(), is(0));
        Assert.assertThat(resource.updateLead(), is(0));
        Assert.assertThat(resource.deleteLead(), is(0));
        Assert.assertThat(resource.listAvailableResources(), is(0));

        Assert.assertThat(resource.logout(), is(0));
    }

    @After
    public void tearDown() throws Exception {
        inputStream.close();
    }
}