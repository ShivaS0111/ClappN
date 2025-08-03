import React from 'react';
import { 
  LayoutDashboard, 
  Users, 
  Package, 
  Settings, 
  BarChart3, 
  ShoppingBag,
  Menu,
  X
} from 'lucide-react';
import clsx from 'clsx';

const Sidebar = ({ activeTab, onTabChange, isSidebarOpen, toggleSidebar }) => {
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'vendors', label: 'Vendors', icon: Users },
    { id: 'products', label: 'Products', icon: Package },
    { id: 'services', label: 'Services', icon: ShoppingBag },
    { id: 'analytics', label: 'Analytics', icon: BarChart3 },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <>
      {/* Mobile Menu Button */}
      <button
        className="lg:hidden fixed top-4 left-4 z-50 p-2 bg-white rounded-lg shadow-md"
        onClick={toggleSidebar}
      >
        {isSidebarOpen ? <X size={24} /> : <Menu size={24} />}
      </button>

      {/* Overlay for mobile */}
      {isSidebarOpen && (
        <div 
          className="lg:hidden fixed inset-0 bg-black bg-opacity-50 z-40"
          onClick={toggleSidebar}
        />
      )}

      {/* Sidebar */}
      <div className={clsx(
        'fixed left-0 top-0 h-full bg-white shadow-lg border-r border-gray-200 transition-transform duration-300 z-40',
        'w-64 lg:translate-x-0',
        isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
      )}>
        {/* Header */}
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center">
              <LayoutDashboard className="text-white" size={20} />
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-900">VendorHub</h1>
              <p className="text-sm text-gray-500">Multi-Vendor Platform</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-2">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            
            return (
              <button
                key={item.id}
                onClick={() => {
                  onTabChange(item.id);
                  if (window.innerWidth < 1024) {
                    toggleSidebar();
                  }
                }}
                className={clsx(
                  'w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-left transition-colors duration-200',
                  isActive
                    ? 'bg-primary-100 text-primary-700 border border-primary-200'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                )}
              >
                <Icon size={20} />
                <span className="font-medium">{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Footer */}
        <div className="absolute bottom-4 left-4 right-4">
          <div className="bg-gray-50 rounded-lg p-4 text-center">
            <p className="text-sm text-gray-600">Version 1.0.0</p>
            <p className="text-xs text-gray-500 mt-1">© 2024 VendorHub</p>
          </div>
        </div>
      </div>
    </>
  );
};

export default Sidebar;