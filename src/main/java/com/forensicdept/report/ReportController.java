package com.forensicdept.report;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "PDF report generation (MLR and PMR)")
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    @GetMapping("/mlr/{mlefId}")
    @Operation(summary = "Generate and download Medico-Legal Report (MLR) PDF")
    public ResponseEntity<byte[]> downloadMlr(@PathVariable Long mlefId) {
        ByteArrayOutputStream bos = reportGenerationService.generateMlr(mlefId);
        return pdfResponse(bos, "MLR-" + mlefId + ".pdf");
    }

    @GetMapping("/pmr/{postmortemId}")
    @Operation(summary = "Generate and download Postmortem Report (PMR) PDF")
    public ResponseEntity<byte[]> downloadPmr(@PathVariable Long postmortemId) {
        ByteArrayOutputStream bos = reportGenerationService.generatePmr(postmortemId);
        return pdfResponse(bos, "PMR-" + postmortemId + ".pdf");
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Generate and download report by Case ID and type")
    public ResponseEntity<byte[]> downloadByCase(@PathVariable Long caseId, @RequestParam String type) {
        ByteArrayOutputStream bos = reportGenerationService.generateReportByCase(caseId, type);
        return pdfResponse(bos, type + "-Case-" + caseId + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(ByteArrayOutputStream bos, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bos.toByteArray());
    }
}
