import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { getLogs } from "../api/authApi";

const styles = {
  page: {
    maxWidth: 1400,
    margin: "0 auto",
    padding: "18px 16px 32px",
    fontFamily: "ui-sans-serif, system-ui, sans-serif",
    boxSizing: "border-box",
  },
  h1: { margin: "0 0 8px", fontSize: 24, fontWeight: 800 },
  sub: { margin: 0, color: "#64748b", fontSize: 14 },
  toolbar: {
    display: "flex",
    alignItems: "center",
    gap: 12,
    marginBottom: 16,
    flexWrap: "wrap",
  },
  btnRefresh: {
    padding: "8px 12px",
    borderRadius: 10,
    border: "1px solid #e2e8f0",
    background: "#fff",
    cursor: "pointer",
    fontWeight: 600,
    fontSize: 13,
  },
  tableWrap: {
    overflowX: "auto",
    border: "1px solid #e2e8f0",
    borderRadius: 12,
    background: "#fff",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    fontSize: 13,
  },
  th: {
    textAlign: "left",
    padding: "12px 10px",
    borderBottom: "1px solid #e2e8f0",
    background: "#f8fafc",
    fontWeight: 700,
    color: "#0f172a",
    position: "sticky",
    top: 0,
    zIndex: 1,
  },
  td: {
    padding: "10px",
    borderBottom: "1px solid #f1f5f9",
    verticalAlign: "top",
  },
  actor: {
    fontWeight: 600,
    color: "#0f172a",
  },
  action: {
    padding: "4px 8px",
    borderRadius: 6,
    fontSize: 11,
    fontWeight: 700,
    display: "inline-block",
    background: "#e0e7ff",
    color: "#3730a3",
  },
  entity: {
    fontFamily: "monospace",
    fontSize: 11,
    color: "#64748b",
    background: "#f1f5f9",
    padding: "2px 6px",
    borderRadius: 4,
  },
  message: {
    color: "#475569",
    maxWidth: 300,
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  metadata: {
    fontFamily: "monospace",
    fontSize: 10,
    color: "#94a3b8",
    maxWidth: 200,
    overflow: "hidden",
    textOverflow: "ellipsis",
  },
  load: { textAlign: "center", padding: 24, color: "#64748b" },
  err: {
    padding: 12,
    background: "#fef2f2",
    border: "1px solid #fecaca",
    borderRadius: 10,
    color: "#b91c1c",
    marginBottom: 16,
  },
  empty: { textAlign: "center", padding: 48, color: "#64748b" },
  count: {
    fontSize: 12,
    color: "#64748b",
    marginLeft: 12,
  },
};

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
      .catch((e) => setError(e.message || "Erreur chargement logs"))
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

  const getActionColor = (action) => {
    if (action?.includes("LOGIN")) return { background: "#dcfce7", color: "#166534" };
    if (action?.includes("CREATE") || action?.includes("CREATED")) return { background: "#dbeafe", color: "#1e40af" };
    if (action?.includes("SIGN") || action?.includes("SIGNED")) return { background: "#fef3c7", color: "#92400e" };
    if (action?.includes("VALIDATE") || action?.includes("VALIDATED")) return { background: "#d1fae5", color: "#065f46" };
    if (action?.includes("DISABLE") || action?.includes("DISABLED")) return { background: "#fee2e2", color: "#991b1b" };
    return { background: "#e0e7ff", color: "#3730a3" };
  };

  return (
    <div style={styles.page}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 8 }}>
        <div>
          <h1 style={styles.h1}>Logs d'audit</h1>
          <p style={styles.sub}>Historique de toutes les actions des utilisateurs</p>
        </div>
        <span style={styles.count}>
          {loading ? "…" : `${logs.length} log${logs.length > 1 ? "s" : ""}`}
        </span>
      </div>

      <div style={styles.toolbar}>
        <button type="button" style={styles.btnRefresh} onClick={load} disabled={loading}>
          {loading ? "Actualisation…" : "Actualiser"}
        </button>
      </div>

      {error && <div style={styles.err}>{error}</div>}
      {loading && <div style={styles.load}>Chargement des logs…</div>}

      {!loading && !error && logs.length === 0 && (
        <div style={styles.empty}>Aucun log disponible.</div>
      )}

      {!loading && !error && logs.length > 0 && (
        <div style={styles.tableWrap}>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Date</th>
                <th style={styles.th}>Utilisateur</th>
                <th style={styles.th}>Action</th>
                <th style={styles.th}>Entité</th>
                <th style={styles.th}>Message</th>
                <th style={styles.th}>IP</th>
                {/* <th style={styles.th}>Métadonnées</th> */}
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td style={styles.td}>{formatDate(log.createdAt)}</td>
                  <td style={styles.td}>
                    <div style={styles.actor}>
                      {log.actorName || log.actorMail || `User #${log.actorUserId || "?"}`}
                    </div>
                    {log.actorMail && log.actorName && (
                      <div style={{ fontSize: 11, color: "#64748b", marginTop: 2 }}>
                        {log.actorMail}
                      </div>
                    )}
                  </td>
                  <td style={styles.td}>
                    <span style={{ ...styles.action, ...getActionColor(log.action) }}>
                      {log.action || "—"}
                    </span>
                  </td>
                  <td style={styles.td}>
                    <span style={styles.entity}>
                      {log.entity || "—"}
                      {log.entityId && ` #${log.entityId}`}
                    </span>
                  </td>
                  <td style={styles.td} title={log.message}>
                    <span style={styles.message}>{log.message || "—"}</span>
                  </td>
                  <td style={styles.td}>
                    <span style={{ fontSize: 11, color: "#64748b" }}>
                      {log.ipAddress || "—"}
                    </span>
                  </td>
                  <td style={styles.td} title={log.metadata}>
                    <span style={styles.metadata}>
                      {log.metadata ? (log.metadata.length > 30 ? `${log.metadata.slice(0, 30)}…` : log.metadata) : "—"}
                    </span>
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
