import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { getOrdersReceived, validateOrder } from "../api/authApi";
import "../components/css/ListOrder/ListOrder.css";

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
        { label: "Scan de la vidéo", status: "loading" },
        { label: "Déchiffrement de la vidéo", status: "loading" },
        { label: "Vérification signature RSA", status: "loading" },
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
        setValidateError(e.message || "Vidéo corrompue ou signature invalide.");
        setValidateSteps([
          { label: "Scan de la vidéo", status: "done" },
          { label: "Déchiffrement de la vidéo", status: "done" },
          { label: "Vérification signature RSA", status: "error" },
        ]);
      } finally {
        setValidatingId(null);
      }
    },
    [user?.token, validateVideoUrl]
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
    <div className="list-order-page">
      <div className="list-order-header">
        <h1>Ordres reçus</h1>
        <p>Vidéos sécurisées qui vous ont été envoyées. Validez pour vérifier l'intégrité et visionner.</p>
      </div>

      <div className="toolbar">
        <label>
          <input type="checkbox" checked={filterShowExpired} onChange={(e) => setFilterShowExpired(e.target.checked)} />
          <span>Afficher les vidéos expirées</span>
        </label>
        <button type="button" className="toggle-btn" onClick={load}>
          Actualiser
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {loading && <div className="loading-message">Chargement en cours...</div>}

      {!loading && !error && filtered.length === 0 && (
        <div className="empty-message">
          Aucun ordre {filterShowExpired ? "expiré" : "actif"} trouvé.
        </div>
      )}

      {!loading && !error && filtered.length > 0 && (
        <div className="table-wrapper">
          <table className="orders-table">
            <thead>
              <tr>
                <th>Titre</th>
                <th>Expéditeur</th>
                <th>Hash</th>
                <th>Chemin</th>
                <th>Expiration</th>
                <th>Statut</th>
                <th>Signé le</th>
                <th>Créé le</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((o) => (
                <tr key={o.id}>
                  <td>{o.videoName || "—"}</td>
                  <td><span className="sender-name">{o.senderName || "—"}</span></td>
                  <td title={o.videoHash}><span className="hash-cell">{o.videoHash ? `${o.videoHash.slice(0, 10)}…` : "—"}</span></td>
                  <td title={o.pathVideo}><span className="path-cell">{o.pathVideo || "—"}</span></td>
                  <td>{fmt(o.expiredVideo)}</td>
                  <td><span className={`status-badge ${o.active ? "active" : "expired"}`}>{o.active ? "Actif" : "Expiré"}</span></td>
                  <td>{fmt(o.signedAt)}</td>
                  <td>{fmt(o.createdAt)}</td>
                  <td>
                    <button
                      type="button"
                      className="validate-btn"
                      onClick={() => onValidate(o.id)}
                      disabled={validatingId != null}
                    >
                      {validatingId === o.id ? "Validation..." : "Valider"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {(validateSteps.length > 0 || validateError || validateVideo) && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-title">Validation de l'ordre</div>
            {validateSteps.length > 0 && (
              <div className="validation-steps">
                {validateSteps.map((s, i) => (
                  <div key={i} className={`validation-step ${s.status === "done" ? "step-done" : s.status === "error" ? "step-error" : "step-loading"}`}>
                    {s.status === "done" ? "✓" : s.status === "error" ? "✗" : "⋯"} {s.label}
                  </div>
                ))}
              </div>
            )}
            {validateError && <div className="video-error">{validateError}</div>}
            {validateVideoUrl && <video className="video-player" src={validateVideoUrl} controls />}
            <button type="button" className="close-modal-btn" onClick={closeModal}>Fermer</button>
          </div>
        </div>
      )}
    </div>
  );
}
