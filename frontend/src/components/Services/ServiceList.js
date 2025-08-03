import React, { useState } from 'react';
import { 
  Plus, 
  Edit, 
  Trash2, 
  Eye, 
  ShoppingBag, 
  Star, 
  Clock, 
  DollarSign,
  Users,
  Filter,
  Download
} from 'lucide-react';
import clsx from 'clsx';

const ServiceList = ({ services, onEdit, onDelete, onView, onAdd }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [vendorFilter, setVendorFilter] = useState('all');
  const [showFilters, setShowFilters] = useState(false);

  // Filter services
  const filteredServices = services.filter(service => {
    const matchesSearch = service.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         service.vendorName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         service.category.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || service.status === statusFilter;
    const matchesCategory = categoryFilter === 'all' || service.category === categoryFilter;
    const matchesVendor = vendorFilter === 'all' || service.vendorName === vendorFilter;
    
    return matchesSearch && matchesStatus && matchesCategory && matchesVendor;
  });

  const getUniqueCategories = () => {
    return [...new Set(services.map(service => service.category))];
  };

  const getUniqueVendors = () => {
    return [...new Set(services.map(service => service.vendorName))];
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'active':
        return <span className="status-badge status-active">Active</span>;
      case 'inactive':
        return <span className="status-badge status-inactive">Inactive</span>;
      case 'paused':
        return <span className="status-badge status-pending">Paused</span>;
      default:
        return <span className="status-badge">{status}</span>;
    }
  };

  const renderStars = (rating) => {
    return (
      <div className="flex items-center">
        {[1, 2, 3, 4, 5].map(star => (
          <Star
            key={star}
            size={14}
            className={clsx(
              star <= rating ? 'text-yellow-400 fill-current' : 'text-gray-300'
            )}
          />
        ))}
        <span className="ml-2 text-sm text-gray-600">{rating}</span>
      </div>
    );
  };

  const ServiceCard = ({ service }) => (
    <div className="card hover:shadow-md transition-shadow duration-200">
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <div className="flex items-center space-x-2 mb-2">
            <h3 className="text-lg font-semibold text-gray-900">{service.name}</h3>
            {getStatusBadge(service.status)}
          </div>
          <p className="text-sm text-blue-600 mb-1">{service.vendorName}</p>
          <p className="text-sm text-gray-600 mb-2">{service.category}</p>
          <p className="text-sm text-gray-700 line-clamp-2">{service.description}</p>
        </div>
        
        <div className="flex items-center space-x-2 ml-4">
          <button
            onClick={() => onView(service)}
            className="p-2 text-gray-400 hover:text-blue-600 transition-colors"
            title="View Details"
          >
            <Eye size={16} />
          </button>
          <button
            onClick={() => onEdit(service)}
            className="p-2 text-gray-400 hover:text-green-600 transition-colors"
            title="Edit Service"
          >
            <Edit size={16} />
          </button>
          <button
            onClick={() => onDelete(service.id)}
            className="p-2 text-gray-400 hover:text-red-600 transition-colors"
            title="Delete Service"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>
      
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-1">
              <DollarSign size={16} className="text-green-600" />
              <span className="text-lg font-semibold text-gray-900">
                {formatCurrency(service.price)}
              </span>
            </div>
            <div className="flex items-center space-x-1">
              <Clock size={16} className="text-blue-600" />
              <span className="text-sm text-gray-600">{service.duration}</span>
            </div>
          </div>
          <div className="text-right">
            <div className="flex items-center space-x-1">
              <Users size={16} className="text-purple-600" />
              <span className="text-sm font-medium text-gray-900">{service.bookings}</span>
              <span className="text-sm text-gray-500">bookings</span>
            </div>
          </div>
        </div>
        
        <div className="flex items-center justify-between pt-3 border-t">
          <div>
            {renderStars(service.rating)}
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => onView(service)}
              className="btn-secondary text-sm py-2 px-3"
            >
              View Details
            </button>
            <button
              onClick={() => onEdit(service)}
              className="btn-primary text-sm py-2 px-3"
            >
              Manage
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Services</h2>
          <p className="text-gray-600">Manage vendor services and bookings</p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="btn-secondary flex items-center space-x-2"
          >
            <Filter size={16} />
            <span>Filters</span>
          </button>
          <button className="btn-secondary flex items-center space-x-2">
            <Download size={16} />
            <span>Export</span>
          </button>
          <button 
            onClick={onAdd}
            className="btn-primary flex items-center space-x-2"
          >
            <Plus size={16} />
            <span>Add Service</span>
          </button>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="space-y-4">
        <div className="relative">
          <input
            type="text"
            placeholder="Search services by name, vendor, or category..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input-field pl-4 pr-4 w-full"
          />
        </div>

        {showFilters && (
          <div className="card">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Status
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="select-field"
                >
                  <option value="all">All Status</option>
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                  <option value="paused">Paused</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Category
                </label>
                <select
                  value={categoryFilter}
                  onChange={(e) => setCategoryFilter(e.target.value)}
                  className="select-field"
                >
                  <option value="all">All Categories</option>
                  {getUniqueCategories().map(category => (
                    <option key={category} value={category}>{category}</option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Vendor
                </label>
                <select
                  value={vendorFilter}
                  onChange={(e) => setVendorFilter(e.target.value)}
                  className="select-field"
                >
                  <option value="all">All Vendors</option>
                  {getUniqueVendors().map(vendor => (
                    <option key={vendor} value={vendor}>{vendor}</option>
                  ))}
                </select>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Services Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="metric-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500">Total Services</p>
              <p className="text-2xl font-bold text-gray-900">{services.length}</p>
            </div>
            <ShoppingBag className="text-blue-600" size={24} />
          </div>
        </div>
        
        <div className="metric-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500">Active Services</p>
              <p className="text-2xl font-bold text-green-600">
                {services.filter(s => s.status === 'active').length}
              </p>
            </div>
            <Users className="text-green-600" size={24} />
          </div>
        </div>
        
        <div className="metric-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500">Total Bookings</p>
              <p className="text-2xl font-bold text-purple-600">
                {services.reduce((sum, service) => sum + service.bookings, 0)}
              </p>
            </div>
            <Clock className="text-purple-600" size={24} />
          </div>
        </div>
        
        <div className="metric-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500">Avg Rating</p>
              <p className="text-2xl font-bold text-yellow-600">
                {(services.reduce((sum, service) => sum + service.rating, 0) / services.length).toFixed(1)}
              </p>
            </div>
            <Star className="text-yellow-600" size={24} />
          </div>
        </div>
      </div>

      {/* Service List */}
      {filteredServices.length > 0 ? (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {filteredServices.map(service => (
            <ServiceCard key={service.id} service={service} />
          ))}
        </div>
      ) : (
        <div className="text-center py-12">
          <div className="w-24 h-24 mx-auto bg-gray-100 rounded-full flex items-center justify-center mb-4">
            <ShoppingBag className="text-gray-400" size={48} />
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">No services found</h3>
          <p className="text-gray-500 mb-4">
            {searchTerm || statusFilter !== 'all' || categoryFilter !== 'all'
              ? 'Try adjusting your search or filters'
              : 'Get started by adding your first service'}
          </p>
          {!searchTerm && statusFilter === 'all' && categoryFilter === 'all' && (
            <button onClick={onAdd} className="btn-primary">
              Add Your First Service
            </button>
          )}
        </div>
      )}

      {/* Results Summary */}
      {filteredServices.length > 0 && (
        <div className="text-center text-sm text-gray-500">
          Showing {filteredServices.length} of {services.length} services
        </div>
      )}
    </div>
  );
};

export default ServiceList;