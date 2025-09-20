import React from 'react';
import { 
  Users, 
  Package, 
  DollarSign, 
  TrendingUp, 
  TrendingDown,
  Star,
  BarChart3
} from 'lucide-react';
import RevenueChart from '../Charts/RevenueChart';
import CustomPieChart from '../Charts/PieChart';
import {
  dashboardMetrics,
  revenueChartData,
  categoryData,
  salesTrendData,
  topProducts,
  topVendors
} from '../../data/mockData';

const Dashboard = () => {
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const MetricCard = ({ title, value, icon: Icon, trend, trendValue, color = 'blue' }) => {
    const colorClasses = {
      blue: 'text-blue-600',
      green: 'text-green-600',
      purple: 'text-purple-600',
      yellow: 'text-yellow-600',
      red: 'text-red-600'
    };

    return (
      <div className="metric-card">
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-500 mb-1">{title}</p>
            <p className="text-2xl font-bold text-gray-900 mb-2">{value}</p>
            {trend && (
              <div className="flex items-center space-x-1">
                {trend === 'up' ? (
                  <TrendingUp className="text-green-500" size={16} />
                ) : (
                  <TrendingDown className="text-red-500" size={16} />
                )}
                <span className={`text-sm font-medium ${
                  trend === 'up' ? 'text-green-500' : 'text-red-500'
                }`}>
                  {trendValue}%
                </span>
                <span className="text-sm text-gray-500">vs last month</span>
              </div>
            )}
          </div>
          <div className={`p-3 rounded-lg bg-opacity-10 ${colorClasses[color]}`}>
            <Icon className={colorClasses[color]} size={24} />
          </div>
        </div>
      </div>
    );
  };

  const QuickStatsGrid = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <MetricCard
        title="Total Vendors"
        value={dashboardMetrics.totalVendors}
        icon={Users}
        trend="up"
        trendValue="12"
        color="blue"
      />
      <MetricCard
        title="Active Products"
        value={dashboardMetrics.activeProducts}
        icon={Package}
        trend="up"
        trendValue="8"
        color="green"
      />
      <MetricCard
        title="Total Revenue"
        value={formatCurrency(dashboardMetrics.totalRevenue)}
        icon={DollarSign}
        trend="up"
        trendValue="15"
        color="purple"
      />
      <MetricCard
        title="Average Rating"
        value={`${dashboardMetrics.averageRating} ⭐`}
        icon={Star}
        trend="up"
        trendValue="2"
        color="yellow"
      />
    </div>
  );

  const TopVendorsCard = () => (
    <div className="card">
      <div className="card-header">
        <h3 className="text-lg font-semibold text-gray-900">Top Vendors</h3>
        <button className="text-sm text-primary-600 hover:text-primary-700">
          View All
        </button>
      </div>
      <div className="space-y-4">
        {topVendors.map((vendor, index) => (
          <div key={vendor.id} className="flex items-center space-x-4">
            <div className="flex-shrink-0">
              <span className="inline-flex items-center justify-center w-8 h-8 bg-gray-100 rounded-full text-sm font-medium text-gray-600">
                {index + 1}
              </span>
            </div>
            <img
              src={vendor.logo}
              alt={vendor.name}
              className="w-10 h-10 rounded-lg object-cover"
            />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">{vendor.name}</p>
              <p className="text-sm text-gray-500">{vendor.category}</p>
            </div>
            <div className="text-right">
              <p className="text-sm font-medium text-green-600">{formatCurrency(vendor.revenue)}</p>
              <div className="flex items-center space-x-1">
                <Star className="text-yellow-400 fill-current" size={12} />
                <span className="text-xs text-gray-500">{vendor.rating}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  const TopProductsCard = () => (
    <div className="card">
      <div className="card-header">
        <h3 className="text-lg font-semibold text-gray-900">Top Products</h3>
        <button className="text-sm text-primary-600 hover:text-primary-700">
          View All
        </button>
      </div>
      <div className="space-y-4">
        {topProducts.map((product, index) => (
          <div key={product.id} className="flex items-center space-x-4">
            <div className="flex-shrink-0">
              <span className="inline-flex items-center justify-center w-8 h-8 bg-gray-100 rounded-full text-sm font-medium text-gray-600">
                {index + 1}
              </span>
            </div>
            <img
              src={product.image}
              alt={product.name}
              className="w-10 h-10 rounded-lg object-cover"
            />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">{product.name}</p>
              <p className="text-sm text-gray-500">{product.vendorName}</p>
            </div>
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900">{product.sales} sold</p>
              <p className="text-sm text-green-600">{formatCurrency(product.totalRevenue)}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  const RecentActivity = () => (
    <div className="card">
      <div className="card-header">
        <h3 className="text-lg font-semibold text-gray-900">Recent Activity</h3>
        <button className="text-sm text-primary-600 hover:text-primary-700">
          View All
        </button>
      </div>
      <div className="space-y-4">
        <div className="flex items-start space-x-3">
          <div className="w-2 h-2 rounded-full bg-green-500 mt-2"></div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-gray-900">New vendor <span className="font-medium">TechCorp Solutions</span> registered</p>
            <p className="text-xs text-gray-500">2 minutes ago</p>
          </div>
        </div>
        <div className="flex items-start space-x-3">
          <div className="w-2 h-2 rounded-full bg-blue-500 mt-2"></div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-gray-900">Product <span className="font-medium">Wireless Headphones</span> stock updated</p>
            <p className="text-xs text-gray-500">15 minutes ago</p>
          </div>
        </div>
        <div className="flex items-start space-x-3">
          <div className="w-2 h-2 rounded-full bg-yellow-500 mt-2"></div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-gray-900">Service booking for <span className="font-medium">Garden Design</span></p>
            <p className="text-xs text-gray-500">1 hour ago</p>
          </div>
        </div>
        <div className="flex items-start space-x-3">
          <div className="w-2 h-2 rounded-full bg-purple-500 mt-2"></div>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-gray-900">Monthly revenue report generated</p>
            <p className="text-xs text-gray-500">3 hours ago</p>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-8">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 rounded-lg p-6 text-white">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold mb-2">Welcome back, John!</h1>
            <p className="text-primary-100">
              Here's what's happening with your vendor marketplace today.
            </p>
          </div>
          <div className="hidden md:block">
            <BarChart3 size={48} className="text-primary-200" />
          </div>
        </div>
      </div>

      {/* Quick Stats */}
      <QuickStatsGrid />

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-semibold text-gray-900">Revenue Overview</h3>
            <div className="flex space-x-2">
              <button className="text-sm px-3 py-1 bg-primary-100 text-primary-700 rounded-lg">
                Line
              </button>
              <button className="text-sm px-3 py-1 text-gray-500 hover:bg-gray-100 rounded-lg">
                Bar
              </button>
            </div>
          </div>
          <RevenueChart data={revenueChartData} height={350} />
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-semibold text-gray-900">Category Distribution</h3>
          </div>
          <CustomPieChart data={categoryData} height={350} />
        </div>
      </div>

      {/* Bottom Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <TopVendorsCard />
        <TopProductsCard />
        <RecentActivity />
      </div>

      {/* Sales Trend */}
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-semibold text-gray-900">Weekly Sales Trend</h3>
          <button className="text-sm text-primary-600 hover:text-primary-700">
            View Details
          </button>
        </div>
        <RevenueChart 
          data={salesTrendData.map(item => ({ ...item, revenue: item.sales, month: item.day }))} 
          height={250} 
          type="bar"
        />
      </div>
    </div>
  );
};

export default Dashboard;