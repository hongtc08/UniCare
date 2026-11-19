import { useEffect, useId, useRef } from "react";
import { X } from "lucide-react";

function isBackdrop(event) {
  if (event.target !== event.currentTarget) return false;
  const { left, right, top, bottom } = event.currentTarget.getBoundingClientRect();
  return event.clientX < left || event.clientX >= right || event.clientY < top || event.clientY >= bottom;
}

export default function Modal({
  isOpen,
  onClose,
  title,
  description,
  children,
  busy = false,
  closeOnBackdrop = false,
  className = "",
}) {
  const dialog = useRef(null);
  const pointerStartedOnBackdrop = useRef(false);
  const titleId = useId();

  useEffect(() => {
    if (!isOpen) return;
    const previous = document.activeElement;
    const element = dialog.current;
    element.showModal();
    const overflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      element.close();
      document.body.style.overflow = overflow;
      previous?.focus();
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <dialog
      ref={dialog}
      className={`app-dialog ${className}`}
      aria-labelledby={titleId}
      onPointerDown={(event) => {
        pointerStartedOnBackdrop.current = closeOnBackdrop && isBackdrop(event);
      }}
      onClick={(event) => {
        if (closeOnBackdrop && !busy && pointerStartedOnBackdrop.current && isBackdrop(event)) onClose();
        pointerStartedOnBackdrop.current = false;
      }}
      aria-describedby={description ? `${titleId}-description` : undefined}
      onCancel={(event) => {
        event.preventDefault();
        if (!busy) onClose();
      }}
    >
      <div className="dialog-heading">
        {description ? (
          <div>
            <h2 id={titleId}>{title}</h2>
            <p id={`${titleId}-description`}>{description}</p>
          </div>
        ) : <h2 id={titleId}>{title}</h2>}
        <button
          type="button"
          className="icon-button"
          aria-label="Đóng hộp thoại"
          disabled={busy}
          onClick={onClose}
        >
          <X size={20} />
        </button>
      </div>
      <div className="dialog-body">{children}</div>
    </dialog>
  );
}
