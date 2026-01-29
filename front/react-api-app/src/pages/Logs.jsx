import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { getLogs } from "../api/authApi";
import "../components/css/Logs/Logs.css";

export default function Logs() {
  const { user } = useAuth();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(() => {
    if (!user?.token) return;
    setLoading(true);
    setError("");
    getLogs(user.token)
      .then((arr) => setLogs(Array.isArray(arr) ? arr : []))
      .catch((e) => setError(e.message || "Erreur lors du chargement des logs"))
      .finally(() => setLoading(false));
  }, [user?.token]);

  useEffect(() => {
    load();
  }, [load]);

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    try {
      return new Date(dateStr).toLocaleString("fr-FR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
    } catch {
      return dateStr;
    }
  };

  const getActionStyle = (action) => {
    if (action?.includes("LOGIN")) return { background: "#dcfce7", color: "#166534" };
    if (action?.includes("CREATE") || action?.includes("CREATED")) return { background: "#dbeafe", color: "#1e40af" };
    if (action?.includes("SIGN") || action?.includes("SIGNED")) return { background: "#fef3c7", color: "#92400e" };
    if (action?.includes("VALIDATE") || action?.includes("VALIDATED")) return { background: "#d1fae5", color: "#065f46" };
    if (action?.includes("DISABLE") || action?.includes("DISABLED")) return { background: "#fee2e2", color: "#991b1b" };
    return { background: "#e0e7ff", color: "#3730a3" };
  };

  return (
    <div className="logs-page">
      <div className="logs-header">
        <div className="logs-header-content">
          <h1>Journal d'audit</h1>
          <p>Historique détaillé de toutes les actions des utilisateurs</p>
        </div>
        <span className="logs-count">
          {loading ? "Chargement..." : `${logs.length} ${logs.length > 1 ? "entrées" : "entrée"}`}
        </span>
      </div>

      <div className="logs-toolbar">
        <button type="button" className="refresh-btn" onClick={load} disabled={loading}>
          {loading ? "Actualisation..." : "Actualiser"}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {loading && <div className="loading-message">Chargement des logs en cours...</div>}

      {!loading && !error && logs.length === 0 && (
        <div className="empty-message">Aucun log disponible pour le moment.</div>
      )}

      {!loading && !error && logs.length > 0 && (
        <div className="logs-table-wrapper">
          <table className="logs-table">
            <thead>
              <tr>
                <th>Date & Heure</th>
                <th>Utilisateur</th>
                <th>Action</th>
                <th>Entité</th>
                <th>Message</th>
                <th>Adresse IP</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{formatDate(log.createdAt)}</td>
                  <td>
                    <div className="actor-name">
                      {log.actorName || log.actorMail || `Utilisateur #${log.actorUserId || "?"}`}
                    </div>
                    {log.actorMail && log.actorName && (
                      <div className="actor-email">{log.actorMail}</div>
                    )}
                  </td>
                  <td>
                    <span className="action-badge" style={getActionStyle(log.action)}>
                      {log.action || "—"}
                    </span>
                  </td>
                  <td>
                    <span className="entity-badge">
                      {log.entity || "—"}
                      {log.entityId && ` #${log.entityId}`}
                    </span>
                  </td>
                  <td title={log.message}>
                    <span className="log-message">{log.message || "—"}</span>
                  </td>
                  <td>
                    <span className="ip-address">{log.ipAddress || "—"}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
