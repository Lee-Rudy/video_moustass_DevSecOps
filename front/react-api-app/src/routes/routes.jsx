import { createBrowserRouter } from "react-router-dom";
import App from "../App";
import Login from "../pages/Login";
import { routesConfig } from "./routesConfig";

export const router = createBrowserRouter([
  // Page d'entrée "/" : Login seul, sans Navbar
  {
    path: "/",
    element: <Login />,
  },
  // Layout avec Navbar pour les autres pages
  {
    path: "/",
    element: <App />,
    children: routesConfig.map((r) => ({
      path: r.path.slice(1),
      element: <r.component />,
    })),
  },
]);





// import { createBrowserRouter } from "react-router-dom";
// import App from "../App";
// import { routesConfig } from "./routesConfig";

// export const router = createBrowserRouter([
//   {
//     path: "/",
//     element: <App />, // layout commun (Navbar + Outlet)
//     children: routesConfig.map((r) => ({
//       path: r.path === "/" ? undefined : r.path.replace("/", ""), // "about"
//       index: r.path === "/",
//       element: r.element, //ici est la différence
//     })),
//   },
// ]);
