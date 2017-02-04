import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.InputStream;
import java.util.Properties;

public class MainTest {
    private static String testUserName;
    private static String testPassword;
    private static String testLoginUrl;
    private static String testGrantService;
    private static String testConsumerKey;
    private static String testConsumerSecret;

    private InputStream inputStream = null;

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

        Main.main(new String[]{testLoginUrl, testGrantService, testConsumerKey, testConsumerSecret, testUserName, testPassword});
    }

    @After
    public void tearDown() throws Exception {
        inputStream.close();
    }
}