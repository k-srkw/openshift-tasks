package sample;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = { "pretty", "html:target/cucumber-reports.html" }, monochrome = true, snippets = CucumberOptions.SnippetType.UNDERSCORE)
public class AcceptanceTest {

    @BeforeClass
    public static void beforeClass() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
}
