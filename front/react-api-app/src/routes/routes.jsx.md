import { createBrowserRouter } from "react-router-dom";
import App from "../App";
import Home from "../pages/Home";
import About from "../pages/About";
import Contact from "../pages/Contact";


export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,        // Layout avec Navbar
    children: [
      { index: true, element: <Home /> },  // "/"
      { path: "about", element: <About /> }, // "/about"
      { path: "contact", element: <Contact /> } // "/about"

    ],
  },
]);
