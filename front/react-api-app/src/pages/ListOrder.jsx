import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { getOrdersReceived, validateOrder } from "../api/authApi";

const styles = {
  page: { maxWidth: 1200, margin: "0 auto", padding: "18px 16px 32px", fontFamily: "ui-sans-serif, system-ui, sans-serif", boxSizing: "border-box" },
  h1: { margin: "0 0 8px", fontSize: 24 },
  sub: { margin: 0, color: "#64748b", fontSize: 14 },
  toolbar: { display: "flex", alignItems: "center", gap: 12, marginBottom: 16, flexWrap: "wrap" },
  toggle: { padding: "8px 12px", borderRadius: 10, border: "1px solid #e2e8f0", background: "#fff", cursor: "pointer", fontWeight: 600, fontSize: 13 },
  tableWrap: { overflowX: "auto", border: "1px solid #e2e8f0", borderRadius: 12, background: "#fff" },
  table: { width: "100%", borderCollapse: "collapse", fontSize: 13 },
  th: { textAlign: "left", padding: "12px 10px", borderBottom: "1px solid #e2e8f0", background: "#f8fafc", fontWeight: 700, color: "#0f172a" },
  td: { padding: "10px", borderBottom: "1px solid #f1f5f9" },
  hash: { fontFamily: "monospace", fontSize: 11, color: "#64748b", maxWidth: 120, overflow: "hidden", textOverflow: "ellipsis" },
  path: { maxWidth: 200, overflow: "hidden", textOverflow: "ellipsis", fontSize: 12, color: "#475569" },
  badge: (ok) => ({ padding: "4px 8px", borderRadius: 8, fontSize: 11, fontWeight: 700, background: ok ? "#dcfce7" : "#fee2e2", color: ok ? "#166534" : "#b91c1c" }),
  btn: { padding: "6px 10px", borderRadius: 8, border: "none", background: "#0f172a", color: "#fff", fontWeight: 700, cursor: "pointer", fontSize: 12 },
  btnDisabled: { opacity: 0.6, cursor: "not-allowed" },
  load: { textAlign: "center", padding: 24, color: "#64748b" },
  err: { padding: 12, background: "#fef2f2", border: "1px solid #fecaca", borderRadius: 10, color: "#b91c1c", marginBottom: 16 },
  modal: { position: "fixed", inset: 0, background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100 },
  modalBox: { background: "#fff", borderRadius: 16, padding: 20, maxWidth: 720, width: "90%", maxHeight: "90vh", overflow: "auto" },
  modalTitle: { margin: "0 0 12px", fontSize: 16, fontWeight: 800 },
  steps: { marginBottom: 16 },
  step: (s) => ({ fontSize: 13, marginBottom: 6, color: s === "done" ? "#15803d" : s === "error" ? "#b91c1c" : "#64748b" }),
  video: { width: "100%", borderRadius: 12, background: "#0f172a", marginTop: 8 },
  videoErr: { color: "#b91c1c", fontWeight: 700, marginTop: 8 },
  close: { marginTop: 12, padding: "8px 14px", borderRadius: 10, border: "1px solid #e2e8f0", background: "#fff", cursor: "pointer", fontWeight: 700 },
  empty: { textAlign: "center", padding: 48, color: "#64748b" },
};

export default function ListOrder() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filterShowExpired, setFilterShowExpired] = useState(false);
  const [validatingId, setValidatingId] = useState(null);
  const [validateSteps, setValidateSteps] = useState([]);
  const [validateError, setValidateError] = useState("");
  const [validateVideo, setValidateVideo] = useState(null);
  const [validateVideoUrl, setValidateVideoUrl] = useState(null);

  const load = useCallback(() => {
    if (!user?.token) return;
    setLoading(true);
    setError("");
    getOrdersReceived(user.token)
      .then((arr) => setOrders(Array.isArray(arr) ? arr : []))
      .catch((e) => setError(e.message || "Erreur chargement"))
      .finally(() => setLoading(false));
  }, [user?.token]);

  useEffect(() => { load(); }, [load]);

  const filtered = filterShowExpired
    ? orders.filter((o) => !o.active)
    : orders.filter((o) => o.active);

  const onValidate = useCallback(
    async (id) => {
      if (!user?.token) return;
      if (validateVideoUrl) URL.revokeObjectURL(validateVideoUrl);
      setValidatingId(id);
      setValidateSteps([
        { label: "Scan de la vidéo…", status: "loading" },
        { label: "Déchiffrement de la vidéo…", status: "loading" },
        { label: "Vérification signature RSA…", status: "loading" },
      ]);
      setValidateError("");
      setValidateVideo(null);
      setValidateVideoUrl(null);

      try {
        const res = await validateOrder(id, user.token);
        setValidateSteps([
          { label: "Scan de la vidéo", status: "done" },
          { label: "Déchiffrement de la vidéo", status: "done" },
          { label: "Vérification signature RSA", status: "done" },
        ]);
        if (res.videoBase64) {
          setValidateVideo(res.videoBase64);
          const bin = Uint8Array.from(atob(res.videoBase64), (c) => c.charCodeAt(0));
          const blob = new Blob([bin], { type: "video/webm" });
          setValidateVideoUrl(URL.createObjectURL(blob));
        }
      } catch (e) {
        setValidateError(e.message || "Vidéo corrompue.");
        setValidateSteps([
          { label: "Scan de la vidéo", status: "done" },
          { label: "Déchiffrement de la vidéo", status: "done" },
          { label: "Vérification signature RSA", status: "error" },
        ]);
      } finally {
        setValidatingId(null);
      }
    },
    [user?.token]
  );

  const closeModal = useCallback(() => {
    if (validateVideoUrl) URL.revokeObjectURL(validateVideoUrl);
    setValidateVideoUrl(null);
    setValidateVideo(null);
    setValidateError("");
    setValidateSteps([]);
  }, [validateVideoUrl]);

  const fmt = (s) => (s ? new Date(s).toLocaleString("fr-FR") : "—");

  return (
    <div style={styles.page}>
      <h1 style={styles.h1}>Liste d’ordres reçus</h1>
      <p style={styles.sub}>Vidéos qui vous ont été envoyées. Validez pour vérifier l’intégrité et regarder.</p>

      <div style={styles.toolbar}>
        <label>
          <input type="checkbox" checked={filterShowExpired} onChange={(e) => setFilterShowExpired(e.target.checked)} />
          <span style={{ marginLeft: 8, fontSize: 13 }}>Voir les vidéos expirées</span>
        </label>
        <button type="button" style={styles.toggle} onClick={load}>
          Actualiser
        </button>
      </div>

      {error && <div style={styles.err}>{error}</div>}
      {loading && <div style={styles.load}>Chargement…</div>}

      {!loading && !error && filtered.length === 0 && (
        <div style={styles.empty}>Aucun ordre reçu{filterShowExpired ? " (expiré)" : ""}.</div>
      )}

      {!loading && !error && filtered.length > 0 && (
        <div style={styles.tableWrap}>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Titre</th>
                <th style={styles.th}>Hash</th>
                <th style={styles.th}>Chemin</th>
                <th style={styles.th}>Expiration</th>
                <th style={styles.th}>Statut</th>
                <th style={styles.th}>Signé le</th>
                <th style={styles.th}>Créé le</th>
                <th style={styles.th}></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((o) => (
                <tr key={o.id}>
                  <td style={styles.td}>{o.videoName || "—"}</td>
                  <td style={styles.td} title={o.videoHash}><span style={styles.hash}>{o.videoHash ? `${o.videoHash.slice(0, 10)}…` : "—"}</span></td>
                  <td style={styles.td} title={o.pathVideo}><span style={styles.path}>{o.pathVideo || "—"}</span></td>
                  <td style={styles.td}>{fmt(o.expiredVideo)}</td>
                  <td style={styles.td}><span style={styles.badge(o.active)}>{o.active ? "Actif" : "Expiré"}</span></td>
                  <td style={styles.td}>{fmt(o.signedAt)}</td>
                  <td style={styles.td}>{fmt(o.createdAt)}</td>
                  <td style={styles.td}>
                    <button
                      type="button"
                      style={{ ...styles.btn, ...(validatingId === o.id ? styles.btnDisabled : {}) }}
                      onClick={() => onValidate(o.id)}
                      disabled={validatingId != null}
                    >
                      {validatingId === o.id ? "…" : "Valider"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {(validateSteps.length > 0 || validateError || validateVideo) && (
        <div style={styles.modal} onClick={closeModal}>
          <div style={styles.modalBox} onClick={(e) => e.stopPropagation()}>
            <div style={styles.modalTitle}>Validation de l’ordre</div>
            {validateSteps.length > 0 && (
              <div style={styles.steps}>
                {validateSteps.map((s, i) => (
                  <div key={i} style={styles.step(s.status)}>{s.status === "done" ? "✓ " : s.status === "error" ? "✗ " : "⋯ "}{s.label}</div>
                ))}
              </div>
            )}
            {validateError && <div style={styles.videoErr}>{validateError}</div>}
            {validateVideoUrl && <video style={styles.video} src={validateVideoUrl} controls />}
            <button type="button" style={styles.close} onClick={closeModal}>Fermer</button>
          </div>
        </div>
      )}
    </div>
  );
}
