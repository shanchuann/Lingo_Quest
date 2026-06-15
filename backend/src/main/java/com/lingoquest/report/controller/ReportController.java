package com.lingoquest.report.controller;

import com.lingoquest.common.api.R;
import com.lingoquest.report.dto.WeeklyReport;
import com.lingoquest.report.service.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final WeeklyReportService weeklyReportService;

    @Operation(summary = "Get the AI-powered weekly learning report")
    @GetMapping("/weekly")
    public R<WeeklyReport> weekly() {
        return R.ok(weeklyReportService.generate());
    }
}
