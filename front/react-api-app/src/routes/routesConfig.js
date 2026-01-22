import Home from "../pages/Home";
import About from "../pages/About";
import Contact from "../pages/Contact";
import Login from "../pages/Login";


export const routesConfig = [
  { path: "/", label: "Home", component: Home, end: true },
  { path: "/about", label: "About", component: About },
  { path: "/contact", label: "Contact", component: Contact },
  { path: "/login", label: "login", component: Login },
];
