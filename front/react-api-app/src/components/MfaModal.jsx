import { useState, useEffect, useRef } from "react";
import "./css/MfaModal/MfaModal.css";

/**
 * Composant Modal pour la saisie du code MFA
 * Affiche un modal élégant pour entrer le code de vérification à 6 chiffres
 */
export default function MfaModal({ isOpen, onClose, onVerify, userEmail, userName, onResend }) {
  const [code, setCode] = useState(["", "", "", "", "", ""]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [resendLoading, setResendLoading] = useState(false);
  const [resendSuccess, setResendSuccess] = useState(false);
  const inputRefs = useRef([]);

  // Focus sur le premier input quand le modal s'ouvre
  useEffect(() => {
    if (isOpen && inputRefs.current[0]) {
      inputRefs.current[0].focus();
    }
  }, [isOpen]);

  // Reset le code et l'erreur quand le modal se ferme
  useEffect(() => {
    if (!isOpen) {
      setCode(["", "", "", "", "", ""]);
      setError("");
      setResendSuccess(false);
    }
  }, [isOpen]);

  /**
   * Gère le changement de valeur dans un input
   */
  const handleChange = (index, value) => {
    // Ne permet que les chiffres
    if (value && !/^\d$/.test(value)) return;

    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);
    setError("");

    // Focus automatique sur l'input suivant
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  /**
   * Gère la touche Backspace pour revenir à l'input précédent
   */
  const handleKeyDown = (index, e) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  /**
   * Gère le collage de code
   */
  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text").trim();
    
    // Vérifie que le texte collé contient 6 chiffres
    if (/^\d{6}$/.test(pastedData)) {
      const newCode = pastedData.split("");
      setCode(newCode);
      setError("");
      // Focus sur le dernier input
      inputRefs.current[5]?.focus();
    }
  };

  /**
   * Soumet le code MFA pour vérification
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    const fullCode = code.join("");

    if (fullCode.length !== 6) {
      setError("Veuillez entrer les 6 chiffres du code");
      return;
    }

    setLoading(true);
    setError("");

    try {
      await onVerify(fullCode);
    } catch (err) {
      setError(err?.message || "Code invalide ou expiré");
      // Reset le code en cas d'erreur
      setCode(["", "", "", "", "", ""]);
      inputRefs.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  };

  /**
   * Renvoie un nouveau code MFA
   */
  const handleResend = async () => {
    setResendLoading(true);
    setError("");
    setResendSuccess(false);

    try {
      await onResend();
      setResendSuccess(true);
      setTimeout(() => setResendSuccess(false), 3000);
    } catch (err) {
      setError(err?.message || "Échec du renvoi du code");
    } finally {
      setResendLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="mfa-modal-overlay" onClick={onClose}>
      <div className="mfa-modal-content" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="mfa-modal-header">
          <div className="mfa-icon">🔐</div>
          <h2>Vérification en deux étapes</h2>
          <p className="mfa-subtitle">
            Un code de vérification a été envoyé à
            <br />
            <strong>{userEmail}</strong>
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="mfa-form">
          <div className="mfa-code-inputs" onPaste={handlePaste}>
            {code.map((digit, index) => (
              <input
                key={index}
                ref={(el) => (inputRefs.current[index] = el)}
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                onChange={(e) => handleChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                className="mfa-code-input"
                disabled={loading}
              />
            ))}
          </div>

          {error && <p className="mfa-error">{error}</p>}
          {resendSuccess && <p className="mfa-success">Code renvoyé avec succès !</p>}

          <button
            type="submit"
            className="mfa-verify-btn"
            disabled={loading || code.join("").length !== 6}
          >
            {loading ? "Vérification..." : "Vérifier"}
          </button>

          <div className="mfa-resend">
            <p>Vous n'avez pas reçu le code ?</p>
            <button
              type="button"
              className="mfa-resend-btn"
              onClick={handleResend}
              disabled={resendLoading}
            >
              {resendLoading ? "Envoi..." : "Renvoyer le code"}
            </button>
          </div>
        </form>

        {/* Close button */}
        <button className="mfa-close-btn" onClick={onClose} type="button">
          ×
        </button>
      </div>
    </div>
  );
}
