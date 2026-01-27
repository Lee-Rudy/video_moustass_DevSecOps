import Home from "../pages/Home";
import Dashboard from "../pages/Dashboard";
import ListOrder from "../pages/ListOrder";
import Logs from "../pages/Logs";
import Order from "../pages/Order";
import Inscription from "../pages/Inscription";
import NotificationsOrdre from "../pages/NotificationsOrdre";

// Routes affichées dans la Navbar (layout avec sidebar) — Login exclu, page d'entrée isolée
// adminOnly: true = réservé aux admins
// userOnly: true = réservé aux utilisateurs normaux (pas admins)
export const routesConfig = [
  { path: "/dashboard", label: "Home", component: Home, end: true },
  { path: "/admin", label: "Dashboard Admin", component: Dashboard, adminOnly: true },
  { path: "/inscription", label: "Créer utilisateur", component: Inscription, adminOnly: true },
  { path: "/logs", label: "Logs", component: Logs, adminOnly: true },
  { path: "/notifications", label: "Notifications Ordre", component: NotificationsOrdre, userOnly: true },
  { path: "/listOrder", label: "Liste d'ordre", component: ListOrder },
  { path: "/order", label: "Order", component: Order },
];