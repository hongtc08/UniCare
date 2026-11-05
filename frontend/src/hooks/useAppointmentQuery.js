import { useCallback, useEffect, useState } from "react";
import { appointmentError } from "../utils/appointments";

// request must be a stable useCallback. Old requests cannot overwrite a newer selection.
export function useAppointmentQuery(request) {
  const [revision, setRevision] = useState(0);
  const [result, setResult] = useState(null);
  const reload = useCallback(() => setRevision((value) => value + 1), []);
  useEffect(() => {
    const controller = new AbortController();
    Promise.resolve().then(() => request(controller.signal))
      .then((data) => {
        if (!controller.signal.aborted) setResult({ request, revision, data, error: "" });
      })
      .catch((cause) => {
        if (!controller.signal.aborted) setResult({ request, revision, data: [], error: appointmentError(cause) });
      });
    return () => controller.abort();
  }, [request, revision]);
  const current = result?.request === request && result?.revision === revision;
  return { data: current ? result.data : [], error: current ? result.error : "", loading: !current, reload };
}
