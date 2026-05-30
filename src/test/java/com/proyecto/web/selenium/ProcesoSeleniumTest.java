package com.proyecto.web.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.support.ui.Select;
import org.junit.jupiter.api.Tag;

@Tag("selenium")
class ProcesoSeleniumTest extends BaseSeleniumTest {

    @BeforeEach
    void hacerLogin() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement correo = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email']")
            )
        );
        correo.sendKeys("selenium@test.com");
        driver.findElement(By.cssSelector("input[type='password']"))
              .sendKeys("qwerty");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/procesos"));
    }

    @Test
    void listarProcesos_paginaCargaCorrectamente() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Verificar que existe el input de búsqueda
        WebElement busqueda = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".input-busqueda")
            )
        );
        assertTrue(busqueda.isDisplayed());
    }

    @Test
    void crearProceso_flujoCompleto() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

        // Clic en botón Nuevo proceso
        WebElement btnNuevo = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-primary"))
        );
        js.executeScript("arguments[0].click();", btnNuevo);

        // Esperar que cargue el editor
        wait.until(ExpectedConditions.urlContains("/procesos/nuevo"));

        // 1. LLENAR DATOS GENERALES
        WebElement nombre = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='proceso-nombre']"))
        );
        escribirCampo(nombre, "Proceso Selenium Test", js);

        escribirCampo(
            driver.findElement(By.cssSelector("[data-testid='proceso-descripcion']")),
            "Creado por Selenium",
            js
        );

        seleccionarPorTexto(driver.findElement(By.cssSelector("[data-testid='proceso-categoria']")), "Comercial", js);

        seleccionarPorTexto(driver.findElement(By.cssSelector("[data-testid='proceso-estado']")), "PUBLICADO", js);

        // Seleccionar Pool
        WebElement poolSelectElement = driver.findElement(By.cssSelector("[data-testid='proceso-pool']"));
        seleccionarPorIndice(poolSelectElement, 1, js);

        wait.until(d -> {
            String val = poolSelectElement.getAttribute("value");
            return val != null && !val.isEmpty() && !val.contains("null");
        });

        WebElement btnAgregarActividad = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='agregar-actividad']"))
        );

        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", btnAgregarActividad);
        btnAgregarActividad = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='agregar-actividad']"))
        );

        agregarActividad(btnAgregarActividad, js, 1);
        
        WebElement primeraCard = esperarCardActividad(wait, 0);

        WebElement actNombre0 = primeraCard.findElement(By.cssSelector("[data-testid='actividad-nombre-0']"));
        escribirCampo(actNombre0, "Primera Actividad", js);
        
        escribirCampo(
            primeraCard.findElement(By.cssSelector("[data-testid='actividad-desc-0']")),
            "Descripción de la primera actividad",
            js
        );
        
        seleccionarPorIndice(primeraCard.findElement(By.cssSelector("[data-testid='actividad-lane-0']")), 1, js);

        btnAgregarActividad = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='agregar-actividad']"))
        );
        agregarActividad(btnAgregarActividad, js, 2);
        
        WebElement segundaCard = esperarCardActividad(wait, 1);

        WebElement actNombre1 = segundaCard.findElement(By.cssSelector("[data-testid='actividad-nombre-1']"));
        escribirCampo(actNombre1, "Segunda Actividad", js);
        
        escribirCampo(
            segundaCard.findElement(By.cssSelector("[data-testid='actividad-desc-1']")),
            "Descripción de la segunda actividad",
            js
        );
        
        seleccionarPorIndice(segundaCard.findElement(By.cssSelector("[data-testid='actividad-lane-1']")), 1, js);

        WebElement btnSubmit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        js.executeScript("arguments[0].click();", btnSubmit);

        esperarRedireccionDetalle(wait);
        assertTrue(driver.getCurrentUrl().contains("/detalle"));
    }

    private void esperarRedireccionDetalle(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.urlContains("/detalle"));
        } catch (TimeoutException ex) {
            String body = driver.findElement(By.tagName("body")).getText();
            throw new AssertionError(
                "El proceso no redirigió al detalle. URL: " + driver.getCurrentUrl()
                    + ". Texto visible: " + body.substring(0, Math.min(body.length(), 1500)),
                ex
            );
        }
    }

    private WebElement esperarCardActividad(WebDriverWait wait, int indice) {
        By card = By.cssSelector("[data-testid='actividad-card-" + indice + "']");
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(card));
        } catch (TimeoutException ex) {
            String body = driver.findElement(By.tagName("body")).getText();
            throw new AssertionError(
                "No apareció la actividad " + indice + ". URL: " + driver.getCurrentUrl()
                    + ". Texto visible: " + body.substring(0, Math.min(body.length(), 1200)),
                ex
            );
        }
    }

    private void clickUsuario(WebElement elemento) {
        new Actions(driver)
            .moveToElement(elemento)
            .pause(Duration.ofMillis(100))
            .click()
            .perform();
    }

    private void agregarActividad(WebElement boton, org.openqa.selenium.JavascriptExecutor js, int totalEsperado) {
        clickUsuario(boton);
        if (driver.findElements(By.cssSelector("[data-testid^='actividad-card-']")).size() >= totalEsperado) {
            return;
        }
        js.executeScript(
            "arguments[0].dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));",
            boton
        );
    }

    private void escribirCampo(WebElement campo, String valor, org.openqa.selenium.JavascriptExecutor js) {
        js.executeScript(
            "const el = arguments[0];"
                + "const value = arguments[1];"
                + "const proto = el.tagName.toLowerCase() === 'textarea' ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype;"
                + "Object.getOwnPropertyDescriptor(proto, 'value').set.call(el, value);"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            campo,
            valor
        );
    }

    private void seleccionarPorTexto(WebElement selectElement, String texto, org.openqa.selenium.JavascriptExecutor js) {
        new Select(selectElement).selectByVisibleText(texto);
        dispararCambio(selectElement, js);
    }

    private void seleccionarPorIndice(WebElement selectElement, int indice, org.openqa.selenium.JavascriptExecutor js) {
        new Select(selectElement).selectByIndex(indice);
        dispararCambio(selectElement, js);
    }

    private void dispararCambio(WebElement elemento, org.openqa.selenium.JavascriptExecutor js) {
        js.executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", elemento);
    }
}
