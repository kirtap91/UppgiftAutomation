package stepDefinitions;

// ----- Importera nödvändiga bibliotek -----
// Cucumber-hooks för Before/After och steg-annotationer
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

// JUnit Assertions för att jämföra förväntat och faktiskt resultat
import org.junit.jupiter.api.Assertions;

// Selenium-klasser för att interagera med webbläsaren
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class stepDefinitions {

    private WebDriver driver;

    // ----- setUp(): Ställ in webbläsaren före varje Scenario -----
    @Before
    public void setUp() {
        // Vi kan välja mellan Chrome och Firefox via system-property "browser"
        String browser = System.getProperty("browser", "chrome");
        if (browser.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
        } else {
            driver = new ChromeDriver();
        }
        // Vid det här laget är 'driver' en öppen webbläsar-instans
    }

    // ----- Givet: Navigera till registreringssidan -----
    @Given("I am on the registration page")
    public void openRegistrationPage() {
        driver.get("https://membership.basketballengland.co.uk/NewSupporterAccount");
        // Webbläsaren öppnar nu exakt den URL där registreringsformuläret finns
    }

    // ----- När: Fyll i alla obligatoriska fält korrekt -----
    @When("I fill in all mandatory fields correctly")
    public void fillAllFieldsCorrectly() {
        // 1) Skriv in "Anna" i förnamnsfältet
        driver.findElement(By.id("member_firstname")).sendKeys("Anna");
        // 2) Skriv in "Svensson" i efternamnsfältet
        driver.findElement(By.id("member_lastname")).sendKeys("Svensson");
        // 3) Skriv in lösenordet två gånger
        driver.findElement(By.id("signupunlicenced_password")).sendKeys("Password123");
        driver.findElement(By.id("signupunlicenced_confirmpassword")).sendKeys("Password123");

        // 4) Välj födelsedatum: Eftersom inputen använder en jQuery-baserad datumväljare
        WebElement dob = driver.findElement(By.id("dp"));
        // Vi sätter värdet direkt via JavaScript och triggar jQuery change för att formvalideringen ska upptäcka det
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = '01/01/1990'; " +
                        "$(arguments[0]).trigger('change');",
                dob
        );

        // 5) Skriv in e-postadressen två gånger
        driver.findElement(By.id("member_emailaddress")).sendKeys("testinnnng@test.com");
        driver.findElement(By.id("member_confirmemailaddress")).sendKeys("testinnnng@test.com");

        // 6) Klicka i checkboxen för Terms & Conditions (dold <input> + synlig <label>)
        WebElement checkbox = driver.findElement(By.cssSelector("label[for='sign_up_25']"));
        checkbox.click();

        // 7) Klicka i checkboxen för åldersbekräftelse (>18 år)
        WebElement ageCheckbox = driver.findElement(By.cssSelector("label[for='sign_up_26']"));
        ageCheckbox.click();

        // 8) Klicka i checkboxen för Code of Ethics & Conduct
        WebElement cocCheckbox = driver.findElement(By.cssSelector("label[for='fanmembersignup_agreetocodeofethicsandconduct']"));
        cocCheckbox.click();
    }

    // ----- När: Fyll i alla fält utom efternamn (sparar för missad efternamn-scenario) -----
    @When("I fill in all fields except last name")
    public void fillAllExceptLastName() throws InterruptedException {
        // Vi återanvänder metoden ovan för att fylla i alla fält
        fillAllFieldsCorrectly();

        // Sedan tömmer vi ut efternamnsfältet så att det blir tomt
        WebElement lastNameInput = driver.findElement(By.id("member_lastname"));
        lastNameInput.clear();
    }

    // ----- När: Fyll i lösenord och bekräftelse med olika värden (för password mismatch-scenario) -----
    @When("I fill in the password and confirmation with different values")
    public void fillPasswordsDifferently() {
        // Vi vill ändå ha alla övriga fält ifyllda korrekt
        fillAllFieldsCorrectly();

        // Töm och skriv ett felaktigt bekräftelselösenord
        WebElement confirmPwd = driver.findElement(By.id("signupunlicenced_confirmpassword"));
        confirmPwd.clear();
        confirmPwd.sendKeys("WrongPassword");
    }

    // ── När: Stöd för Scenario Outline (parametriserat steg) ──
    @When("I fill in {string} with {string}")
    public void fillInFieldWithValue(String field, String value) {
        // Börja alltid med att fylla i alla fält korrekt
        fillAllFieldsCorrectly();

        // Om vi vill testa "last name" saknas -> töm efternamnsfältet
        if (field.equalsIgnoreCase("last name")) {
            WebElement lastNameInput = driver.findElement(By.id("member_lastname"));
            lastNameInput.clear();
        }
        // Om det handlar om "confirmPassword" -> sätt det här värdet
        else if (field.equalsIgnoreCase("confirmPassword")) {
            WebElement cp = driver.findElement(By.id("signupunlicenced_confirmpassword"));
            cp.clear();
            cp.sendKeys(value);
        }
        // Alla andra värden är ogiltiga i vårt Scenario Outline
        else {
            throw new IllegalArgumentException("Ogiltigt fält: " + field);
        }
    }
    // ────────────────────────────────────────────────────────────────────────

    // ----- När: Klicka i Terms & Conditions -----
    @When("I accept the terms and conditions")
    public void acceptTerms() {
        // Hitta det faktiska <input>-elementet (checkboxen), som kan vara dolt
        WebElement termsInput = driver.findElement(By.id("sign_up_25"));
        // Om det inte redan är markerat, klicka på motsvarande <label> för att toggla
        if (!termsInput.isSelected()) {
            driver.findElement(By.cssSelector("label[for='sign_up_25']")).click();
        }
        // Säkerställ att checkboxen faktiskt är markerad efter klicket
        Assertions.assertTrue(termsInput.isSelected(),
                "Terms & Conditions should now be selected");
    }

    // ----- När: Lämna Terms & Conditions avmarkerat (för "terms not accepted"-scenario) -----
    @When("I do not accept the terms and conditions")
    public void doNotAcceptTerms() {
        WebElement termsInput = driver.findElement(By.id("sign_up_25"));
        // Om checkboxen är markerad, klicka på label för att avmarkera
        if (termsInput.isSelected()) {
            driver.findElement(By.cssSelector("label[for='sign_up_25']")).click();
        }
        // Säkerställ att den nu är avmarkerad
        Assertions.assertFalse(termsInput.isSelected(),
                "Terms & Conditions should now be unselected");
    }

    // ----- När: Klicka på "Confirm and Join"-knappen (Register) -----
    @When("I click Register")
    public void clickRegister() {
        // Hitta knappen via name="join" och klicka
        driver.findElement(By.name("join")).click();
    }

    // ----- Då: Verifiera att kontot skapats → "Go to My Locker" ska synas -----
    @Then("the account should be created and I should see a confirmation message")
    public void verifySuccessMessage() throws InterruptedException {
        // Vänta 5 sekunder bara för att vara säker på att sidan hinner laddas klart
        Thread.sleep(5000);

        // Nu väntar vi max 10 sekunder på att "GO TO MY LOCKER"-knappen blir klickbar
        WebElement lockerBtn = waitForElement(
                By.cssSelector("a.btn.red.margin-bottom-20"),
                5
                );

        // Jämför knapptexten med exakt förväntat värde
        Assertions.assertEquals("GO TO MY LOCKER", lockerBtn.getText().trim());
    }

    // ----- Då: Verifiera fel när efternamn saknas -----
    @Then("I should see an error message about missing last name")
    public void verifyLastNameError() {
        // Vänta upp till 5 sekunder på att felmeddelandets <span> visas
        WebElement lastNameError = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("span.field-validation-error[data-valmsg-for='Surname']")
                ));

        String errorText = lastNameError.getText().trim();
        Assertions.assertEquals("Last Name is required", errorText);
    }

    // ----- Då: Verifiera fel när lösenorden inte matchar -----
    @Then("I should see an error message about password mismatch")
    public void verifyPasswordMismatchError() {
        WebElement passwordError = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("span.field-validation-error[data-valmsg-for='ConfirmPassword']")
                ));

        String errorText = passwordError.getText().trim();
        Assertions.assertEquals("Password did not match", errorText);
    }

    // ----- Då: Verifiera fel när Terms & Conditions inte godkänts -----
    @Then("I should see an error message about accepting terms and conditions")
    public void verifyTermsError() {
        WebElement termsError = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("span.field-validation-error[data-valmsg-for='TermsAccept']")
                ));

        String errorText = termsError.getText().trim();
        Assertions.assertEquals("You must confirm that you have read and accepted our Terms and Conditions", errorText);
    }

    // ── Då: Generellt steg för Scenario Outline, validerar vilket fel som helst ──
    @Then("I should see error message {string}")
    public void i_should_see_error_message(String expectedError) {
        // Hitta första felmeddelande‐span och läs ut dess text
        WebElement errorSpan = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("span.field-validation-error")
                ));

        String actualText = errorSpan.getText().trim();
        // Jämför att den innehåller det förväntade felet (contains är mjukare än equals)
        Assertions.assertTrue(
                actualText.contains(expectedError),
                "Förväntade: \"" + expectedError + "\" men fick: \"" + actualText + "\""
        );
    }
    // ───────────────────────────────────────────────────────────────────────────

    // ----- @After: Stäng webbläsaren efter varje Scenario -----
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    //hjälpfunktion för att vänta på ett element -----
    private WebElement waitForElement(By locator, int timeoutSec) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
