import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import MfaModal from "../components/MfaModal";
import { initiateGoogleOAuth, verifyMfaCode, resendMfaCode } from "../api/authApi";
import { FiMail, FiLock, FiEye, FiEyeOff } from "react-icons/fi";
import "../components/css/Login/Login.css";

export default function Login() {
  const navigate = useNavigate();
  const { login, setAuthData } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  // États pour le formulaire classique
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // États pour OAuth2 + MFA
  const [showMfaModal, setShowMfaModal] = useState(false);
  const [mfaSessionToken, setMfaSessionToken] = useState("");
  const [mfaUserEmail, setMfaUserEmail] = useState("");
  const [mfaUserName, setMfaUserName] = useState("");

  const googleIconUrl =
    "https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg";

  // Détecte le retour OAuth2 avec paramètres MFA
  useEffect(() => {
    const mfaParam = searchParams.get("mfa");
    const sessionParam = searchParams.get("session");
    const emailParam = searchParams.get("email");
    const nameParam = searchParams.get("name");
    const errorParam = searchParams.get("error");

    if (errorParam) {
      setError(decodeURIComponent(errorParam));
      // Nettoie les paramètres de l'URL
      setSearchParams({});
      return;
    }

    if (mfaParam === "true" && sessionParam) {
      // Affiche le modal MFA
      setMfaSessionToken(sessionParam);
      setMfaUserEmail(emailParam ? decodeURIComponent(emailParam) : "");
      setMfaUserName(nameParam ? decodeURIComponent(nameParam) : "");
      setShowMfaModal(true);
      // Nettoie les paramètres de l'URL
      setSearchParams({});
    }
  }, [searchParams, setSearchParams]);

  /**
   * Gère la connexion classique (email/password)
   */
  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const userData = await login(email, password);
      // Rediriger vers le dashboard admin si c'est un admin, sinon vers le dashboard normal
      if (userData.isAdmin) {
        navigate("/admin");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      setError(err?.message || "Erreur de connexion");
    } finally {
      setLoading(false);
    }
  }

  /**
   * Gère le clic sur le bouton "Se connecter avec Google"
   */
  function handleGoogleLogin() {
    setError("");
    initiateGoogleOAuth();
  }

  /**
   * Gère la vérification du code MFA
   */
  async function handleMfaVerify(code) {
    try {
      const result = await verifyMfaCode(mfaSessionToken, code);
      
      // Stocke les données d'authentification
      setAuthData({
        token: result.token,
        userId: result.userId,
        name: result.name,
        isAdmin: result.isAdmin,
      });

      // Ferme le modal
      setShowMfaModal(false);

      // Redirige vers le dashboard approprié
      if (result.isAdmin) {
        navigate("/admin");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      throw err; // Laisse le modal gérer l'erreur
    }
  }

  /**
   * Gère le renvoi du code MFA
   */
  async function handleMfaResend() {
    try {
      const result = await resendMfaCode(mfaSessionToken);
      // Met à jour le session token avec le nouveau
      setMfaSessionToken(result.sessionToken);
    } catch (err) {
      throw err; // Laisse le modal gérer l'erreur
    }
  }

  /**
   * Gère la fermeture du modal MFA
   */
  function handleMfaClose() {
    setShowMfaModal(false);
    setMfaSessionToken("");
    setMfaUserEmail("");
    setMfaUserName("");
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>Connexion</h1>
          <p>Bienvenue, veuillez vous connecter à votre compte</p>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label className="form-label">
              <FiMail className="label-icon" />
              Email
            </label>
            <input
              type="email"
              className="form-input"
              placeholder="votre@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              <FiLock className="label-icon" />
              Mot de passe
            </label>
            <div className="password-wrapper">
              <input
                type={showPwd ? "text" : "password"}
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowPwd(!showPwd)}
                aria-label={showPwd ? "Masquer le mot de passe" : "Afficher le mot de passe"}
              >
                {showPwd ? <FiEyeOff /> : <FiEye />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn-submit"
            disabled={loading || !email || password.length < 4}
          >
            {loading ? "Connexion en cours..." : "Se connecter"}
          </button>
        </form>

        <div className="divider">
          <span>OU</span>
        </div>

        <button 
          className="btn-google" 
          type="button"
          onClick={handleGoogleLogin}
        >
          <img src={googleIconUrl} alt="Google" className="google-icon" />
          Continuer avec Google
        </button>
      </div>

      {/* MFA MODAL */}
      <MfaModal
        isOpen={showMfaModal}
        onClose={handleMfaClose}
        onVerify={handleMfaVerify}
        onResend={handleMfaResend}
        userEmail={mfaUserEmail}
        userName={mfaUserName}
      />
    </div>
  );
}
