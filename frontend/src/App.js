import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import Dashboard from './components/Dashboard/Dashboard';
import Vendors from './components/Vendors/Vendors';
import Products from './components/Products/Products';
import Services from './components/Services/Services';

function App() {
  const [currentSection, setCurrentSection] = useState('dashboard');

  return (
    <Router>
      <div className="App">
        <Layout currentSection={currentSection} setCurrentSection={setCurrentSection}>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/vendors" element={<Vendors />} />
            <Route path="/products" element={<Products />} />
            <Route path="/services" element={<Services />} />
          </Routes>
        </Layout>
      </div>
    </Router>
  );
}

export default App;