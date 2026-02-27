package com.auticare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebDashboardController {

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/courses")
    public String courses() {
        return "courses";
    }

    @GetMapping("/evaluations")
    public String evaluations() {
        return "evaluations";
    }

    @GetMapping("/consultations")
    public String consultations() {
        return "consultations";
    }

    @GetMapping("/events")
    public String events() {
        return "events";
    }

    @GetMapping("/complaints")
    public String complaints() {
        return "complaints";
    }
}
