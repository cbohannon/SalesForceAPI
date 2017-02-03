import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class MainTest {
    @Before
    public void setUp() throws Exception {

    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void mainWithSystemExitZero() throws Exception {
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> Assert.assertTrue(true));

        Main.main(null);
    }

    @Test
    @Ignore
    public void mainWithSystemExitNegativeOne() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }
}