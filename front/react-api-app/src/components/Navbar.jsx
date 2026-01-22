import { NavLink } from "react-router-dom";
import { routesConfig } from "../routes/routesConfig";

export default function Navbar() {
  const base = { padding: "8px 12px", textDecoration: "none", borderRadius: 8 };

  return (
    <nav style={{ display: "flex", gap: 10, padding: 12, borderBottom: "1px solid #ddd" }}>
      {routesConfig.map((r) => (
        <NavLink
          key={r.path}
          to={r.path}
          end={r.end}
          style={({ isActive }) => ({
            ...base,
            background: isActive ? "#eee" : "transparent",
          })}
        >
          {r.label}
        </NavLink>
      ))}
    </nav>
  );
}
