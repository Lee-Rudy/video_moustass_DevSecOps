import { useMemo, useEffect, useRef, useState, useCallback } from "react";

/**
 * Clean Transaction Video Form (FULL)
 * ✅ Fix superposition champs (grid responsive + minWidth/boxSizing)
 * ✅ Force webcam PC (ignore phone/continuity/droidcam) via enumerateDevices + deviceId exact
 * ✅ Real live recording (getUserMedia + MediaRecorder)
 * ✅ Preview live + playback + download
 *
 * Notes:
 * - getUserMedia works on https or localhost
 * - First time: browser permission popup is normal (mandatory)
 */

function useMediaQuery(query) {
  const get = () =>
    typeof window !== "undefined" ? window.matchMedia(query).matches : false;
  const [matches, setMatches] = useState(get);

  useEffect(() => {
    const m = window.matchMedia(query);
    const onChange = () => setMatches(m.matches);
    onChange();
    m.addEventListener?.("change", onChange);
    return () => m.removeEventListener?.("change", onChange);
  }, [query]);

  return matches;
}

export default function About() {
  // Mock data (later from API)
  const people = useMemo(
    () => [
      { id: "u1", name: "Bruner Noel" },
      { id: "u2", name: "Moussa Diallo" },
      { id: "u3", name: "Mino Zo" },
      { id: "u4", name: "Rota Randria" },
      { id: "u5", name: "Alice Rakoto" },
    ],
    []
  );

  // -------------------------
  // Responsive
  // -------------------------
  const isNarrow = useMediaQuery("(max-width: 920px)");

  // -------------------------
  // Form state
  // -------------------------
  const [search, setSearch] = useState("");
  const [recipientId, setRecipientId] = useState("");
  const [amount, setAmount] = useState("5000");
  const [title, setTitle] = useState("2000 dollars");
  const [errors, setErrors] = useState({
    recipientId: "",
    amount: "",
    title: "",
  });

  const selectedRecipient = useMemo(
    () => people.find((p) => p.id === recipientId) || null,
    [people, recipientId]
  );

  const filteredPeople = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return people;
    return people.filter((p) => p.name.toLowerCase().includes(q));
  }, [people, search]);

  // -------------------------
  // Helpers: sanitize + validate
  // -------------------------
  const sanitizeTitle = (raw) => {
    let v = raw.replace(/\s+/g, " ").trim();
    v = v.replace(/[^a-zA-Z0-9 \-_.()]/g, "");
    v = v.replace(/^[.\-_ ]+|[.\-_ ]+$/g, "");
    if (v.length > 80) v = v.slice(0, 80).trim();
    return v;
  };

  const normalizeAmount = (raw) => {
    let v = raw.replace(/[^\d.]/g, "");
    const firstDot = v.indexOf(".");
    if (firstDot !== -1) {
      v =
        v.slice(0, firstDot + 1) + v.slice(firstDot + 1).replace(/\./g, "");
      const [a, b] = v.split(".");
      v = a + "." + (b || "").slice(0, 2);
    }
    if (v.length > 12) v = v.slice(0, 12);
    return v;
  };

  const validate = useCallback(() => {
    const next = { recipientId: "", amount: "", title: "" };

    if (!recipientId)
      next.recipientId = "Veuillez sélectionner un destinataire.";

    if (!amount.trim()) next.amount = "Veuillez saisir un montant.";
    else if (!/^\d+(\.\d{1,2})?$/.test(amount)) {
      next.amount = "Format invalide (ex: 5000 ou 5000.50).";
    }

    const cleanTitle = sanitizeTitle(title);
    if (!cleanTitle) next.title = "Veuillez saisir un titre valide.";
    else if (cleanTitle.length < 4)
      next.title = "Titre trop court (min 4 caractères).";

    setErrors(next);
    return !next.recipientId && !next.amount && !next.title;
  }, [recipientId, amount, title]);

  // -------------------------
  // Recording state (real)
  // -------------------------
  const videoLiveRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const streamRef = useRef(null);
  const chunksRef = useRef([]);

  const [recOpen, setRecOpen] = useState(false); // show/hide recorder UI
  const [recStatus, setRecStatus] = useState("idle"); // idle | recording | paused | stopped
  const [recError, setRecError] = useState("");
  const [recordedUrl, setRecordedUrl] = useState("");
  const [recordedBlob, setRecordedBlob] = useState(null);

  const cleanupStream = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((t) => t.stop());
      streamRef.current = null;
    }
    if (videoLiveRef.current) {
      videoLiveRef.current.srcObject = null;
    }
  }, []);

  const revokeRecordedUrl = useCallback(() => {
    if (recordedUrl) URL.revokeObjectURL(recordedUrl);
  }, [recordedUrl]);

  useEffect(() => {
    // cleanup on unmount
    return () => {
      revokeRecordedUrl();
      cleanupStream();
    };
  }, [cleanupStream, revokeRecordedUrl]);

  // Pick "PC webcam" (avoid phone / continuity / droidcam)
  const pickBestCamera = (devices) => {
    const cams = devices.filter((d) => d.kind === "videoinput");
    const blacklist = /infinix|android|iphone|continuity|droidcam|epoccam/i;
    const whitelist = /integrated|built-in|webcam|usb|hd camera|logitech|camera/i;

    const preferred =
      cams.find((c) => whitelist.test(c.label) && !blacklist.test(c.label)) ||
      cams.find((c) => !blacklist.test(c.label)) ||
      cams[0];

    return preferred?.deviceId || "";
  };

  const openRecorder = useCallback(async () => {
    setRecError("");
    setRecOpen(true);

    try {
      // 1) ask permission once so labels are available
      const tmp = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true,
      });
      tmp.getTracks().forEach((t) => t.stop());

      // 2) list devices + pick best camera
      const devices = await navigator.mediaDevices.enumerateDevices();
      const bestVideoId = pickBestCamera(devices);

      // 3) open stream forcing deviceId exact (=> avoids phone)
      const stream = await navigator.mediaDevices.getUserMedia({
        video: bestVideoId ? { deviceId: { exact: bestVideoId } } : true,
        audio: true,
      });

      streamRef.current = stream;

      if (videoLiveRef.current) {
        videoLiveRef.current.srcObject = stream;
        videoLiveRef.current.muted = true; // allow autoplay
        await videoLiveRef.current.play().catch(() => {});
      }

      setRecStatus("idle");
    } catch (err) {
      setRecError(
        "Impossible d’accéder à la caméra/micro. Vérifie permissions (HTTPS/localhost) et la disponibilité de ta webcam PC."
      );
      cleanupStream();
    }
  }, [cleanupStream]);

  const startRecording = useCallback(() => {
    setRecError("");

    if (!streamRef.current) {
      setRecError("Aucun flux caméra détecté. Clique d’abord sur “Enregistrer une vidéo”.");
      return;
    }

    // reset previous
    chunksRef.current = [];
    revokeRecordedUrl();
    setRecordedUrl("");
    setRecordedBlob(null);

    const mimeCandidates = [
      "video/webm;codecs=vp9,opus",
      "video/webm;codecs=vp8,opus",
      "video/webm",
    ];
    const mimeType =
      mimeCandidates.find((m) => window.MediaRecorder?.isTypeSupported?.(m)) ||
      "";

    try {
      const recorder = new MediaRecorder(
        streamRef.current,
        mimeType ? { mimeType } : undefined
      );
      mediaRecorderRef.current = recorder;

      recorder.ondataavailable = (e) => {
        if (e.data && e.data.size > 0) chunksRef.current.push(e.data);
      };

      recorder.onstop = () => {
        const blob = new Blob(chunksRef.current, {
          type: recorder.mimeType || "video/webm",
        });
        const url = URL.createObjectURL(blob);
        setRecordedBlob(blob);
        setRecordedUrl(url);
        setRecStatus("stopped");
      };

      recorder.start(250);
      setRecStatus("recording");
    } catch (err) {
      setRecError("Erreur MediaRecorder. Essaie Chrome/Edge et vérifie les permissions.");
    }
  }, [revokeRecordedUrl]);

  const pauseRecording = useCallback(() => {
    const r = mediaRecorderRef.current;
    if (!r) return;
    if (r.state === "recording") {
      r.pause();
      setRecStatus("paused");
    }
  }, []);

  const resumeRecording = useCallback(() => {
    const r = mediaRecorderRef.current;
    if (!r) return;
    if (r.state === "paused") {
      r.resume();
      setRecStatus("recording");
    }
  }, []);

  const stopRecording = useCallback(() => {
    const r = mediaRecorderRef.current;
    if (!r) return;
    if (r.state === "recording" || r.state === "paused") {
      r.stop();
    }
  }, []);

  const closeRecorder = useCallback(() => {
    setRecError("");

    // Stop recorder if running
    const r = mediaRecorderRef.current;
    if (r && (r.state === "recording" || r.state === "paused")) {
      r.stop();
    }
    mediaRecorderRef.current = null;

    // Stop stream
    cleanupStream();

    setRecStatus("idle");
    setRecOpen(false);
  }, [cleanupStream]);

  const clearRecorded = useCallback(() => {
    revokeRecordedUrl();
    setRecordedUrl("");
    setRecordedBlob(null);
  }, [revokeRecordedUrl]);

  // -------------------------
  // Submit / Reset
  // -------------------------
  const onSubmit = (e) => {
    e.preventDefault();
    const ok = validate();
    if (!ok) return;

    const payload = {
      recipientId,
      amount: amount.trim(),
      title: sanitizeTitle(title),
      hasVideo: !!recordedBlob,
      // later: send recordedBlob with FormData
    };

    console.log("Front payload (demo):", payload);
    alert(
      recordedBlob
        ? "OK ✅ Formulaire + vidéo enregistrée (front). Prêt à brancher backend."
        : "OK ✅ Formulaire (front). Enregistre la vidéo pour compléter."
    );
  };

  const onReset = () => {
    setSearch("");
    setRecipientId("");
    setAmount("");
    setTitle("");
    setErrors({ recipientId: "", amount: "", title: "" });

    // recording reset
    closeRecorder();
    clearRecorded();
  };

  // -------------------------
  // UI
  // -------------------------
  return (
    <div style={ui.page}>
      <div style={ui.header}>
        <div>
          <h1 style={ui.h1}>Nouvelle transaction vidéo</h1>
          <p style={ui.sub}>
            Remplissez les informations, puis lancez l’enregistrement (front only pour l’instant).
          </p>
        </div>
        {/* <div style={ui.badge}>Draft</div> */}
      </div>

      <form onSubmit={onSubmit} style={ui.card}>
        {/* Top grid: recipient + amount */}
        <div style={ui.grid2(isNarrow)}>
          {/* Recipient */}
          <div style={ui.gridItem}>
            <label style={ui.label}>Envoyé à</label>

            <div style={ui.stack8}>
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Rechercher un nom complet…"
                style={ui.input}
              />

              <select
                value={recipientId}
                onChange={(e) => setRecipientId(e.target.value)}
                style={ui.select}
              >
                <option value="" style={ui.option}>
                  — Sélectionner —
                </option>
                {filteredPeople.map((p) => (
                  <option key={p.id} value={p.id} style={ui.option}>
                    {p.name}
                  </option>
                ))}
              </select>

              {errors.recipientId ? (
                <div style={ui.error}>{errors.recipientId}</div>
              ) : null}
            </div>
          </div>

          {/* Amount */}
          <div style={ui.gridItem}>
            <label style={ui.label}>Montant de la transaction ($)</label>
            <div style={ui.amountWrap}>
              <span style={ui.currency}>$</span>
              <input
                value={amount}
                onChange={(e) => setAmount(normalizeAmount(e.target.value))}
                placeholder="ex: 5000 ou 5000.50"
                inputMode="decimal"
                style={ui.amount}
              />
            </div>
            {errors.amount ? <div style={ui.error}>{errors.amount}</div> : null}
          </div>
        </div>

        {/* Title */}
        <div style={{ marginTop: 16 }}>
          <label style={ui.label}>Titre de la vidéo</label>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            onBlur={(e) => {
              const clean = sanitizeTitle(e.target.value);
              if (clean !== e.target.value) setTitle(clean);
            }}
            placeholder='Ex: "Transaction 5000 dollars"'
            style={ui.input}
          />
          <div style={ui.helper}>
            Règles: 4–80 caractères, lettres/chiffres/espaces +{" "}
            <code>- _ . ( )</code>.
          </div>
          {errors.title ? <div style={ui.error}>{errors.title}</div> : null}
        </div>

        {/* Recorder block */}
        <div style={ui.recordCard}>
          <div style={ui.recordHeader}>
            <div>
              <div style={ui.recordTitle}>Enregistrement live</div>
              <div style={ui.recordSub}>
                {recOpen
                  ? recStatus === "recording"
                    ? "Enregistrement en cours…"
                    : recStatus === "paused"
                    ? "En pause…"
                    : recStatus === "stopped"
                    ? "Enregistrement terminé."
                    : "Caméra prête."
                  : "Clique sur “Enregistrer une vidéo” pour ouvrir la caméra."}
              </div>
            </div>

            <div style={ui.row10}>
              {!recOpen ? (
                <button type="button" onClick={openRecorder} style={ui.btnPrimary}>
                  ● Enregistrer une vidéo
                </button>
              ) : (
                <>
                  {recStatus !== "recording" && recStatus !== "paused" ? (
                    <button type="button" onClick={startRecording} style={ui.btnPrimary}>
                      ● Démarrer
                    </button>
                  ) : null}

                  {recStatus === "recording" ? (
                    <button type="button" onClick={pauseRecording} style={ui.btnGhost}>
                      Pause
                    </button>
                  ) : null}

                  {recStatus === "paused" ? (
                    <button type="button" onClick={resumeRecording} style={ui.btnGhost}>
                      Reprendre
                    </button>
                  ) : null}

                  {(recStatus === "recording" || recStatus === "paused") ? (
                    <button type="button" onClick={stopRecording} style={ui.btnDark}>
                      Stop
                    </button>
                  ) : null}

                  <button type="button" onClick={closeRecorder} style={ui.btnLight}>
                    Fermer
                  </button>
                </>
              )}
            </div>
          </div>

          {recOpen ? (
            <div style={ui.recBody(isNarrow)}>
              {/* Live preview */}
              <div style={ui.videoPanel}>
                <div style={ui.panelTitle}>Live preview</div>
                <div style={ui.videoBox}>
                  <video ref={videoLiveRef} style={ui.video} playsInline muted autoPlay />
                </div>

                {recError ? <div style={ui.errorBox}>{recError}</div> : null}
              </div>

              {/* Summary + recorded */}
              <div style={ui.sidePanel}>
                <div style={ui.panelTitle}>Détails</div>

                <div style={ui.kv}>
                  <span style={ui.k}>Destinataire</span>
                  <span style={ui.v}>{selectedRecipient ? selectedRecipient.name : "—"}</span>
                </div>
                <div style={ui.kv}>
                  <span style={ui.k}>Montant</span>
                  <span style={ui.v}>{amount ? `$${amount}` : "—"}</span>
                </div>
                <div style={ui.kv}>
                  <span style={ui.k}>Titre</span>
                  <span style={ui.v}>{title || "—"}</span>
                </div>

                <div style={{ marginTop: 14 }}>
                  <div style={ui.panelTitleRow}>
                    <div style={ui.panelTitle} aria-hidden>
                      Vidéo enregistrée
                    </div>
                    {recordedUrl ? (
                      <button
                        type="button"
                        onClick={clearRecorded}
                        style={ui.btnMini}
                        title="Supprimer la vidéo enregistrée"
                      >
                        Effacer
                      </button>
                    ) : null}
                  </div>

                  {recordedUrl ? (
                    <>
                      <video style={ui.videoPlayback} src={recordedUrl} controls />
                      <a
                        href={recordedUrl}
                        download={`${sanitizeTitle(title) || "transaction"}.webm`}
                        style={ui.download}
                      >
                        Télécharger la vidéo (.webm)
                      </a>
                      <div style={ui.smallHint}>
                        (Plus tard : envoi du blob au backend via FormData.)
                      </div>
                    </>
                  ) : (
                    <div style={ui.muted}>Aucune vidéo enregistrée pour l’instant.</div>
                  )}
                </div>
              </div>
            </div>
          ) : null}
        </div>

        {/* Actions */}
        <div style={ui.actions}>
          <button type="submit" style={ui.btnDark}>
            Enregistrer (front)
          </button>
          <button type="button" onClick={onReset} style={ui.btnLight}>
            Réinitialiser
          </button>
        </div>
      </form>
    </div>
  );
}

// -------------------------
// UI styles (clean + responsive)
// -------------------------
const ui = {
  page: {
    maxWidth: 980,
    margin: "0 auto",
    padding: "18px 16px 32px",
    fontFamily:
      'ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, "Helvetica Neue", Arial',
    boxSizing: "border-box",
  },
  header: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: 16,
    marginBottom: 14,
  },
  h1: { margin: 0, fontSize: 26, letterSpacing: "-0.02em" },
  sub: { margin: "6px 0 0", color: "#5b6070" },
  badge: {
    padding: "6px 10px",
    borderRadius: 999,
    background: "#eef2ff",
    color: "#3730a3",
    fontWeight: 800,
    fontSize: 12,
    alignSelf: "center",
  },
  card: {
    background: "white",
    borderRadius: 18,
    padding: 18,
    border: "1px solid rgba(15, 23, 42, 0.08)",
    boxShadow: "0 12px 30px rgba(2, 6, 23, 0.06)",
    boxSizing: "border-box",
  },
  label: {
    display: "block",
    fontSize: 13,
    fontWeight: 800,
    marginBottom: 8,
    color: "#0f172a",
  },
  helper: { marginTop: 8, fontSize: 12, color: "#6b7280" },

  grid2: (isNarrow) => ({
    display: "grid",
    gridTemplateColumns: isNarrow ? "1fr" : "1.4fr 1fr",
    gap: 16,
    alignItems: "start",
  }),
  gridItem: { minWidth: 0 },

  stack8: { display: "grid", gap: 8 },

  input: {
    width: "100%",
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(15, 23, 42, 0.12)",
    outline: "none",
    background: "#fff",
    color: "#0f172a",
    boxSizing: "border-box",
  },

  // ✅ Fix select/option text color
  select: {
    width: "100%",
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(15, 23, 42, 0.12)",
    outline: "none",
    background: "#fff",
    color: "#0f172a",
    appearance: "auto",
    boxSizing: "border-box",
  },
  option: {
    color: "#0f172a",
    background: "#ffffff",
  },

  amountWrap: {
    display: "flex",
    alignItems: "center",
    borderRadius: 12,
    border: "1px solid rgba(15, 23, 42, 0.12)",
    overflow: "hidden",
    background: "white",
    width: "100%",
    boxSizing: "border-box",
  },
  currency: {
    padding: "10px 12px",
    background: "#f1f5f9",
    color: "#0f172a",
    fontWeight: 800,
    borderRight: "1px solid rgba(15, 23, 42, 0.10)",
    flex: "0 0 auto",
  },
  amount: {
    width: "100%",
    padding: "10px 12px",
    border: "none",
    outline: "none",
    color: "#0f172a",
    background: "#fff",
    boxSizing: "border-box",
  },

  recordCard: {
    marginTop: 18,
    borderRadius: 18,
    border: "1px solid rgba(15, 23, 42, 0.10)",
    background: "linear-gradient(180deg, rgba(2,6,23,0.02), rgba(2,6,23,0.00))",
    overflow: "hidden",
  },
  recordHeader: {
    padding: 16,
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: 12,
    flexWrap: "wrap",
    borderBottom: "1px solid rgba(15, 23, 42, 0.08)",
  },
  recordTitle: { fontSize: 14, fontWeight: 900, color: "#0f172a" },
  recordSub: { fontSize: 12, color: "#6b7280", marginTop: 2 },
  row10: { display: "flex", gap: 10, flexWrap: "wrap" },

  recBody: (isNarrow) => ({
    display: "grid",
    gridTemplateColumns: isNarrow ? "1fr" : "1.2fr 1fr",
    gap: 14,
    padding: 16,
    alignItems: "start",
  }),

  videoPanel: {
    minWidth: 0,
    borderRadius: 16,
    border: "1px dashed rgba(15, 23, 42, 0.22)",
    background: "rgba(255,255,255,0.75)",
    padding: 12,
    boxSizing: "border-box",
  },
  sidePanel: {
    minWidth: 0,
    borderRadius: 16,
    border: "1px solid rgba(15, 23, 42, 0.08)",
    background: "white",
    padding: 12,
    boxSizing: "border-box",
    alignContent: "start",
  },
  panelTitle: { fontSize: 12, fontWeight: 900, color: "#0f172a" },
  panelTitleRow: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 10,
    gap: 10,
  },

  videoBox: {
    marginTop: 10,
    borderRadius: 14,
    overflow: "hidden",
    background: "#0b1220",
    border: "1px solid rgba(15, 23, 42, 0.10)",
  },
  video: {
    width: "100%",
    height: 260,
    objectFit: "cover",
    display: "block",
  },

  kv: {
    display: "flex",
    justifyContent: "space-between",
    gap: 10,
    padding: "8px 0",
    borderBottom: "1px solid rgba(15, 23, 42, 0.06)",
  },
  k: { fontSize: 12, color: "#6b7280", fontWeight: 800 },
  v: { fontSize: 13, color: "#0f172a", fontWeight: 900 },

  videoPlayback: {
    width: "100%",
    marginTop: 8,
    borderRadius: 12,
    border: "1px solid rgba(15, 23, 42, 0.10)",
    background: "#0b1220",
  },
  download: {
    display: "inline-block",
    marginTop: 10,
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(15, 23, 42, 0.14)",
    background: "white",
    color: "#0f172a",
    fontWeight: 900,
    textDecoration: "none",
  },
  smallHint: { marginTop: 8, fontSize: 12, color: "#6b7280" },
  muted: { fontSize: 12, color: "#6b7280" },

  actions: { marginTop: 16, display: "flex", gap: 10, flexWrap: "wrap" },

  btnPrimary: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(124, 58, 237, 0.35)",
    background: "linear-gradient(90deg, rgba(124, 58, 237, 0.95), rgba(34, 197, 94, 0.85))",
    color: "white",
    fontWeight: 900,
    cursor: "pointer",
  },
  btnGhost: {
    padding: "10px 12px",
    borderRadius: 12,
    border: "1px solid rgba(15, 23, 42, 0.14)",
    background: "white",
    color: "#0f172a",
    fontWeight: 900,
    cursor: "pointer",
  },
  btnDark: {
    padding: "10px 14px",
    borderRadius: 12,
    border: "none",
    background: "#0f172a",
    color: "white",
    fontWeight: 900,
    cursor: "pointer",
  },
  btnLight: {
    padding: "10px 14px",
    borderRadius: 12,
    border: "1px solid rgba(15, 23, 42, 0.14)",
    background: "white",
    color: "#0f172a",
    fontWeight: 900,
    cursor: "pointer",
  },
  btnMini: {
    padding: "6px 10px",
    borderRadius: 10,
    border: "1px solid rgba(15, 23, 42, 0.14)",
    background: "white",
    color: "#0f172a",
    fontWeight: 900,
    cursor: "pointer",
    fontSize: 12,
  },

  error: { marginTop: 8, color: "#b91c1c", fontSize: 12, fontWeight: 800 },
  errorBox: {
    marginTop: 10,
    color: "#b91c1c",
    fontSize: 12,
    fontWeight: 800,
    background: "#fef2f2",
    border: "1px solid #fecaca",
    padding: "10px 12px",
    borderRadius: 12,
  },
};
