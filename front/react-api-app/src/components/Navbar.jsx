import { NavLink } from "react-router-dom";
import { routesConfig } from "../routes/routesConfig";
import "./css/Navbar/Navbar.css";

export default function Navbar() {
  return (
    <aside className="sidebar">
      <div className="sidebar__title">Dashboard</div>

      <nav className="sidebar__nav">
        {routesConfig.map((r) => (
          <NavLink
            key={r.path}
            to={r.path}
            end={r.end}
            className={({ isActive }) =>
              `sidebar__link ${isActive ? "active" : ""}`
            }
          >
            {r.label}
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
