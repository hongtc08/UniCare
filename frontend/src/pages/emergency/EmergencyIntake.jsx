import { useState } from "react";
import { Download, Plus } from "lucide-react";
import EmergencyIntakeModal from "../../components/emergency/EmergencyIntakeModal";
import { EmergencyStats, EmergencyWorkflow, EmergencyCaseTable } from "../../components/emergency/EmergencyOverview";
import "./EmergencyIntake.css";

export default function EmergencyIntake() {
  const [open, setOpen] = useState(false);
  const [cases, setCases] = useState([]);

  function handleCreated(checkup) {
    setCases((current) => [checkup, ...current.filter((item) => item.id !== checkup.id)]);
    setOpen(false);
  }

  return (
    <div className="emergency-page">
      <header className="emergency-page-heading">
        <div>
          <h1>Cấp cứu &amp; chuyển viện</h1>
          <p>Quản lý tiếp nhận, sơ cứu, chuyển viện bên ngoài và kết thúc ca.</p>
        </div>
        <div className="emergency-table-actions">
          <button className="emergency-button" disabled><Download size={17} aria-hidden="true" /> Xuất dữ liệu</button>
          <button className="emergency-button emergency-button-primary" onClick={() => setOpen(true)}>
            <Plus size={18} aria-hidden="true" /> Tiếp nhận ca khẩn cấp
          </button>
        </div>
      </header>
      <EmergencyStats count={cases.length} />
      <EmergencyWorkflow />
      <EmergencyCaseTable cases={cases} />
      {open && <EmergencyIntakeModal onClose={() => setOpen(false)} onCreated={handleCreated} />}
    </div>
  );
}
