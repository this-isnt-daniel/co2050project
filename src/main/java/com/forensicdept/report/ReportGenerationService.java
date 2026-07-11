package com.forensicdept.report;

import java.io.ByteArrayOutputStream;

/**
 * Service interface for generating medico-legal PDF reports.
 * Full template fidelity to departmental forms is a follow-up item.
 * MVP: structured PDF with all case data.
 */
public interface ReportGenerationService {

    /**
     * Generates a Medico-Legal Report (MLR) PDF from MLEF data.
     *
     * @param mlefId MLEF record id
     * @return PDF bytes
     */
    ByteArrayOutputStream generateMlr(Long mlefId);

    /**
     * Generates a Postmortem Report (PMR) PDF from postmortem data.
     *
     * @param postmortemId postmortem record id
     * @return PDF bytes
     */
    ByteArrayOutputStream generatePmr(Long postmortemId);
}
