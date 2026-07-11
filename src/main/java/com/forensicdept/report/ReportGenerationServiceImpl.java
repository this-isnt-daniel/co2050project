package com.forensicdept.report;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.mlef.entity.MlefEntity;
import com.forensicdept.mlef.repository.MlefRepository;
import com.forensicdept.postmortem.entity.PostmortemEntity;
import com.forensicdept.postmortem.repository.PostmortemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * MVP PDF report implementation using OpenPDF.
 *
 * <p><strong>Follow-up items</strong> (noted for viva):
 * <ul>
 *   <li>Template fidelity to the department's official MLEF / PMR forms</li>
 *   <li>Department letterhead and official stamps</li>
 *   <li>Digital signature blocks</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private final MlefRepository mlefRepository;
    private final PostmortemRepository postmortemRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generateMlr(Long mlefId) {
        MlefEntity mlef = mlefRepository.findById(mlefId)
                .orElseThrow(() -> new ResourceNotFoundException("MLEF", mlefId));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        try {
            PdfWriter.getInstance(doc, bos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            doc.add(new Paragraph("MEDICO-LEGAL REPORT (MLR)", titleFont));
            doc.add(new Paragraph("Forensic Medicine Department", bodyFont));
            doc.add(Chunk.NEWLINE);

            doc.add(sectionParagraph("Case Number", mlef.getCaseRef().getCaseNumber(), headFont, bodyFont));
            doc.add(sectionParagraph("Patient", mlef.getCaseRef().getPatient() != null
                    ? mlef.getCaseRef().getPatient().getFullName() : "N/A", headFont, bodyFont));
            doc.add(sectionParagraph("Examining Doctor", mlef.getExaminingDoctor().getName(), headFont, bodyFont));
            doc.add(sectionParagraph("Date of Issue",
                    mlef.getDateOfIssue() != null ? mlef.getDateOfIssue().format(DATE_FMT) : "Pending", headFont, bodyFont));
            doc.add(sectionParagraph("Examination Date & Time",
                    mlef.getExaminationDateTime() != null ? mlef.getExaminationDateTime().format(DATETIME_FMT) : "Pending", headFont, bodyFont));
            doc.add(Chunk.NEWLINE);

            doc.add(section("Nature of Bodily Harm", mlef.getNatureOfBodilyHarm(), headFont, bodyFont));
            doc.add(section("Causative Weapon", mlef.getCausativeWeapon(), headFont, bodyFont));
            doc.add(section("Alcohol / Drug Test Results", mlef.getAlcoholDrugTestResults(), headFont, bodyFont));
            doc.add(section("Clinical Findings", mlef.getFindings(), headFont, bodyFont));

            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Report Status: " + mlef.getReportStatus(), headFont));

        } catch (Exception e) {
            log.error("Failed to generate MLR for MLEF {}: {}", mlefId, e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }
        return bos;
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePmr(Long postmortemId) {
        PostmortemEntity pm = postmortemRepository.findById(postmortemId)
                .orElseThrow(() -> new ResourceNotFoundException("Postmortem", postmortemId));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        try {
            PdfWriter.getInstance(doc, bos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            doc.add(new Paragraph("POSTMORTEM REPORT (PMR)", titleFont));
            doc.add(new Paragraph("Forensic Medicine Department", bodyFont));
            doc.add(Chunk.NEWLINE);

            doc.add(sectionParagraph("Case Number", pm.getCaseRef().getCaseNumber(), headFont, bodyFont));
            doc.add(sectionParagraph("Deceased", pm.getCaseRef().getPatient() != null
                    ? pm.getCaseRef().getPatient().getFullName() : "Unidentified", headFont, bodyFont));
            doc.add(sectionParagraph("Pathologist", pm.getDoctor().getName(), headFont, bodyFont));
            doc.add(sectionParagraph("Inquest Order Ref", pm.getInquestOrderRef(), headFont, bodyFont));
            doc.add(sectionParagraph("Inquest Date",
                    pm.getInquestDate() != null ? pm.getInquestDate().format(DATE_FMT) : "Pending", headFont, bodyFont));
            doc.add(sectionParagraph("Place of Postmortem", pm.getPlaceOfPm(), headFont, bodyFont));
            doc.add(sectionParagraph("Manner of Death", pm.getCauseOfDeathCategory(), headFont, bodyFont));
            doc.add(Chunk.NEWLINE);

            doc.add(section("Postmortem Findings", pm.getFindings(), headFont, bodyFont));
            doc.add(section("Cause of Death", pm.getCauseOfDeath(), headFont, bodyFont));

        } catch (Exception e) {
            log.error("Failed to generate PMR for Postmortem {}: {}", postmortemId, e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }
        return bos;
    }

    private Paragraph sectionParagraph(String label, String value, Font headFont, Font bodyFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", headFont));
        p.add(new Chunk(value != null ? value : "—", bodyFont));
        return p;
    }

    private Paragraph section(String heading, String content, Font headFont, Font bodyFont) {
        Paragraph p = new Paragraph();
        p.add(new Paragraph(heading, headFont));
        p.add(new Paragraph(content != null ? content : "Not recorded.", bodyFont));
        p.setSpacingAfter(10);
        return p;
    }
}
