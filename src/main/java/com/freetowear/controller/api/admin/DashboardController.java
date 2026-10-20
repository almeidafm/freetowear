package com.freetowear.controller.api.admin;

import com.freetowear.dto.response.dashboard.DashboardResponse;
import com.freetowear.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/dashboard", "/api/admin/dashboard"})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<DashboardResponse> getMetrics(
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics(days));
    }
}