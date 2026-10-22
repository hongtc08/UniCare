import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./global.css";
import "./App.css";
import App from "./App.jsx";
import { removeLegacyDemoData } from "./utils/session";

removeLegacyDemoData();

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
