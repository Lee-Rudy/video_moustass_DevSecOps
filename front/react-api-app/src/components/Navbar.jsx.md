import { NavLink } from "react-router-dom";

export default function Navbar() {
  const base = { padding: "8px 12px", textDecoration: "none", borderRadius: 8 };

  return (
    <nav style={{ display: "flex", gap: 10, padding: 12, borderBottom: "1px solid #ddd" }}>
      <NavLink
        to="/"
        end
        style={({ isActive }) => ({
          ...base,
          background: isActive ? "#eee" : "transparent",
        })}
      >
        Home
      </NavLink>

      <NavLink
        to="/about"
        style={({ isActive }) => ({
          ...base,
          background: isActive ? "#eee" : "transparent",
        })}
      >
        About
      </NavLink>

      <NavLink
        to="/contact"
        style={({ isActive }) => ({
          ...base,
          background: isActive ? "#eee" : "transparent",
        })}
      >
        About
      </NavLink>
    </nav>
  );
}
