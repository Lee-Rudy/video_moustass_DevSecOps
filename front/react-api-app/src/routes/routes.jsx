import { createBrowserRouter } from "react-router-dom";
import App from "../App";
import { routesConfig } from "./routesConfig";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: routesConfig.map((r) => ({
      path: r.path === "/" ? undefined : r.path.replace("/", ""),
      index: r.path === "/",
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
