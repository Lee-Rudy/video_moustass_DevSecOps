import { Outlet } from "react-router-dom";
import Navbar from "./components/Navbar";
import "./App.css";

export default function App() {
  return (
    <div className="layout">
      <Navbar />
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}


// Outlet = l’endroit où React Router va afficher la page courante.


// import Header from './components/Header'
// import Nav

// export default function App() 
// {
//   return (
//     <>
//       <Header />
//       <main style={{ padding: 12 }}>
//         <h1>Accueil</h1>
//         <p>Hello world</p>
//       </main>
//     </>
//   );
// }