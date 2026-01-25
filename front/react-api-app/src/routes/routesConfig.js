import Home from "../pages/Home";
import About from "../pages/About";
import Contact from "../pages/Contact";
import Order from "../pages/Order";

// Routes affichées dans la Navbar (layout avec sidebar) — Login exclu, page d'entrée isolée
export const routesConfig = [
  { path: "/dashboard", label: "Home", component: Home, end: true },
  { path: "/about", label: "About", component: About },
  { path: "/contact", label: "Contact", component: Contact },
  { path: "/order", label: "Order", component: Order },
];