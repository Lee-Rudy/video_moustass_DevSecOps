import { NavLink } from "react-router-dom";
import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { routesConfig } from "../routes/routesConfig";
import { 
  FiHome, 
  FiUsers, 
  FiUserPlus, 
  FiFileText, 
  FiBell, 
  FiList, 
  FiPlusCircle,
  FiLogOut 
} from "react-icons/fi";
import "./css/Navbar/Navbar.css";

// Mapping des icônes pour chaque route
const routeIcons = {
  "/dashboard": FiHome,
  "/admin": FiUsers,
  "/inscription": FiUserPlus,
  "/logs": FiFileText,
  "/notifications": FiBell,
  "/listOrder": FiList,
  "/order": FiPlusCircle,
};

export default function Navbar() {
  const { user } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  // Filtrer les routes selon le rôle de l'utilisateur
  const visibleRoutes = routesConfig.filter((r) => {
    if (r.adminOnly) {
      return user?.isAdmin === true;
    }
    if (r.userOnly) {
      return user?.isAdmin === false;
    }
    return true;
  });

  // Simuler le chargement du nombre de notifications non lues
  // En production, cela viendrait d'une API
  useEffect(() => {
    if (user && !user.isAdmin) {
      // Simuler 2 notifications non lues
      setUnreadCount(2);
    }
  }, [user]);

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="sidebar-logo">
          <div className="logo-icon">MV</div>
          <div className="logo-text">
            <div className="logo-title">Moustass Video</div>
            <div className="logo-subtitle">Dashboard</div>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {visibleRoutes.map((r) => {
          const Icon = routeIcons[r.path] || FiHome;
          return (
            <NavLink
              key={r.path}
              to={r.path}
              end={r.end}
              className={({ isActive }) =>
                `nav-link ${isActive ? "active" : ""}`
              }
            >
              <Icon className="nav-icon" />
              <span className="nav-label">{r.label}</span>
              {r.path === "/notifications" && unreadCount > 0 && (
                <span className="notification-badge">{unreadCount}</span>
              )}
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <NavLink
          to="/"
          className="nav-link logout-link"
        >
          <FiLogOut className="nav-icon" />
          <span className="nav-label">Déconnexion</span>
        </NavLink>
      </div>
    </aside>
  );
}







//old version 

// import { NavLink } from "react-router-dom";
// import { routesConfig } from "../routes/routesConfig";

// export default function Navbar() {
//   const base = { padding: "8px 12px", textDecoration: "none", borderRadius: 8 };

//   return (
//     <nav style={{ display: "flex", gap: 10, padding: 12, borderBottom: "1px solid #ddd" }}>
//       {routesConfig.map((r) => (
//         <NavLink
//           key={r.path}
//           to={r.path}
//           end={r.end}
//           style={({ isActive }) => ({
//             ...base,
//             background: isActive ? "#eee" : "transparent",
//           })}
//         >
//           {r.label}
//         </NavLink>
//       ))}
//     </nav>
//   );
// }
