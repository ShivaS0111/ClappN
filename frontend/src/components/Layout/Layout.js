import React, { useState } from 'react';
import Header from './Header';
import Sidebar from './Sidebar';

const Layout = ({ children, currentSection, setCurrentSection }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const sectionTitles = {
    dashboard: { title: 'Dashboard', subtitle: 'Welcome to your multi-vendor management platform' },
    vendors: { title: 'Vendors', subtitle: 'Manage your vendor network' },
    products: { title: 'Products', subtitle: 'Product catalog management' },
    services: { title: 'Services', subtitle: 'Service offerings management' }
  };

  const currentTitle = sectionTitles[currentSection] || sectionTitles.dashboard;

  return (
    <div className="min-h-screen bg-gray-50">
      <Sidebar 
        currentSection={currentSection}
        setCurrentSection={setCurrentSection}
        sidebarOpen={sidebarOpen}
        setSidebarOpen={setSidebarOpen}
      />
      
      <div className="lg:pl-64">
        <Header 
          title={currentTitle.title}
          subtitle={currentTitle.subtitle}
        />
        
        <main className="py-6">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};

export default Layout;