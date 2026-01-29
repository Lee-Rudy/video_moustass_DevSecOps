import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { FiUser, FiKey } from "react-icons/fi";
import "../components/css/Dashboard/Dashboard.css";

export default function Home() {
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (!user) navigate("/", { replace: true });
  }, [user, navigate]);

  if (!user) return null;

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Tableau de bord</h1>
        <p className="dashboard-subtitle">
          Bienvenue {user.name}, voici vos informations de session
        </p>
      </div>

      <div className="dashboard-section">
        <h2 className="section-title">
          <FiUser style={{ fontSize: 20 }} />
          Informations utilisateur
        </h2>

        <div style={{ display: "grid", gap: "16px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "12px", padding: "16px", background: "#f9fafb", borderRadius: "10px" }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: "13px", color: "#6b7280", fontWeight: 600, marginBottom: "4px" }}>
                Nom d'utilisateur
              </div>
              <div style={{ fontSize: "15px", color: "#111827", fontWeight: 600 }}>
                {user.name || "(non renseigné)"}
              </div>
            </div>
          </div>

          <div style={{ padding: "16px", background: "#f9fafb", borderRadius: "10px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "12px" }}>
              <FiKey style={{ fontSize: 16, color: "#6b7280" }} />
              <div style={{ fontSize: "13px", color: "#6b7280", fontWeight: 600 }}>
                Token JWT de session
              </div>
            </div>
            <pre style={{ 
              overflow: "auto", 
              wordBreak: "break-all", 
              fontSize: "11px", 
              fontFamily: "Monaco, Menlo, Consolas, monospace",
              background: "#ffffff",
              padding: "12px",
              borderRadius: "8px",
              border: "1px solid #e5e7eb",
              color: "#4b5563",
              lineHeight: 1.6,
              margin: 0
            }}>
              {user.token}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
}
