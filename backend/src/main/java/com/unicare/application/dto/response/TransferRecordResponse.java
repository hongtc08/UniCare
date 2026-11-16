package com.unicare.application.dto.response;

import com.unicare.domain.entity.TransferRecord;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Thông tin hồ sơ chuyển viện gắn với phiếu khám bệnh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRecordResponse {

    private Long id;
    private String recordCode;
    private Long checkupId;
    private String targetHospital;
    private String reasonCategory;
    private String transferReason;
    private String currentCondition;
    private String firstAidNotes;
    private String refusalNotes;
    private OffsetDateTime transferredAt;

    public static TransferRecordResponse fromEntity(TransferRecord entity) {
        if (entity == null) {
            return null;
        }
        return TransferRecordResponse.builder()
                .id(entity.getId())
                .recordCode(entity.getRecordCode())
                .checkupId(entity.getMedicalCheckup() != null ? entity.getMedicalCheckup().getId() : null)
                .targetHospital(entity.getTargetHospital())
                .reasonCategory(entity.getReasonCategory())
                .transferReason(entity.getTransferReason())
                .currentCondition(entity.getCurrentCondition())
                .firstAidNotes(entity.getFirstAidNotes())
                .refusalNotes(entity.getRefusalNotes())
                .transferredAt(entity.getTransferredAt())
                .build();
    }
}
