package com.forensicdept.common.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for {@link DocumentSerialSequenceEntity}.
 */
public class DocumentSerialSequenceId implements Serializable {

    private String docType;
    private Integer year;

    public DocumentSerialSequenceId() {}

    public DocumentSerialSequenceId(String docType, Integer year) {
        this.docType = docType;
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentSerialSequenceId that)) return false;
        return Objects.equals(docType, that.docType) && Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docType, year);
    }
}
