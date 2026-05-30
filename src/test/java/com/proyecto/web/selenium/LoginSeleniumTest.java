package com.proyecto.web.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Tag;

@Tag("selenium")
class LoginSeleniumTest extends BaseSeleniumTest {

    @Test
    void loginExitoso_redirigeProcesos() {
        driver.get(BASE_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Llenar correo
        WebElement campoCorreo = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email']")
            )
        );
        campoCorreo.sendKeys("selenium@test.com");

        // Llenar contraseña
        driver.findElement(By.cssSelector("input[type='password']"))
              .sendKeys("qwerty");

        // Clic en botón Entrar
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Esperar que cargue la lista de procesos
        wait.until(ExpectedConditions.urlContains("/procesos"));

        assertTrue(driver.getCurrentUrl().contains("/procesos"),
            "Debería redirigir a /procesos después del login");
    }

    @Test
    void loginFallido_muestraError() {
        driver.get(BASE_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement campoCorreo = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email']")
            )
        );
        campoCorreo.sendKeys("noexiste@test.com");
        driver.findElement(By.cssSelector("input[type='password']"))
              .sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verificar que aparece mensaje de error
        WebElement error = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error"))
        );

        assertNotNull(error.getText());
        assertFalse(error.getText().isBlank());
    }
}