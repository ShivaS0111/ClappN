import React, { useState } from 'react';
import { 
  Plus, 
  Edit, 
  Trash2, 
  Eye, 
  Package, 
  AlertTriangle,
  Filter,
  Download,
  Grid,
  List as ListIcon
} from 'lucide-react';
import clsx from 'clsx';

const ProductList = ({ products, onEdit, onDelete, onView, onAdd }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [vendorFilter, setVendorFilter] = useState('all');
  const [viewMode, setViewMode] = useState('grid');
  const [showFilters, setShowFilters] = useState(false);

  // Filter products
  const filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.sku.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.vendorName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || product.status === statusFilter;
    const matchesCategory = categoryFilter === 'all' || product.category === categoryFilter;
    const matchesVendor = vendorFilter === 'all' || product.vendorName === vendorFilter;
    
    return matchesSearch && matchesStatus && matchesCategory && matchesVendor;
  });

  const getUniqueCategories = () => {
    return [...new Set(products.map(product => product.category))];
  };

  const getUniqueVendors = () => {
    return [...new Set(products.map(product => product.vendorName))];
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const getStatusBadge = (status, stock) => {
    if (status === 'out_of_stock' || stock === 0) {
      return <span className="status-badge bg-red-100 text-red-800">Out of Stock</span>;
    }
    if (stock < 10) {
      return <span className="status-badge bg-yellow-100 text-yellow-800">Low Stock</span>;
    }
    if (status === 'active') {
      return <span className="status-badge status-active">Active</span>;
    }
    return <span className="status-badge status-inactive">Inactive</span>;
  };

  const ProductCard = ({ product }) => (
    <div className="card hover:shadow-md transition-shadow duration-200">
      <div className="relative">
        <img
          src={product.image}
          alt={product.name}
          className="w-full h-48 object-cover rounded-lg mb-4"
        />
        <div className="absolute top-2 right-2">
          {getStatusBadge(product.status, product.stock)}
        </div>
        {product.stock < 10 && product.stock > 0 && (
          <div className="absolute top-2 left-2">
            <AlertTriangle className="text-yellow-500" size={20} />
          </div>
        )}
      </div>
      
      <div className="space-y-3">
        <div>
          <h3 className="text-lg font-semibold text-gray-900 truncate">{product.name}</h3>
          <p className="text-sm text-gray-500">SKU: {product.sku}</p>
          <p className="text-sm text-blue-600">{product.vendorName}</p>
        </div>
        
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xl font-bold text-gray-900">{formatCurrency(product.price)}</p>
            <p className="text-sm text-gray-500">Stock: {product.stock}</p>
          </div>
          <div className="text-right">
            <p className="text-sm font-medium text-green-600">{product.sales} sold</p>
            <p className="text-xs text-gray-500">{product.category}</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-2 pt-3 border-t">
          <button
            onClick={() => onView(product)}
            className="flex-1 btn-secondary text-sm py-2"
          >
            <Eye size={14} className="mr-1" />
            View
          </button>
          <button
            onClick={() => onEdit(product)}
            className="flex-1 btn-primary text-sm py-2"
          >
            <Edit size={14} className="mr-1" />
            Edit
          </button>
          <button
            onClick={() => onDelete(product.id)}
            className="p-2 text-gray-400 hover:text-red-600 transition-colors"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>
    </div>
  );

  const ProductRow = ({ product }) => (
    <div className="card">
      <div className="flex items-center space-x-4">
        <img
          src={product.image}
          alt={product.name}
          className="w-16 h-16 object-cover rounded-lg"
        />
        
        <div className="flex-1 min-w-0">
          <div className="flex items-center space-x-2 mb-1">
            <h3 className="text-lg font-semibold text-gray-900 truncate">{product.name}</h3>
            {getStatusBadge(product.status, product.stock)}
          </div>
          <p className="text-sm text-gray-500">SKU: {product.sku} • {product.vendorName}</p>
          <p className="text-sm text-gray-600">{product.category}</p>
        </div>
        
        <div className="grid grid-cols-3 gap-8 text-center">
          <div>
            <p className="text-lg font-semibold text-gray-900">{formatCurrency(product.price)}</p>
            <p className="text-sm text-gray-500">Price</p>
          </div>
          <div>
            <p className="text-lg font-semibold text-gray-900">{product.stock}</p>
            <p className="text-sm text-gray-500">Stock</p>
          </div>
          <div>
            <p className="text-lg font-semibold text-green-600">{product.sales}</p>
            <p className="text-sm text-gray-500">Sales</p>
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          <button
            onClick={() => onView(product)}
            className="p-2 text-gray-400 hover:text-blue-600 transition-colors"
          >
            <Eye size={16} />
          </button>
          <button
            onClick={() => onEdit(product)}
            className="p-2 text-gray-400 hover:text-green-600 transition-colors"
          >
            <Edit size={16} />
          </button>
          <button
            onClick={() => onDelete(product.id)}
            className="p-2 text-gray-400 hover:text-red-600 transition-colors"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Products</h2>
          <p className="text-gray-600">Manage your product inventory</p>
        </div>
        <div className="flex items-center space-x-3">
          <div className="flex items-center bg-gray-100 rounded-lg p-1">
            <button
              onClick={() => setViewMode('grid')}
              className={clsx(
                'p-2 rounded-md transition-colors',
                viewMode === 'grid' ? 'bg-white text-primary-600 shadow-sm' : 'text-gray-500'
              )}
            >
              <Grid size={16} />
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={clsx(
                'p-2 rounded-md transition-colors',
                viewMode === 'list' ? 'bg-white text-primary-600 shadow-sm' : 'text-gray-500'
              )}
            >
              <ListIcon size={16} />
            </button>
          </div>
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
            <span>Add Product</span>
          </button>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="space-y-4">
        <div className="relative">
          <input
            type="text"
            placeholder="Search products by name, SKU, or vendor..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input-field pl-4 pr-4 w-full"
          />
        </div>

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
                  <option value="out_of_stock">Out of Stock</option>
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

      {/* Product List */}
      {filteredProducts.length > 0 ? (
        viewMode === 'grid' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredProducts.map(product => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        ) : (
          <div className="space-y-4">
            {filteredProducts.map(product => (
              <ProductRow key={product.id} product={product} />
            ))}
          </div>
        )
      ) : (
        <div className="text-center py-12">
          <div className="w-24 h-24 mx-auto bg-gray-100 rounded-full flex items-center justify-center mb-4">
            <Package className="text-gray-400" size={48} />
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">No products found</h3>
          <p className="text-gray-500 mb-4">
            {searchTerm || statusFilter !== 'all' || categoryFilter !== 'all'
              ? 'Try adjusting your search or filters'
              : 'Get started by adding your first product'}
          </p>
          {!searchTerm && statusFilter === 'all' && categoryFilter === 'all' && (
            <button onClick={onAdd} className="btn-primary">
              Add Your First Product
            </button>
          )}
        </div>
      )}

      {/* Results Summary */}
      {filteredProducts.length > 0 && (
        <div className="text-center text-sm text-gray-500">
          Showing {filteredProducts.length} of {products.length} products
        </div>
      )}
    </div>
  );
};

export default ProductList;