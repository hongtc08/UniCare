import { useState, useEffect } from "react";
import { useParams, useSearchParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Lock,
  Clock,
  AlertCircle,
  Save,
  CheckCircle2,
  Ambulance,
  Printer,
  Building2,
} from "lucide-react";
import { medicalRecordApi, CHECKUP_TYPES } from "../../api/medicalRecordApi";
import Toast from "../../components/common/Toast";
import GeneralCheckupForm from "../../components/checkup/GeneralCheckupForm";
import PatientOverviewWrapper from "../../components/checkup/PatientOverviewWrapper";
import PrescriptionTable from "../../components/checkup/PrescriptionTable";
import CompleteCheckupModal from "../../components/checkup/CompleteCheckupModal";
import TransferRecordModal from "../../components/checkup/TransferRecordModal";
import TransferReferralPrintModal from "../../components/checkup/TransferReferralPrintModal";
import { createEmptyMedicine, validateAndCleanMedicines } from "../../utils/prescription";
import {
  validateCheckupForm,
  isPsychologicalCheckup,
  normalizeCheckupType,
} from "../../utils/checkupValidation";

export default function GeneralExaminationPage() {
  const { id: pathId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // Params từ URL
  const appointmentId = searchParams.get("appointmentId");
  const studentIdParam = searchParams.get("studentId");
  const isEmergencyParam =
    searchParams.get("isEmergency") === "true" ||
    searchParams.get("emergency") === "true";

  // State quản lý phiếu khám
  const [checkupId, setCheckupId] = useState(pathId ? Number(pathId) : null);
  const [checkupDetail, setCheckupDetail] = useState(null);
  const [appointmentData, setAppointmentData] = useState(null);
  const [studentOverview, setStudentOverview] = useState(null);
  const [diseaseGroups, setDiseaseGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [errors, setErrors] = useState({});
  const [prescriptionErrors, setPrescriptionErrors] = useState({});
  const [toast, setToast] = useState(null);

  // States chống bấm đúp & Modal
  const [isSavingDraft, setIsSavingDraft] = useState(false);
  const [isCompleting, setIsCompleting] = useState(false);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [transferModalOpen, setTransferModalOpen] = useState(false);
  const [isSubmittingTransfer, setIsSubmittingTransfer] = useState(false);
  const [transferApiError, setTransferApiError] = useState(null);
  const [printModalOpen, setPrintModalOpen] = useState(false);

  // Form state lâm sàng (Khớp với Create/UpdateMedicalCheckupRequest DTO)
  const [formData, setFormData] = useState({
    checkupType: CHECKUP_TYPES.GENERAL,
    isPeriodic: false,
    periodicResult: null,
    isEmergency: isEmergencyParam,
    diseaseGroupId: null,
    symptoms: "",
    treatmentSuggestions: "",
    diagnosisNotes: "",
  });

  // State danh sách đơn thuốc động
  const [medicines, setMedicines] = useState([]);

  // Trạng thái phiếu khám: DRAFT hoặc COMPLETED
  const [status, setStatus] = useState("DRAFT");
  const isReadOnly = status === "COMPLETED";

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // Cập nhật từng trường của Form khám lâm sàng
  const handleFormChange = (field, value) => {
    if (isReadOnly) return;
    setFormData((prev) => {
      if (field === "checkupType") {
        const normalizedType = normalizeCheckupType(value);
        if (normalizedType === CHECKUP_TYPES.PSYCHOLOGICAL) {
          return {
            ...prev,
            checkupType: CHECKUP_TYPES.PSYCHOLOGICAL,
            isPeriodic: false,
            periodicResult: null,
            diseaseGroupId: null,
          };
        }
        return {
          ...prev,
          checkupType: CHECKUP_TYPES.GENERAL,
        };
      }

      if (field === "isPeriodic") {
        const enabled = Boolean(value);
        return {
          ...prev,
          isPeriodic: enabled,
          periodicResult: enabled ? prev.periodicResult : null,
        };
      }

      return {
        ...prev,
        [field]: value,
      };
    });

    if (errors[field] || field === "checkupType" || field === "isPeriodic") {
      setErrors((prev) => {
        const next = { ...prev };
        delete next[field];
        if (field === "checkupType" || field === "isPeriodic") {
          delete next.periodicResult;
        }
        if (field === "checkupType" && normalizeCheckupType(value) === CHECKUP_TYPES.PSYCHOLOGICAL) {
          delete next.symptoms;
        }
        return next;
      });
    }

    if (field === "checkupType" && normalizeCheckupType(value) === CHECKUP_TYPES.PSYCHOLOGICAL) {
      setPrescriptionErrors({});
    }
  };

  // Cập nhật danh sách thuốc kê đơn động
  const handleMedicinesChange = (updatedMedicines) => {
    if (isReadOnly) return;
    setMedicines(updatedMedicines);

    if (Object.keys(prescriptionErrors).length > 0) {
      setPrescriptionErrors({});
    }
  };

  // Nạp dữ liệu ban đầu
  useEffect(() => {
    let active = true;

    async function initializeCheckup() {
      try {
        // 1. Tải danh mục nhóm bệnh dịch tễ
        try {
          const groups = await medicalRecordApi.getDiseaseGroups();
          if (active) {
            setDiseaseGroups(groups || []);
          }
        } catch (err) {
          console.warn("Không thể tải danh mục nhóm bệnh:", err);
        }

        // 2. Nạp dữ liệu phiếu khám nếu có ID trên path param (/doctor/checkups/:id)
        if (pathId) {
          const detail = await medicalRecordApi.getCheckupDetail(pathId);
          if (active && detail) {
            const normalizedType = normalizeCheckupType(detail.checkupType);
            const periodicFlag =
              normalizedType !== CHECKUP_TYPES.PSYCHOLOGICAL &&
              Boolean(detail.isPeriodic || detail.periodicResult);
            setCheckupId(detail.id);
            setCheckupDetail(detail);
            setStatus(detail.status || "DRAFT");
            setFormData({
              checkupType: normalizedType,
              isPeriodic: periodicFlag,
              periodicResult: periodicFlag ? detail.periodicResult || null : null,
              isEmergency: Boolean(detail.isEmergency),
              diseaseGroupId: detail.diseaseGroupId ? Number(detail.diseaseGroupId) : null,
              symptoms: detail.symptoms || "",
              treatmentSuggestions: detail.treatmentSuggestions || "",
              diagnosisNotes: detail.diagnosisNotes || "",
            });
            setMedicines(
              detail.prescribedMedicines && detail.prescribedMedicines.length > 0
                ? detail.prescribedMedicines.map((m) => createEmptyMedicine(m))
                : []
            );
          }
          return;
        }

        // 3. Nếu mở từ lịch hẹn (appointmentId), kiểm tra xem lịch hẹn đã có phiếu khám chưa
        if (appointmentId) {
          try {
            const existing = await medicalRecordApi.getCheckupByAppointment(appointmentId);
            if (active && existing) {
              const normalizedType = normalizeCheckupType(existing.checkupType);
              const periodicFlag =
                normalizedType !== CHECKUP_TYPES.PSYCHOLOGICAL &&
                Boolean(existing.isPeriodic || existing.periodicResult);
              setCheckupId(existing.id);
              setCheckupDetail(existing);
              setStatus(existing.status || "DRAFT");
              setFormData({
                checkupType: normalizedType,
                isPeriodic: periodicFlag,
                periodicResult: periodicFlag ? existing.periodicResult || null : null,
                isEmergency: Boolean(existing.isEmergency),
                diseaseGroupId: existing.diseaseGroupId ? Number(existing.diseaseGroupId) : null,
                symptoms: existing.symptoms || "",
                treatmentSuggestions: existing.treatmentSuggestions || "",
                diagnosisNotes: existing.diagnosisNotes || "",
              });
              setMedicines(
                existing.prescribedMedicines && existing.prescribedMedicines.length > 0
                  ? existing.prescribedMedicines.map((m) => createEmptyMedicine(m))
                  : []
              );
              return;
            }
          } catch (err) {
            // BẮT BUỘC: Xử lý catch lỗi êm nếu backend trả về 404 (chưa tạo phiếu cho lịch hẹn này)
            // Khởi tạo form trống, tuyệt đối không báo lỗi đỏ cho người dùng.
            const isNotFound =
              err?.response?.status === 404 ||
              err?.status === 404 ||
              String(err?.message || "").includes("Không tìm thấy");

            if (!isNotFound) {
              console.warn("Lỗi kiểm tra phiếu khám theo lịch hẹn:", err);
            }
          }

          // Tải thông tin lịch hẹn để điền trước thông tin sinh viên & triệu chứng khởi phát
          try {
            const appt = await medicalRecordApi.getAppointmentById(appointmentId);
            if (active && appt) {
              setAppointmentData(appt);
              const isPsyShift = appt.shiftType === "TU_VAN_TAM_LY";
              setFormData((prev) => ({
                ...prev,
                symptoms: prev.symptoms || appt.reasonSymptoms || "",
                checkupType: isPsyShift
                  ? CHECKUP_TYPES.PSYCHOLOGICAL
                  : CHECKUP_TYPES.GENERAL,
                isPeriodic: isPsyShift ? false : prev.isPeriodic,
                periodicResult: isPsyShift ? null : prev.periodicResult,
                isEmergency: false,
              }));
            }
          } catch (err) {
            console.warn("Không thể tải chi tiết lịch hẹn ban đầu:", err);
          }
        }

        // 4. Mặc định tạo mới hoàn toàn
        if (active) {
          setStatus("DRAFT");
          if (isEmergencyParam) {
            setFormData((prev) => ({ ...prev, isEmergency: true }));
          }
        }
      } catch (err) {
        if (!active) return;
        console.error("Lỗi nạp dữ liệu phiếu khám:", err);
        setError(
          err?.response?.data?.message ||
            err?.message ||
            "Không thể tải thông tin phiếu khám."
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    initializeCheckup();

    return () => {
      active = false;
    };
  }, [pathId, appointmentId, isEmergencyParam]);

  // Xác định studentId và thông tin sinh viên dự phòng
  const effectiveStudentId =
    checkupDetail?.studentId ||
    appointmentData?.studentId ||
    (studentIdParam ? Number(studentIdParam) : null);

  const fallbackStudentData = {
    studentName: checkupDetail?.studentName || appointmentData?.studentName || "",
    studentCode: checkupDetail?.studentCode || appointmentData?.studentCode || "",
    className: checkupDetail?.className || "",
    facultyName: checkupDetail?.facultyName || "",
    studentPhone: checkupDetail?.studentPhone || appointmentData?.studentPhone || "",
    studentEmail: checkupDetail?.studentEmail || appointmentData?.studentEmail || "",
  };

  // LUỒNG 1: LƯU NHÁP (DRAFT)
  const handleSaveDraft = async () => {
    if (isReadOnly || isSavingDraft || isCompleting) return;

    const normalizedType = normalizeCheckupType(formData.checkupType);
    const isPsy = normalizedType === CHECKUP_TYPES.PSYCHOLOGICAL;

    // Validate danh sách thuốc (chỉ áp dụng cho Khám Đa khoa / Định kỳ)
    const presResult = isPsy
      ? { isValid: true, cleanedMedicines: [], rowErrors: {} }
      : validateAndCleanMedicines(medicines);
    if (!presResult.isValid) {
      setPrescriptionErrors(presResult.rowErrors);
      showToast("Vui lòng kiểm tra và sửa thông tin thuốc bị lỗi trước khi lưu.", "error");
      return;
    }

    try {
      setIsSavingDraft(true);
      setError(null);

      const isPeriodicFlag = !isPsy && Boolean(formData.isPeriodic);

      const payload = {
        appointmentId: appointmentId ? Number(appointmentId) : undefined,
        studentId: effectiveStudentId || undefined,
        isEmergency: Boolean(formData.isEmergency),
        checkupType: normalizedType,
        isPeriodic: isPeriodicFlag,
        periodicResult: isPeriodicFlag ? formData.periodicResult || null : null,
        diseaseGroupId: isPsy
          ? null
          : formData.diseaseGroupId
          ? Number(formData.diseaseGroupId)
          : null,
        symptoms: formData.symptoms?.trim() || null,
        treatmentSuggestions: formData.treatmentSuggestions?.trim() || null,
        diagnosisNotes: formData.diagnosisNotes?.trim() || null,
        status: "DRAFT",
      };

      let targetId = checkupId;
      let latestDetail = null;
      if (!targetId) {
        // Tạo mới phiếu khám nháp
        const created = await medicalRecordApi.createCheckup(payload);
        targetId = created.id;
        latestDetail = created;
        setCheckupId(targetId);
        setCheckupDetail(created);
        // Cập nhật URL sang /doctor/checkups/{id} để tránh tạo trùng nếu bấm lại
        navigate(`/doctor/checkups/${targetId}`, { replace: true });
      } else {
        // Cập nhật phiếu khám nháp đã có
        const updated = await medicalRecordApi.updateCheckup(targetId, payload);
        latestDetail = updated;
        setCheckupDetail(updated);
      }

      if (!isPsy) {
        // Lưu danh sách thuốc đi kèm (Đa khoa / Định kỳ)
        const withMedicines = await medicalRecordApi.updatePrescriptions(
          targetId,
          presResult.cleanedMedicines
        );
        latestDetail = withMedicines;
        setMedicines(
          withMedicines.prescribedMedicines && withMedicines.prescribedMedicines.length > 0
            ? withMedicines.prescribedMedicines.map((m) => createEmptyMedicine(m))
            : presResult.cleanedMedicines.map((m) => createEmptyMedicine(m))
        );
      } else if (checkupDetail?.prescribedMedicines?.length > 0) {
        // Xóa thuốc cũ nếu trước đó phiếu là Đa khoa có kê đơn rồi chuyển sang Tâm lý
        latestDetail = await medicalRecordApi.updatePrescriptions(targetId, []);
        setMedicines([]);
      } else {
        setMedicines([]);
      }

      setCheckupDetail(latestDetail);
      setStatus("DRAFT");

      showToast("Đã lưu nháp phiếu khám thành công!", "success");
    } catch (err) {
      console.error("Lỗi khi lưu nháp phiếu khám:", err);
      const msg = err?.response?.data?.message || err?.message || "Không thể lưu nháp phiếu khám.";
      setError(msg);
      showToast(msg, "error");
    } finally {
      setIsSavingDraft(false);
    }
  };

  // LUỒNG 2: MỞ MODAL XÁC NHẬN HOÀN TẤT CA KHÁM
  const handleOpenCompleteModal = () => {
    if (isReadOnly || isSavingDraft || isCompleting) return;

    const isPsy = isPsychologicalCheckup(formData.checkupType);

    // Validate toàn diện form lâm sàng & đơn thuốc (bỏ qua đơn thuốc khi Tư vấn tâm lý)
    const formVal = validateCheckupForm(formData, true);
    const presVal = isPsy
      ? { isValid: true, rowErrors: {} }
      : validateAndCleanMedicines(medicines);

    if (!formVal.isValid || !presVal.isValid) {
      setErrors(formVal.errors || {});
      setPrescriptionErrors(presVal.rowErrors || {});
      showToast("Vui lòng điền đầy đủ các thông tin bắt buộc trước khi hoàn tất.", "error");
      return;
    }

    setErrors({});
    setPrescriptionErrors({});
    setConfirmModalOpen(true);
  };

  // LUỒNG 3: XÁC NHẬN HOÀN TẤT TRONG MODAL (3 BƯỚC TUẦN TỰ)
  const handleConfirmComplete = async () => {
    const normalizedType = normalizeCheckupType(formData.checkupType);
    const isPsy = normalizedType === CHECKUP_TYPES.PSYCHOLOGICAL;
    const presResult = isPsy
      ? { isValid: true, cleanedMedicines: [] }
      : validateAndCleanMedicines(medicines);

    try {
      setIsCompleting(true);
      setError(null);

      const isPeriodicFlag = !isPsy && Boolean(formData.isPeriodic);

      const checkupPayload = {
        appointmentId: appointmentId ? Number(appointmentId) : undefined,
        studentId: effectiveStudentId || undefined,
        isEmergency: Boolean(formData.isEmergency),
        checkupType: normalizedType,
        isPeriodic: isPeriodicFlag,
        periodicResult: isPeriodicFlag ? formData.periodicResult || null : null,
        diseaseGroupId: isPsy
          ? null
          : formData.diseaseGroupId
          ? Number(formData.diseaseGroupId)
          : null,
        symptoms: formData.symptoms?.trim() || null,
        treatmentSuggestions: formData.treatmentSuggestions?.trim() || null,
        diagnosisNotes: formData.diagnosisNotes?.trim() || null,
        status: "DRAFT", // BẮT BUỘC lưu với DRAFT để có targetId và có thể lưu đơn thuốc
      };

      // BƯỚC A: Lưu thông tin lâm sàng ở trạng thái DRAFT
      let targetId = checkupId;
      if (!targetId) {
        const created = await medicalRecordApi.createCheckup(checkupPayload);
        targetId = created.id;
        setCheckupId(targetId);
      } else {
        await medicalRecordApi.updateCheckup(targetId, checkupPayload);
      }

      // BƯỚC B: Lưu đơn thuốc (chỉ khi không phải Tư vấn tâm lý; xóa thuốc cũ nếu có)
      if (!isPsy) {
        await medicalRecordApi.updatePrescriptions(targetId, presResult.cleanedMedicines);
      } else if (checkupDetail?.prescribedMedicines?.length > 0) {
        await medicalRecordApi.updatePrescriptions(targetId, []);
      }

      // BƯỚC C: Chốt trạng thái COMPLETED qua PUT kèm đầy đủ payload (isPeriodic, periodicResult)
      const completed = await medicalRecordApi.updateCheckup(targetId, {
        ...checkupPayload,
        status: "COMPLETED",
      });

      // Cập nhật giao diện sang chế độ Chỉ đọc ngay lập tức
      setCheckupId(targetId);
      setCheckupDetail(completed);
      setStatus("COMPLETED");
      setMedicines(
        !isPsy && completed.prescribedMedicines && completed.prescribedMedicines.length > 0
          ? completed.prescribedMedicines.map((m) => createEmptyMedicine(m))
          : []
      );
      setConfirmModalOpen(false);

      showToast("Hoàn tất ca khám thành công! Phiếu khám đã được chuyển sang chế độ Chỉ đọc.", "success");
      navigate(`/doctor/checkups/${targetId}`, { replace: true });
    } catch (err) {
      console.error("Lỗi khi hoàn tất ca khám:", err);
      const msg = err?.response?.data?.message || err?.message || "Không thể hoàn tất ca khám.";
      setError(msg);
      showToast(msg, "error");
      setConfirmModalOpen(false);
    } finally {
      setIsCompleting(false);
    }
  };

  // LUỒNG 4: LẬP HỒ SƠ CHUYỂN VIỆN (Tự động lưu nháp nếu chưa có checkupId, sau đó POST /transfer)
  const handleOpenTransferModal = () => {
    if (isReadOnly || isSavingDraft || isCompleting || isSubmittingTransfer) return;
    setTransferApiError(null);
    setTransferModalOpen(true);
  };

  const handleConfirmTransfer = async (transferPayload) => {
    try {
      setIsSubmittingTransfer(true);
      setTransferApiError(null);
      setError(null);

      const normalizedType = normalizeCheckupType(formData.checkupType);
      const isPsy = normalizedType === CHECKUP_TYPES.PSYCHOLOGICAL;

      let targetId = checkupId;
      if (!targetId) {
        const isPeriodicFlag = !isPsy && Boolean(formData.isPeriodic);

        const draftPayload = {
          appointmentId: appointmentId ? Number(appointmentId) : undefined,
          studentId: effectiveStudentId || undefined,
          isEmergency: Boolean(formData.isEmergency),
          checkupType: normalizedType,
          isPeriodic: isPeriodicFlag,
          periodicResult: isPeriodicFlag ? formData.periodicResult || null : null,
          diseaseGroupId: isPsy
            ? null
            : formData.diseaseGroupId
            ? Number(formData.diseaseGroupId)
            : null,
          symptoms: formData.symptoms?.trim() || null,
          treatmentSuggestions: formData.treatmentSuggestions?.trim() || null,
          diagnosisNotes: formData.diagnosisNotes?.trim() || null,
          status: "DRAFT",
        };

        const created = await medicalRecordApi.createCheckup(draftPayload);
        targetId = created.id;
        setCheckupId(targetId);

        if (!isPsy) {
          const presResult = validateAndCleanMedicines(medicines);
          if (presResult.isValid && presResult.cleanedMedicines.length > 0) {
            await medicalRecordApi.updatePrescriptions(targetId, presResult.cleanedMedicines);
          }
        }
      }

      const transferred = await medicalRecordApi.createTransferRecord(targetId, transferPayload);

      setCheckupId(targetId);
      setCheckupDetail(transferred);
      setStatus(transferred.status || "COMPLETED");
      setFormData((prev) => ({
        ...prev,
        symptoms: transferred.symptoms || prev.symptoms,
        diagnosisNotes: transferred.diagnosisNotes || prev.diagnosisNotes,
        treatmentSuggestions: transferred.treatmentSuggestions || prev.treatmentSuggestions,
      }));
      setMedicines(
        transferred.prescribedMedicines && transferred.prescribedMedicines.length > 0
          ? transferred.prescribedMedicines.map((m) => createEmptyMedicine(m))
          : []
      );
      setTransferModalOpen(false);
      showToast(
        "Lập hồ sơ chuyển viện thành công! Ca khám tại trường đã được tự động kết thúc và khóa chỉ đọc.",
        "success"
      );
      navigate(`/doctor/checkups/${targetId}`, { replace: true });
      setPrintModalOpen(true);
    } catch (err) {
      console.error("Lỗi khi lập hồ sơ chuyển viện:", err);
      const msg =
        err?.response?.data?.message || err?.message || "Không thể lập hồ sơ chuyển viện.";
      setTransferApiError(msg);
      showToast(msg, "error");
    } finally {
      setIsSubmittingTransfer(false);
    }
  };

  const transferRecord = checkupDetail?.transferRecord || null;
  const hasTransferRecord = Boolean(transferRecord);

  const getTransferReasonLabel = (category) => {
    switch (category) {
      case "QUA_TAI":
        return "Quá tải";
      case "THIEU_THIET_BI":
        return "Thiếu thiết bị / thuốc chuyên khoa";
      case "CAP_CUU_NANG":
        return "Cấp cứu nặng vượt khả năng chuyên môn";
      default:
        return category || "—";
    }
  };

  const isPsychologicalMode = isPsychologicalCheckup(formData.checkupType);
  const isPeriodicMode = !isPsychologicalMode && Boolean(formData.isPeriodic);
  const checkupModeTitle = isPsychologicalMode
    ? "Phiếu Tư Vấn Tâm Lý"
    : isPeriodicMode
    ? "Phiếu Khám Sức Khỏe Định Kỳ"
    : "Phiếu Khám Đa Khoa";

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
      {/* Toast thông báo */}
      <Toast toast={toast} onClose={() => setToast(null)} />

      {/* HEADER & THANH ĐIỀU HƯỚNG */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate("/doctor/appointments")}
            className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors"
            title="Quay lại danh sách ca khám"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-bold text-slate-800">
                {isReadOnly
                  ? `${checkupModeTitle} (Chế độ Chỉ đọc)`
                  : checkupId
                  ? isPsychologicalMode
                    ? checkupModeTitle
                    : `${checkupModeTitle} & Kê Đơn Thuốc`
                  : `Lập ${checkupModeTitle} Mới`}
              </h1>
              {isReadOnly ? (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-300">
                  <Lock className="w-3.5 h-3.5 text-emerald-600" />
                  ĐÃ HOÀN TẤT (CHỈ ĐỌC)
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-300">
                  <Clock className="w-3.5 h-3.5 text-amber-600" />
                  BẢN NHÁP (DRAFT)
                </span>
              )}
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              {checkupDetail?.checkupCode
                ? `Mã phiếu: ${checkupDetail.checkupCode}`
                : appointmentId
                ? `Tiếp nhận từ Lịch hẹn #${appointmentId}`
                : isEmergencyParam || formData.isEmergency
                ? "Tiếp nhận khẩn cấp / Cấp cứu"
                : "Ca khám đa khoa"}
              {checkupDetail?.completedAt && (
                <span className="ml-2 font-medium text-emerald-700">
                  · Hoàn tất lúc:{" "}
                  {new Date(checkupDetail.completedAt).toLocaleString("vi-VN")}
                </span>
              )}
            </p>
          </div>
        </div>

        {/* CỤM NÚT THAO TÁC ĐẦU TRANG */}
        <div className="flex flex-wrap items-center gap-2">
          {!isReadOnly && !hasTransferRecord && (
            <button
              type="button"
              onClick={handleOpenTransferModal}
              disabled={loading || isSavingDraft || isCompleting || isSubmittingTransfer}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-red-700 bg-red-50 border border-red-200 rounded-lg hover:bg-red-100 disabled:opacity-50 transition-colors shadow-xs"
            >
              <Ambulance className="w-4 h-4 text-red-600" />
              <span>Chuyển viện</span>
            </button>
          )}

          {!isReadOnly && (
            <>
              <button
                type="button"
                onClick={handleSaveDraft}
                disabled={loading || isSavingDraft || isCompleting || isSubmittingTransfer}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 active:bg-slate-100 disabled:opacity-50 transition-colors shadow-xs"
              >
                {isSavingDraft ? (
                  <>
                    <div className="w-3.5 h-3.5 border-2 border-slate-500 border-t-transparent rounded-full animate-spin" />
                    <span>Đang lưu nháp...</span>
                  </>
                ) : (
                  <>
                    <Save className="w-4 h-4 text-slate-500" />
                    <span>Lưu nháp</span>
                  </>
                )}
              </button>

              <button
                type="button"
                onClick={handleOpenCompleteModal}
                disabled={loading || isSavingDraft || isCompleting || isSubmittingTransfer}
                className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 active:bg-blue-800 disabled:opacity-50 rounded-lg transition-colors shadow-xs"
              >
                {isCompleting ? (
                  <>
                    <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Đang hoàn tất...</span>
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Hoàn tất ca khám</span>
                  </>
                )}
              </button>
            </>
          )}
        </div>
      </div>

      {/* THÔNG BÁO LỖI NẾU CÓ */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3 text-red-700 text-sm">
          <AlertCircle className="w-5 h-5 shrink-0 mt-0.5 text-red-600" />
          <div className="flex-1">
            <p className="font-semibold">Đã xảy ra lỗi</p>
            <p className="text-xs mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* NỘI DUNG CHÍNH: BỐ CỤC 2 CỘT */}
      {loading ? (
        <div className="py-20 text-center text-slate-500 text-sm flex flex-col items-center justify-center gap-3">
          <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
          <span>Đang nạp dữ liệu phiếu khám...</span>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* CỘT TRÁI (4 cols): HỒ SƠ Y TẾ TỔNG HỢP CỦA SINH VIÊN */}
          <div className="lg:col-span-4 space-y-4">
            <PatientOverviewWrapper
              studentId={effectiveStudentId}
              fallbackData={fallbackStudentData}
              onOverviewLoaded={(data) => setStudentOverview(data)}
            />
          </div>

          {/* CỘT PHẢI (8 cols): FORM PHIẾU KHÁM & BẢNG KÊ ĐƠN THUỐC */}
          <div className="lg:col-span-8 space-y-6">
            {/* THẺ TÓM TẮT HỒ SƠ CHUYỂN VIỆN (READ-ONLY) KHI ĐÃ CÓ CHUYỂN VIỆN */}
            {hasTransferRecord && (
              <div className="bg-white rounded-xl border border-red-200/90 shadow-xs overflow-hidden">
                <div className="px-5 py-3.5 bg-red-50/60 border-b border-red-100 flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <div className="p-2 bg-red-100 text-red-600 rounded-lg">
                      <Ambulance className="w-5 h-5" />
                    </div>
                    <div>
                      <h2 className="text-sm font-bold text-slate-800 flex items-center gap-2">
                        Hồ sơ Chuyển viện Tuyến trên
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-red-100 text-red-800 border border-red-200">
                          <Lock className="w-3 h-3" />
                          Chỉ đọc
                        </span>
                      </h2>
                      <p className="text-[11px] text-slate-500">
                        Mã hồ sơ: <span className="font-bold text-slate-700">{transferRecord.recordCode}</span>
                      </p>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => setPrintModalOpen(true)}
                    className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors shadow-xs"
                  >
                    <Printer className="w-4 h-4" />
                    <span>Xem / In Giấy chuyển viện</span>
                  </button>
                </div>

                <div className="p-5 grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                  <div className="space-y-1">
                    <p className="text-slate-400 font-semibold uppercase text-[11px]">
                      Bệnh viện tuyến trên
                    </p>
                    <p className="font-bold text-slate-800 flex items-center gap-1.5">
                      <Building2 className="w-4 h-4 text-slate-400 shrink-0" />
                      {transferRecord.targetHospital}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-slate-400 font-semibold uppercase text-[11px]">
                      Phân loại lý do
                    </p>
                    <p className="font-semibold text-red-700">
                      {getTransferReasonLabel(transferRecord.reasonCategory)}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-slate-400 font-semibold uppercase text-[11px]">
                      Thời gian chuyển
                    </p>
                    <p className="font-medium text-slate-700">
                      {transferRecord.transferredAt
                        ? new Date(transferRecord.transferredAt).toLocaleString("vi-VN")
                        : "—"}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-slate-400 font-semibold uppercase text-[11px]">
                      Lý do chi tiết
                    </p>
                    <p className="text-slate-700">{transferRecord.transferReason || "—"}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Form Phiếu khám Đa khoa / Tư vấn Tâm lý */}
            <GeneralCheckupForm
              formData={formData}
              onChange={handleFormChange}
              diseaseGroups={diseaseGroups}
              isReadOnly={isReadOnly}
              canSwitchCheckupType={!appointmentId && !checkupDetail?.appointmentId && !isReadOnly}
              errors={errors}
              diseaseGroupNameFallback={checkupDetail?.diseaseGroupName}
            />

            {/* Bảng Kê đơn thuốc điện tử động (Ẩn khi Tư vấn Tâm lý) */}
            {!isPsychologicalMode && (
              <PrescriptionTable
                medicines={medicines}
                onChange={handleMedicinesChange}
                isReadOnly={isReadOnly}
                rowErrors={prescriptionErrors}
              />
            )}

            {/* CỤM NÚT DƯỚI FORM */}
            {!isReadOnly && (
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleSaveDraft}
                  disabled={isSavingDraft || isCompleting}
                  className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 active:bg-slate-100 disabled:opacity-50 transition-colors shadow-xs"
                >
                  <Save className="w-4 h-4 text-slate-500" />
                  <span>{isSavingDraft ? "Đang lưu..." : "Lưu nháp phiếu khám"}</span>
                </button>

                <button
                  type="button"
                  onClick={handleOpenCompleteModal}
                  disabled={isSavingDraft || isCompleting}
                  className="inline-flex items-center gap-1.5 px-5 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 active:bg-blue-800 disabled:opacity-50 rounded-lg transition-colors shadow-xs"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  <span>{isCompleting ? "Đang xử lý..." : "Hoàn tất ca khám"}</span>
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* MODAL XÁC NHẬN HOÀN TẤT CA KHÁM */}
      <CompleteCheckupModal
        isOpen={confirmModalOpen}
        onClose={() => setConfirmModalOpen(false)}
        onConfirm={handleConfirmComplete}
        submitting={isCompleting}
        checkupInfo={{
          studentName: fallbackStudentData.studentName,
          studentCode: fallbackStudentData.studentCode,
          isPsychological: isPsychologicalMode,
          medicinesCount: isPsychologicalMode
            ? 0
            : validateAndCleanMedicines(medicines).cleanedMedicines.length,
        }}
      />

      {/* MODAL LẬP HỒ SƠ CHUYỂN VIỆN */}
      <TransferRecordModal
        isOpen={transferModalOpen}
        onClose={() => setTransferModalOpen(false)}
        onSubmit={handleConfirmTransfer}
        submitting={isSubmittingTransfer}
        apiError={transferApiError}
        initialValues={{
          currentCondition: formData.symptoms,
          firstAidNotes: formData.treatmentSuggestions,
        }}
        studentInfo={{
          studentName: fallbackStudentData.studentName,
          studentCode: fallbackStudentData.studentCode,
        }}
        healthInsurance={studentOverview?.healthInsurance || null}
      />

      {/* MODAL XEM / IN GIẤY GIỚI THIỆU CHUYỂN VIỆN */}
      <TransferReferralPrintModal
        isOpen={printModalOpen}
        onClose={() => setPrintModalOpen(false)}
        checkupId={checkupId}
      />
    </div>
  );
}
