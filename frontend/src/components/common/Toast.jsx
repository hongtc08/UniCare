import React from "react";
import { CheckCircle2, AlertCircle, Info, X } from "lucide-react";

export default function Toast({ toast, onClose }) {
  if (!toast) return null;

  const isSuccess = toast.type === "success";
  const isError = toast.type === "error";

  return (
    <div className="fixed top-5 right-5 z-[9999] max-w-md animate-in slide-in-from-top-3 fade-in duration-200">
      <div
        className={`flex items-start gap-3 p-4 rounded-xl shadow-lg border backdrop-blur-md ${
          isSuccess
            ? "bg-emerald-50/95 border-emerald-200 text-emerald-900"
            : isError
            ? "bg-red-50/95 border-red-200 text-red-900"
            : "bg-blue-50/95 border-blue-200 text-blue-900"
        }`}
      >
        {isSuccess && <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />}
        {isError && <AlertCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />}
        {!isSuccess && !isError && <Info className="w-5 h-5 text-blue-600 shrink-0 mt-0.5" />}

        <div className="flex-1 text-sm font-medium leading-relaxed break-words">
          {toast.message}
        </div>

        <button
          type="button"
          onClick={onClose}
          className="text-slate-400 hover:text-slate-600 p-0.5 rounded-lg transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}

