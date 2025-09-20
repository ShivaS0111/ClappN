import React, { useState } from 'react';
import { 
  Plus, 
  Edit, 
  Trash2, 
  Eye, 
  Star, 
  Mail, 
  Phone, 
  Filter,
  Download,
  Users
} from 'lucide-react';

const VendorList = ({ vendors, onEdit, onDelete, onView, onAdd }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [sortBy, setSortBy] = useState('name');
  const [sortOrder, setSortOrder] = useState('asc');
  const [showFilters, setShowFilters] = useState(false);

  // Filter and sort vendors
  const filteredVendors = vendors
    .filter(vendor => {
      const matchesSearch = vendor.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           vendor.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           vendor.category.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesStatus = statusFilter === 'all' || vendor.status === statusFilter;
      const matchesCategory = categoryFilter === 'all' || vendor.category === categoryFilter;
      
      return matchesSearch && matchesStatus && matchesCategory;
    })
    .sort((a, b) => {
      let aValue = a[sortBy];
      let bValue = b[sortBy];
      
      if (typeof aValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = bValue.toLowerCase();
      }
      
      if (sortOrder === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

  const getUniqueCategories = () => {
    return [...new Set(vendors.map(vendor => vendor.category))];
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'active':
        return <span className="status-badge status-active">Active</span>;
      case 'inactive':
        return <span className="status-badge status-inactive">Inactive</span>;
      case 'pending':
        return <span className="status-badge status-pending">Pending</span>;
      default:
        return <span className="status-badge">{status}</span>;
    }
  };

  const VendorCard = ({ vendor }) => (
    <div className="card hover:shadow-md transition-shadow duration-200">
      <div className="flex items-start justify-between">
        <div className="flex items-center space-x-4">
          <img
            src={vendor.logo}
            alt={vendor.name}
            className="w-16 h-16 rounded-lg object-cover"
          />
          <div className="flex-1">
            <div className="flex items-center space-x-2 mb-1">
              <h3 className="text-lg font-semibold text-gray-900">{vendor.name}</h3>
              {getStatusBadge(vendor.status)}
            </div>
            <p className="text-sm text-gray-600 mb-1">{vendor.category}</p>
            <div className="flex items-center space-x-4 text-sm text-gray-500">
              <div className="flex items-center space-x-1">
                <Mail size={14} />
                <span>{vendor.email}</span>
              </div>
              <div className="flex items-center space-x-1">
                <Phone size={14} />
                <span>{vendor.phone}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          <button
            onClick={() => onView(vendor)}
            className="p-2 text-gray-400 hover:text-blue-600 transition-colors"
            title="View Details"
          >
            <Eye size={16} />
          </button>
          <button
            onClick={() => onEdit(vendor)}
            className="p-2 text-gray-400 hover:text-green-600 transition-colors"
            title="Edit Vendor"
          >
            <Edit size={16} />
          </button>
          <button
            onClick={() => onDelete(vendor.id)}
            className="p-2 text-gray-400 hover:text-red-600 transition-colors"
            title="Delete Vendor"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>
      
      <div className="mt-4 grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="text-center">
          <p className="text-lg font-semibold text-gray-900">{vendor.totalProducts}</p>
          <p className="text-xs text-gray-500">Products</p>
        </div>
        <div className="text-center">
          <p className="text-lg font-semibold text-gray-900">{vendor.totalServices}</p>
          <p className="text-xs text-gray-500">Services</p>
        </div>
        <div className="text-center">
          <p className="text-lg font-semibold text-green-600">{formatCurrency(vendor.revenue)}</p>
          <p className="text-xs text-gray-500">Revenue</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center space-x-1">
            <Star className="text-yellow-400 fill-current" size={14} />
            <p className="text-lg font-semibold text-gray-900">{vendor.rating}</p>
          </div>
          <p className="text-xs text-gray-500">Rating</p>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Vendors</h2>
          <p className="text-gray-600">Manage your vendor partners</p>
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
            <span>Add Vendor</span>
          </button>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="space-y-4">
        {/* Search Bar */}
        <div className="relative">
          <input
            type="text"
            placeholder="Search vendors by name, email, or category..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input-field pl-4 pr-4 w-full"
          />
        </div>

        {/* Filters */}
        {showFilters && (
          <div className="card">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
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
                  <option value="pending">Pending</option>
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
                  Sort By
                </label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="select-field"
                >
                  <option value="name">Name</option>
                  <option value="revenue">Revenue</option>
                  <option value="rating">Rating</option>
                  <option value="joinedDate">Join Date</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Order
                </label>
                <select
                  value={sortOrder}
                  onChange={(e) => setSortOrder(e.target.value)}
                  className="select-field"
                >
                  <option value="asc">Ascending</option>
                  <option value="desc">Descending</option>
                </select>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Vendor List */}
      <div className="grid grid-cols-1 gap-6">
        {filteredVendors.length > 0 ? (
          filteredVendors.map(vendor => (
            <VendorCard key={vendor.id} vendor={vendor} />
          ))
        ) : (
          <div className="text-center py-12">
            <div className="w-24 h-24 mx-auto bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <Users className="text-gray-400" size={48} />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">No vendors found</h3>
            <p className="text-gray-500 mb-4">
              {searchTerm || statusFilter !== 'all' || categoryFilter !== 'all'
                ? 'Try adjusting your search or filters'
                : 'Get started by adding your first vendor'}
            </p>
            {!searchTerm && statusFilter === 'all' && categoryFilter === 'all' && (
              <button onClick={onAdd} className="btn-primary">
                Add Your First Vendor
              </button>
            )}
          </div>
        )}
      </div>

      {/* Results Summary */}
      {filteredVendors.length > 0 && (
        <div className="text-center text-sm text-gray-500">
          Showing {filteredVendors.length} of {vendors.length} vendors
        </div>
      )}
    </div>
  );
};

export default VendorList;