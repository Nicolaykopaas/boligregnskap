package no.nicolay.boligregnskap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class BoligregnskapApplication {

    @Value("${server.port:8080}")
    private String serverPort;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(BoligregnskapApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("http://localhost:" + serverPort));
                System.out.println("Napnet nettleser mot http://localhost:" + serverPort);
            } catch (Exception e) {
                System.out.println("Kunne ikke apne nettleser automatisk: " + e.getMessage());
            }
        } else {
            System.out.println("Automatisk nettleser-apning stottes ikke i dette miljoet (isDesktopSupported=" + Desktop.isDesktopSupported() + ")");
        }
    }
}
