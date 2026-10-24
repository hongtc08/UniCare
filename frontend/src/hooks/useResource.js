import { useCallback, useEffect, useState } from "react";
import { adminApi, errorMessage } from "../api/adminApi";

export function useResource(resource, enabled = true) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);
  const reload = useCallback(() => {
    setLoading(true);
    setError("");
    setRevision((value) => value + 1);
  }, []);

  useEffect(() => {
    if (!enabled) return;
    let active = true;
    adminApi
      .list(resource)
      .then((data) => {
        if (active) {
          setRecords(data);
          setError("");
        }
      })
      .catch((cause) => {
        if (active) setError(errorMessage(cause));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [resource, revision, enabled]);

  return { records, loading, error, reload };
}
