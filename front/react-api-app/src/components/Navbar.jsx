import { NavLink } from "react-router-dom";
import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { routesConfig } from "../routes/routesConfig";
import "./css/Navbar/Navbar.css";

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
      <div className="sidebar__title">Dashboard</div>

      <nav className="sidebar__nav">
        {visibleRoutes.map((r) => (
          <NavLink
            key={r.path}
            to={r.path}
            end={r.end}
            className={({ isActive }) =>
              `sidebar__link ${isActive ? "active" : ""}`
            }
          >
            {r.label}
            {r.path === "/notifications" && unreadCount > 0 && (
              <span className="notification-badge">{unreadCount}</span>
            )}
          </NavLink>
        ))}
        <NavLink
          to="/"
          className="sidebar__link sidebar__link--logout"
        >
          Déconnexion
        </NavLink>
      </nav>
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
