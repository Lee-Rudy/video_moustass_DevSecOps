import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "../components/css/Login/Login.css";

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const googleIconUrl =
    "https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg";

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (err) {
      setError(err?.message || "Erreur de connexion");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-layout">
      {/* LEFT */}
      <div className="login-page">
        <div className="login-card">
          <div className="login-header">
            <div className="logo">Moustass Video</div>
            <h1>Content de te revoir</h1>
            <p>Connecte-toi pour continuer</p>
          </div>

          <button className="google-btn">
            <img src={googleIconUrl} alt="Google" />
            Se connecter avec Google
          </button>

          <div className="divider">OU</div>

          <form onSubmit={handleSubmit}>
            {error && <p className="login-error" style={{ color: "#c00", marginBottom: 8 }}>{error}</p>}
            <label>Email</label>
            <input
              type="email"
              placeholder="ex: brunernoel@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <label>Mot de passe</label>
            <div className="password-field">
              <input
                type={showPwd ? "text" : "password"}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                className="toggle-btn"
                onClick={() => setShowPwd(!showPwd)}
              >
                {showPwd ? "Masquer" : "Afficher"}
              </button>
            </div>

            <button
              className="submit-btn"
              type="submit"
              disabled={loading || !email || password.length < 4}
            >
              {loading ? "Connexion..." : "Se connecter"}
            </button>
          </form>

          <p className="footer-text">
            Pas encore de compte ? <a href="/register">Créer un compte</a>
          </p>
        </div>
      </div>

      {/* RIGHT IMAGE */}
      <div className="login-image" />
    </div>
  );
}
