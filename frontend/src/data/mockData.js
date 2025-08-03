import { v4 as uuidv4 } from 'uuid';

// Mock Vendors Data
export const mockVendors = [
  {
    id: uuidv4(),
    name: 'TechCorp Solutions',
    email: 'contact@techcorp.com',
    phone: '+1-234-567-8901',
    address: '123 Tech Street, Silicon Valley, CA 94105',
    status: 'active',
    joinedDate: '2023-01-15',
    totalProducts: 45,
    totalServices: 12,
    revenue: 125000,
    rating: 4.8,
    category: 'Technology',
    logo: 'https://via.placeholder.com/100x100/3B82F6/FFFFFF?text=TC'
  },
  {
    id: uuidv4(),
    name: 'Fashion Hub Inc',
    email: 'sales@fashionhub.com',
    phone: '+1-234-567-8902',
    address: '456 Fashion Ave, New York, NY 10001',
    status: 'active',
    joinedDate: '2023-02-20',
    totalProducts: 180,
    totalServices: 8,
    revenue: 89000,
    rating: 4.6,
    category: 'Fashion',
    logo: 'https://via.placeholder.com/100x100/F59E0B/FFFFFF?text=FH'
  },
  {
    id: uuidv4(),
    name: 'Green Gardens Co',
    email: 'info@greengardens.com',
    phone: '+1-234-567-8903',
    address: '789 Garden Lane, Portland, OR 97201',
    status: 'pending',
    joinedDate: '2023-03-10',
    totalProducts: 67,
    totalServices: 15,
    revenue: 45000,
    rating: 4.2,
    category: 'Home & Garden',
    logo: 'https://via.placeholder.com/100x100/10B981/FFFFFF?text=GG'
  },
  {
    id: uuidv4(),
    name: 'Food Delights Ltd',
    email: 'orders@fooddelights.com',
    phone: '+1-234-567-8904',
    address: '321 Culinary Blvd, Austin, TX 78701',
    status: 'active',
    joinedDate: '2023-01-28',
    totalProducts: 234,
    totalServices: 6,
    revenue: 156000,
    rating: 4.9,
    category: 'Food & Beverage',
    logo: 'https://via.placeholder.com/100x100/EF4444/FFFFFF?text=FD'
  },
  {
    id: uuidv4(),
    name: 'Auto Parts Pro',
    email: 'support@autopartspro.com',
    phone: '+1-234-567-8905',
    address: '654 Auto Street, Detroit, MI 48201',
    status: 'inactive',
    joinedDate: '2022-11-05',
    totalProducts: 89,
    totalServices: 18,
    revenue: 72000,
    rating: 4.1,
    category: 'Automotive',
    logo: 'https://via.placeholder.com/100x100/8B5CF6/FFFFFF?text=AP'
  }
];

// Mock Products Data
export const mockProducts = [
  {
    id: uuidv4(),
    name: 'Wireless Bluetooth Headphones',
    sku: 'WBH-001',
    category: 'Electronics',
    vendorId: mockVendors[0].id,
    vendorName: 'TechCorp Solutions',
    price: 129.99,
    stock: 45,
    status: 'active',
    createdDate: '2023-03-15',
    sales: 234,
    image: 'https://via.placeholder.com/150x150/3B82F6/FFFFFF?text=HEADPHONES',
    description: 'High-quality wireless headphones with noise cancellation'
  },
  {
    id: uuidv4(),
    name: 'Designer Cotton T-Shirt',
    sku: 'DCT-002',
    category: 'Fashion',
    vendorId: mockVendors[1].id,
    vendorName: 'Fashion Hub Inc',
    price: 39.99,
    stock: 120,
    status: 'active',
    createdDate: '2023-03-10',
    sales: 567,
    image: 'https://via.placeholder.com/150x150/F59E0B/FFFFFF?text=TSHIRT',
    description: 'Premium cotton t-shirt with modern design'
  },
  {
    id: uuidv4(),
    name: 'Organic Plant Fertilizer',
    sku: 'OPF-003',
    category: 'Home & Garden',
    vendorId: mockVendors[2].id,
    vendorName: 'Green Gardens Co',
    price: 24.99,
    stock: 78,
    status: 'active',
    createdDate: '2023-03-08',
    sales: 145,
    image: 'https://via.placeholder.com/150x150/10B981/FFFFFF?text=FERTILIZER',
    description: 'Natural organic fertilizer for healthy plant growth'
  },
  {
    id: uuidv4(),
    name: 'Gourmet Coffee Beans',
    sku: 'GCB-004',
    category: 'Food & Beverage',
    vendorId: mockVendors[3].id,
    vendorName: 'Food Delights Ltd',
    price: 18.99,
    stock: 200,
    status: 'active',
    createdDate: '2023-03-05',
    sales: 892,
    image: 'https://via.placeholder.com/150x150/EF4444/FFFFFF?text=COFFEE',
    description: 'Premium roasted coffee beans from sustainable farms'
  },
  {
    id: uuidv4(),
    name: 'Car Engine Oil',
    sku: 'CEO-005',
    category: 'Automotive',
    vendorId: mockVendors[4].id,
    vendorName: 'Auto Parts Pro',
    price: 45.99,
    stock: 0,
    status: 'out_of_stock',
    createdDate: '2023-02-28',
    sales: 324,
    image: 'https://via.placeholder.com/150x150/8B5CF6/FFFFFF?text=OIL',
    description: 'High-performance synthetic engine oil'
  }
];

// Mock Services Data
export const mockServices = [
  {
    id: uuidv4(),
    name: 'Web Development',
    category: 'Technology',
    vendorId: mockVendors[0].id,
    vendorName: 'TechCorp Solutions',
    price: 2500,
    duration: '2-4 weeks',
    status: 'active',
    bookings: 12,
    rating: 4.9,
    description: 'Custom web application development using modern technologies'
  },
  {
    id: uuidv4(),
    name: 'Fashion Consulting',
    category: 'Fashion',
    vendorId: mockVendors[1].id,
    vendorName: 'Fashion Hub Inc',
    price: 150,
    duration: '1-2 hours',
    status: 'active',
    bookings: 45,
    rating: 4.7,
    description: 'Personal styling and wardrobe consultation services'
  },
  {
    id: uuidv4(),
    name: 'Garden Design',
    category: 'Home & Garden',
    vendorId: mockVendors[2].id,
    vendorName: 'Green Gardens Co',
    price: 800,
    duration: '1 week',
    status: 'active',
    bookings: 8,
    rating: 4.5,
    description: 'Professional landscape and garden design services'
  },
  {
    id: uuidv4(),
    name: 'Catering Service',
    category: 'Food & Beverage',
    vendorId: mockVendors[3].id,
    vendorName: 'Food Delights Ltd',
    price: 35,
    duration: 'Per person',
    status: 'active',
    bookings: 23,
    rating: 4.8,
    description: 'Premium catering for events and corporate functions'
  },
  {
    id: uuidv4(),
    name: 'Auto Repair',
    category: 'Automotive',
    vendorId: mockVendors[4].id,
    vendorName: 'Auto Parts Pro',
    price: 120,
    duration: '1-3 days',
    status: 'inactive',
    bookings: 67,
    rating: 4.2,
    description: 'Professional automotive repair and maintenance services'
  }
];

// Dashboard Metrics
export const dashboardMetrics = {
  totalVendors: mockVendors.length,
  activeVendors: mockVendors.filter(v => v.status === 'active').length,
  totalProducts: mockProducts.length,
  activeProducts: mockProducts.filter(p => p.status === 'active').length,
  totalServices: mockServices.length,
  activeServices: mockServices.filter(s => s.status === 'active').length,
  totalRevenue: mockVendors.reduce((sum, vendor) => sum + vendor.revenue, 0),
  averageRating: (mockVendors.reduce((sum, vendor) => sum + vendor.rating, 0) / mockVendors.length).toFixed(1)
};

// Chart Data
export const revenueChartData = [
  { month: 'Jan', revenue: 45000, orders: 120 },
  { month: 'Feb', revenue: 52000, orders: 145 },
  { month: 'Mar', revenue: 48000, orders: 135 },
  { month: 'Apr', revenue: 61000, orders: 180 },
  { month: 'May', revenue: 55000, orders: 165 },
  { month: 'Jun', revenue: 67000, orders: 195 },
  { month: 'Jul', revenue: 59000, orders: 175 },
  { month: 'Aug', revenue: 71000, orders: 210 },
  { month: 'Sep', revenue: 65000, orders: 190 },
  { month: 'Oct', revenue: 73000, orders: 220 },
  { month: 'Nov', revenue: 69000, orders: 205 },
  { month: 'Dec', revenue: 78000, orders: 235 }
];

export const categoryData = [
  { name: 'Technology', value: 35, color: '#3B82F6' },
  { name: 'Fashion', value: 25, color: '#F59E0B' },
  { name: 'Food & Beverage', value: 20, color: '#EF4444' },
  { name: 'Home & Garden', value: 12, color: '#10B981' },
  { name: 'Automotive', value: 8, color: '#8B5CF6' }
];

export const salesTrendData = [
  { day: 'Mon', sales: 1200 },
  { day: 'Tue', sales: 1900 },
  { day: 'Wed', sales: 1500 },
  { day: 'Thu', sales: 2100 },
  { day: 'Fri', sales: 2500 },
  { day: 'Sat', sales: 2200 },
  { day: 'Sun', sales: 1800 }
];

export const topProducts = mockProducts
  .sort((a, b) => b.sales - a.sales)
  .slice(0, 5)
  .map(product => ({
    ...product,
    totalRevenue: product.price * product.sales
  }));

export const topVendors = mockVendors
  .sort((a, b) => b.revenue - a.revenue)
  .slice(0, 5);