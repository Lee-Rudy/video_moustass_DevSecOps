import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { createUser } from "../api/authApi";
import { FiUser, FiMail, FiLock, FiEye, FiEyeOff, FiUserCheck } from "react-icons/fi";
import "../components/css/Dashboard/Dashboard.css";

export default function Inscription() {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    name: "",
    mail: "",
    psw: "",
    confirmPsw: "",
    isAdmin: false,
  });
  const [showPwd, setShowPwd] = useState(false);
  const [showConfirmPwd, setShowConfirmPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const validateForm = () => {
    if (!formData.name.trim()) {
      setError("Le nom est requis");
      return false;
    }
    if (!formData.mail.trim()) {
      setError("L'email est requis");
      return false;
    }
    if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/.test(formData.mail)) {
      setError("L'email n'est pas valide");
      return false;
    }
    if (formData.psw.length < 8) {
      setError("Le mot de passe doit contenir au moins 8 caractères");
      return false;
    }
    if (!/[A-Z]/.test(formData.psw)) {
      setError("Le mot de passe doit contenir au moins une lettre majuscule");
      return false;
    }
    if (formData.psw !== formData.confirmPsw) {
      setError("Les mots de passe ne correspondent pas");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!validateForm()) return;

    setLoading(true);
    try {
      const userData = {
        name: formData.name.trim(),
        mail: formData.mail.trim().toLowerCase(),
        psw: formData.psw,
        isAdmin: formData.isAdmin,
      };

      await createUser(userData, user.token);
      setSuccess(`Utilisateur "${formData.name}" créé avec succès !`);
      
      // Réinitialiser le formulaire
      setFormData({
        name: "",
        mail: "",
        psw: "",
        confirmPsw: "",
        isAdmin: false,
      });
    } catch (err) {
      setError(err?.message || "Erreur lors de la création de l'utilisateur");
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setFormData({
      name: "",
      mail: "",
      psw: "",
      confirmPsw: "",
      isAdmin: false,
    });
    setError("");
    setSuccess("");
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Créer un utilisateur</h1>
        <p className="dashboard-subtitle">
          Remplissez le formulaire pour créer un nouvel utilisateur
        </p>
      </div>

      <div className="dashboard-section">
        {error && <div className="message error">{error}</div>}
        {success && <div className="message success">{success}</div>}

        <form onSubmit={handleSubmit} className="inscription-form">
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">
                <FiUser style={{ display: 'inline', marginRight: 6 }} />
                Nom <span className="required">*</span>
              </label>
              <input
                type="text"
                name="name"
                className="form-input"
                placeholder="ex: Alice"
                value={formData.name}
                onChange={handleChange}
                disabled={loading}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                <FiMail style={{ display: 'inline', marginRight: 6 }} />
                Email <span className="required">*</span>
              </label>
              <input
                type="email"
                name="mail"
                className="form-input"
                placeholder="ex: alice@gmail.com"
                value={formData.mail}
                onChange={handleChange}
                disabled={loading}
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">
                <FiLock style={{ display: 'inline', marginRight: 6 }} />
                Mot de passe <span className="required">*</span>
              </label>
              <div className="password-field">
                <input
                  type={showPwd ? "text" : "password"}
                  name="psw"
                  className="form-input"
                  placeholder="••••••••"
                  value={formData.psw}
                  onChange={handleChange}
                  disabled={loading}
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPwd(!showPwd)}
                  disabled={loading}
                  title={showPwd ? "Masquer" : "Afficher"}
                >
                  {showPwd ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
              <small style={{ fontSize: 12, color: "#64748b" }}>
                Min. 8 caractères avec au moins 1 majuscule
              </small>
            </div>

            <div className="form-group">
              <label className="form-label">
                <FiLock style={{ display: 'inline', marginRight: 6 }} />
                Confirmer le mot de passe <span className="required">*</span>
              </label>
              <div className="password-field">
                <input
                  type={showConfirmPwd ? "text" : "password"}
                  name="confirmPsw"
                  className="form-input"
                  placeholder="••••••••"
                  value={formData.confirmPsw}
                  onChange={handleChange}
                  disabled={loading}
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowConfirmPwd(!showConfirmPwd)}
                  disabled={loading}
                  title={showConfirmPwd ? "Masquer" : "Afficher"}
                >
                  {showConfirmPwd ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
            </div>
          </div>

          <div className="form-group full-width">
            <div className="form-checkbox-group">
              <input
                type="checkbox"
                name="isAdmin"
                id="isAdmin"
                className="form-checkbox"
                checked={formData.isAdmin}
                onChange={handleChange}
                disabled={loading}
              />
              <label htmlFor="isAdmin" className="form-label" style={{ margin: 0, display: 'flex', alignItems: 'center', gap: 6 }}>
                <FiUserCheck />
                Définir comme administrateur
              </label>
            </div>
            <small style={{ fontSize: 12, color: "#64748b", marginLeft: 26 }}>
              Par défaut, les utilisateurs sont créés sans privilèges administrateur
            </small>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleReset}
              disabled={loading}
            >
              Réinitialiser
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "Création en cours..." : "Créer l'utilisateur"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}