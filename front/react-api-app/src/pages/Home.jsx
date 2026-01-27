import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Home() {
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (!user) navigate("/", { replace: true });
  }, [user, navigate]);

  if (!user) return null;

  return (
    <div>
      <h1>Bienvenue sur la page Home</h1>
      <section className="user-info" style={{ marginTop: 24, padding: 16, background: "#f5f5f5", borderRadius: 8 }}>
        <h2>Utilisateur connecté</h2>
        <p><strong>Nom :</strong> {user.name || "(non renseigné)"}</p>
        <p><strong>Token JWT :</strong></p>
        <pre style={{ overflow: "auto", wordBreak: "break-all", fontSize: 12 }}>{user.token}</pre>
      </section>
    </div>
  );
}