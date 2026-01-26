import Home from "../pages/Home";
import ListOrder from "../pages/ListOrder";
import Logs from "../pages/Logs";
import Order from "../pages/Order";

// Routes affichées dans la Navbar (layout avec sidebar) — Login exclu, page d'entrée isolée
export const routesConfig = [
  { path: "/dashboard", label: "Home", component: Home, end: true },
  { path: "/listOrder", label: "liste d'ordre", component: ListOrder },
  { path: "/logs", label: "Logs", component: Logs },
  { path: "/order", label: "Order", component: Order },
];