import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { FiBell, FiEye, FiCheckCircle } from "react-icons/fi";
import "../components/css/Dashboard/Dashboard.css";

const styles = {
  page: {
    maxWidth: 1400,
    margin: "0 auto",
    padding: "18px 16px 32px",
    fontFamily: "ui-sans-serif, system-ui, sans-serif",
  },
  h1: { margin: "0 0 8px", fontSize: 24, fontWeight: 800 },
  sub: { margin: 0, color: "#64748b", fontSize: 14 },
  empty: {
    textAlign: "center",
    padding: 48,
    color: "#64748b",
    background: "#f8fafc",
    borderRadius: 12,
    border: "1px solid #e2e8f0",
  },
  emptyIcon: {
    fontSize: 48,
    marginBottom: 16,
    opacity: 0.5,
  },
  notificationCard: {
    background: "white",
    border: "1px solid #e2e8f0",
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    display: "flex",
    alignItems: "center",
    gap: 16,
    transition: "all 0.2s",
  },
  notificationNew: {
    borderLeft: "4px solid #6366f1",
    background: "#f8fafc",
  },
  badge: {
    display: "inline-block",
    padding: "4px 12px",
    borderRadius: 6,
    fontSize: 11,
    fontWeight: 700,
  },
  badgeNew: {
    background: "#fef3c7",
    color: "#92400e",
  },
  badgeRead: {
    background: "#dcfce7",
    color: "#166534",
  },
};

export default function NotificationsOrdre() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadNotifications = useCallback(() => {
    // Simulation de notifications (en production, utiliser une vraie API)
    const mockNotifications = [
      {
        id: 1,
        type: "ORDRE_RECU",
        expediteur: "Admin",
        message: "Vous avez reçu une nouvelle demande d'ordre",
        ordreId: 123,
        date: new Date(Date.now() - 1000 * 60 * 30).toISOString(), // 30 min ago
        isNew: true,
      },
      {
        id: 2,
        type: "ORDRE_RECU",
        expediteur: "Alice",
        message: "Alice vous a envoyé un ordre",
        ordreId: 122,
        date: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), // 2h ago
        isNew: true,
      },
      {
        id: 3,
        type: "ORDRE_VALIDE",
        expediteur: "Système",
        message: "Votre ordre #121 a été validé",
        ordreId: 121,
        date: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(), // 1 day ago
        isNew: false,
      },
    ];

    // Filtrer pour ne garder que les notifications de l'utilisateur connecté
    // En production, l'API filtrerait côté serveur
    setTimeout(() => {
      setNotifications(mockNotifications);
      setLoading(false);
    }, 500);
  }, [user?.userId]);

  useEffect(() => {
    if (!user || user.isAdmin) {
      navigate("/dashboard", { replace: true });
      return;
    }
    loadNotifications();
  }, [user, navigate, loadNotifications]);

  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays === 1) return "Hier";
    if (diffDays < 7) return `Il y a ${diffDays} jours`;
    return date.toLocaleDateString("fr-FR");
  };

  const handleViewDetails = (ordreId) => {
    // Marquer comme lu (en production, appeler l'API)
    setNotifications((prev) =>
      prev.map((n) => (n.ordreId === ordreId ? { ...n, isNew: false } : n))
    );
    navigate("/listOrder");
  };

  const markAsRead = (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isNew: false } : n))
    );
  };

  const newCount = notifications.filter((n) => n.isNew).length;

  if (!user || user.isAdmin) return null;

  return (
    <div style={styles.page}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 24 }}>
        <div>
          <h1 style={styles.h1}>
            <FiBell style={{ display: "inline", marginRight: 8 }} />
            Notifications Ordre
          </h1>
          <p style={styles.sub}>
            Historique de vos demandes d'ordre et notifications
          </p>
        </div>
        {newCount > 0 && (
          <div
            style={{
              background: "#ef4444",
              color: "white",
              borderRadius: "50%",
              width: 32,
              height: 32,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontWeight: 700,
              fontSize: 14,
            }}
          >
            {newCount}
          </div>
        )}
      </div>

      {loading && (
        <div style={{ textAlign: "center", padding: 48, color: "#64748b" }}>
          Chargement des notifications...
        </div>
      )}

      {!loading && notifications.length === 0 && (
        <div style={styles.empty}>
          <div style={styles.emptyIcon}>🔔</div>
          <h3 style={{ margin: "0 0 8px", fontSize: 18, color: "#0f172a" }}>
            Aucune notification
          </h3>
          <p style={{ margin: 0, fontSize: 14 }}>
            Vous n'avez pas encore reçu de notifications d'ordre
          </p>
        </div>
      )}

      {!loading && notifications.length > 0 && (
        <div>
          {notifications.map((notif) => (
            <div
              key={notif.id}
              style={{
                ...styles.notificationCard,
                ...(notif.isNew ? styles.notificationNew : {}),
              }}
            >
              <div
                style={{
                  fontSize: 24,
                  color: notif.isNew ? "#6366f1" : "#94a3b8",
                }}
              >
                <FiBell />
              </div>
              <div style={{ flex: 1 }}>
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 8,
                    marginBottom: 4,
                  }}
                >
                  <strong style={{ fontSize: 14, color: "#0f172a" }}>
                    {notif.message}
                  </strong>
                  <span
                    style={{
                      ...styles.badge,
                      ...(notif.isNew ? styles.badgeNew : styles.badgeRead),
                    }}
                  >
                    {notif.isNew ? "NOUVEAU" : "LU"}
                  </span>
                </div>
                <div style={{ fontSize: 13, color: "#64748b" }}>
                  De : {notif.expediteur} • Ordre #{notif.ordreId} • {formatDate(notif.date)}
                </div>
              </div>
              <div style={{ display: "flex", gap: 8 }}>
                {notif.isNew && (
                  <button
                    onClick={() => markAsRead(notif.id)}
                    className="btn btn-secondary"
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: 6,
                      padding: "8px 16px",
                      fontSize: 13,
                    }}
                    title="Marquer comme lu"
                  >
                    <FiCheckCircle size={14} />
                    Marquer lu
                  </button>
                )}
                <button
                  onClick={() => handleViewDetails(notif.ordreId)}
                  className="btn btn-primary"
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 6,
                    padding: "8px 16px",
                    fontSize: 13,
                  }}
                >
                  <FiEye size={14} />
                  Voir détails
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
