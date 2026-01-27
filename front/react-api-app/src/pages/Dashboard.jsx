import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getAllUsers, deleteUser } from "../api/authApi";
import { FiUserPlus, FiFileText, FiRefreshCw, FiTrash2 } from "react-icons/fi";
import "../components/css/Dashboard/Dashboard.css";

export default function Dashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState(null);

  const loadUsers = useCallback(() => {
    if (!user?.token) return;
    setLoading(true);
    setError("");
    getAllUsers(user.token)
      .then((data) => setUsers(Array.isArray(data) ? data : []))
      .catch((e) => setError(e.message || "Erreur chargement utilisateurs"))
      .finally(() => setLoading(false));
  }, [user?.token]);

  useEffect(() => {
    if (!user) {
      navigate("/", { replace: true });
      return;
    }
    if (!user.isAdmin) {
      navigate("/dashboard", { replace: true });
      return;
    }
    loadUsers();
  }, [user, navigate, loadUsers]);

  const handleDelete = async (userId, userName) => {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${userName}" ?`)) {
      return;
    }

    setDeletingId(userId);
    setError("");
    try {
      await deleteUser(userId, user.token);
      setUsers((prev) => prev.filter((u) => u.idUsers !== userId));
    } catch (e) {
      setError(e.message || "Erreur suppression utilisateur");
    } finally {
      setDeletingId(null);
    }
  };

  if (!user || !user.isAdmin) return null;

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Dashboard Administrateur</h1>
        <p className="dashboard-subtitle">
          Bienvenue {user.name}, vous avez accès aux fonctionnalités d'administration
        </p>
      </div>

      {/* Actions rapides */}
      <div className="dashboard-section">
        <h2 className="section-title">Actions rapides</h2>
        <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
          <button
            className="btn btn-primary"
            onClick={() => navigate("/inscription")}
            style={{ display: 'flex', alignItems: 'center', gap: 8 }}
          >
            <FiUserPlus size={18} />
            Créer un utilisateur
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => navigate("/logs")}
            style={{ display: 'flex', alignItems: 'center', gap: 8 }}
          >
            <FiFileText size={18} />
            Consulter les logs
          </button>
          <button
            className="btn btn-secondary"
            onClick={loadUsers}
            disabled={loading}
            style={{ display: 'flex', alignItems: 'center', gap: 8 }}
          >
            <FiRefreshCw size={18} />
            Actualiser
          </button>
        </div>
      </div>

      {/* Liste des utilisateurs */}
      <div className="dashboard-section">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
          <h2 className="section-title" style={{ margin: 0 }}>
            Gestion des utilisateurs
          </h2>
          <span style={{ fontSize: 14, color: "#64748b" }}>
            {loading ? "…" : `${users.length} utilisateur${users.length > 1 ? "s" : ""}`}
          </span>
        </div>

        {error && <div className="message error">{error}</div>}

        {loading && (
          <div className="loading-state">Chargement des utilisateurs…</div>
        )}

        {!loading && !error && users.length === 0 && (
          <div className="empty-state">
            <div className="empty-state-icon">👥</div>
            <p>Aucun utilisateur trouvé</p>
          </div>
        )}

        {!loading && !error && users.length > 0 && (
          <div className="users-table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nom</th>
                  <th>Email</th>
                  <th>Rôle</th>
                  <th>Clé publique</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.idUsers}>
                    <td style={{ fontFamily: "monospace", color: "#64748b" }}>
                      #{u.idUsers}
                    </td>
                    <td>
                      <div className="user-name">{u.name}</div>
                    </td>
                    <td>
                      <div className="user-email">{u.mail}</div>
                    </td>
                    <td>
                      <span className={`badge ${u.isAdmin ? "admin" : "user"}`}>
                        {u.isAdmin ? "Administrateur" : "Utilisateur"}
                      </span>
                    </td>
                    <td>
                      <span style={{ 
                        fontFamily: "monospace", 
                        fontSize: 11, 
                        color: "#64748b",
                        display: "block",
                        maxWidth: 200,
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap"
                      }} title={u.publicKey}>
                        {u.publicKey ? `${u.publicKey.slice(0, 30)}...` : "—"}
                      </span>
                    </td>
                    <td>
                      <div className="actions-cell">
                        <button
                          className="btn btn-danger"
                          onClick={() => handleDelete(u.idUsers, u.name)}
                          disabled={deletingId === u.idUsers || u.isAdmin}
                          title={u.isAdmin ? "Impossible de supprimer un admin" : "Supprimer cet utilisateur"}
                          style={{ display: 'flex', alignItems: 'center', gap: 6 }}
                        >
                          <FiTrash2 size={14} />
                          {deletingId === u.idUsers ? "Suppression..." : "Supprimer"}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
