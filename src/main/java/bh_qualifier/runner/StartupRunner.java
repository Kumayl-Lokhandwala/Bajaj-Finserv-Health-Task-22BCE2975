package bh_qualifier.runner;

import bh_qualifier.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final WebhookService webhookService;

    @Autowired
    public StartupRunner(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public void run(String... args) {
        log.info("StartupRunner executing - beginning webhook flow");
        try {
            webhookService.startProcess();
            log.info("StartupRunner completed webhook flow");
        } catch (Exception ex) {
            log.error("Unhandled exception during startup webhook flow", ex);
        }
    }
}
