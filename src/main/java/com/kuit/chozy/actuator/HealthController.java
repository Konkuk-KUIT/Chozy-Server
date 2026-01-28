package com.kuit.chozy.actuator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Chozy server is running";
    }

    @GetMapping("/fast")
    public String fast() {
        return "fast";
    }

    @GetMapping("/slow")
    public String slow() throws InterruptedException {
        Thread.sleep(1200); // 1.2초
        return "slow";
    }

}
