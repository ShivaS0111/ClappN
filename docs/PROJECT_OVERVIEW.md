# 📱 CraftLane (ClappN) - Project Overview

**Last Updated**: March 14, 2026  
**Status**: Production-Ready Core Features + Active Development  
**Version**: 2.0

---

## 🎯 Project at a Glance

**CraftLane (ClappN)** is a comprehensive **full-stack e-commerce and business management platform** designed for small to medium enterprises (SMEs), service providers, and multi-vendor marketplaces.

### Core Purpose
Enable businesses to:
- ✅ Manage products, inventory, and services
- ✅ Process online and offline orders through POS
- ✅ Offer discounts and run marketing campaigns
- ✅ Accept payments through multiple gateways
- ✅ Track analytics and business metrics
- ✅ Manage teams with role-based permissions
- ✅ Integrate with suppliers and vendors

### Key Statistics
- **Backend**: Java Spring Boot microservices architecture
- **Frontend**: React 18 + TypeScript with Vite
- **Database**: MySQL with Flyway migrations
- **APIs**: 100+ RESTful endpoints
- **Components**: 40+ reusable React components
- **Custom Hooks**: 20+ feature-specific hooks
- **Permissions**: 100+ granular permission types
- **User Roles**: 15+ predefined roles

---

## 🏢 Business Model

### Target Users
1. **Small Business Owners** - Run their own store
2. **Chain Retailers** - Manage multiple stores
3. **Service Providers** - Salons, repair shops, consultants
4. **B2B Vendors** - Supply products to other businesses
5. **Marketplace Operators** - Multi-vendor platform

### Revenue Streams (Planned)
- Transaction fees (0.5-2% per order)
- Premium features subscription
- Payment gateway commissions
- Advertising/featured listings

---

## 🏗️ Technical Stack

### Backend
```
Java Spring Boot
├── Spring Data JPA (ORM)
├── Spring Security (Auth & RBAC)
├── Spring Web (REST APIs)
├── MySQL Database
├── Flyway (DB Migrations)
├── JWT (Token Auth)
├── Log4j (Logging)
└── Maven (Build)
```

### Frontend
```
React 18 + TypeScript
├── Vite (Build tool)
├── React Router (Navigation)
├── Tailwind CSS (Styling)
├── Axios (HTTP Client)
├── Context API (State Management)
└── Custom Hooks (Business Logic)
```

### Infrastructure
- **Server**: Spring Boot embedded Tomcat
- **Database**: MySQL 8.0+
- **Payment Gateways**: Razorpay, Stripe
- **Hosting**: Docker-ready (can containerize)
- **Version Control**: Git

---

## 📊 Core Features

### 1. **Authentication & Authorization** ✅
- JWT-based token authentication
- 15+ role types with permission inheritance
- Permission-based access control (100+ permissions)
- Multi-level approval workflows
- Session management

### 2. **Business Management** ✅
- Business registration & verification
- Multi-store support
- Business type classification (12 types)
- Operating hours & locations
- Rating & review system

### 3. **Inventory Management** ✅
- Product catalog with categories (hierarchical)
- Product lots with expiration tracking
- Stock/inventory levels
- Batch number management
- Low stock alerts

### 4. **POS (Point of Sale) System** ⭐ **CORE FEATURE**
- Real-time cart management
- Barcode scanning support
- Product/service selection
- Manual discounts (percentage/fixed amount)
- GST calculation (SGST/CGST)
- Invoice generation & printing
- Multiple payment methods
- Order tracking

### 5. **Orders & Invoicing** ✅
- Online order creation
- Order status tracking (PENDING → COMPLETE)
- Auto-generated invoices with GST
- Order history & archiving
- Invoice customization with business details
- PDF generation & download

### 6. **Payment Processing** ✅
- Razorpay integration
- Stripe integration
- Payment status tracking
- Refund processing
- Payment history
- Subscription payments (future)

### 7. **Service Management** ✅
- Service creation & configuration
- Service booking system
- Availability management
- Service ratings & reviews
- Service pricing tiers
- Duration-based service setup

### 8. **Offers & Coupons** ✅ **MARKETING**
- Create/edit promotional offers
- Discount types: PERCENTAGE or FIXED_AMOUNT
- Usage limits & tracking
- Validity period management
- Target audience segmentation
- Coupon code generation
- Auto-apply conditions

### 9. **Analytics & Reporting** ✅ **DASHBOARDS**
- Entity overview reports (Products, Services, Orders)
- Sales trends & revenue analysis
- Customer segmentation
- Order metrics (count, value, conversion)
- Business KPIs (daily/monthly/yearly)
- System health monitoring
- Approval activity tracking

### 10. **Approval Workflow** ✅ **GOVERNANCE**
- Multi-level approvals for:
  - Business registrations
  - Product additions
  - Service offerings
  - Offer/coupon creation
  - Refund requests
  - Large transactions
- Status tracking: PENDING → APPROVED → REJECTED
- Comment & reason tracking
- Escalation capabilities

### 11. **Team & Permissions** ✅
- Employee management
- Role assignment
- Team structure
- Permission matrix
- Activity logging
- Access controls

### 12. **Vendor Management** ✅ (BETA)
- Supplier/vendor database
- Vendor performance tracking
- Supply chain integration
- Purchase order management
- Vendor payment tracking

---

## 🎨 Frontend Architecture Highlights

### Component System
```
40+ Reusable Components:
├── Basic (Button, Input, Text, Avatar, Badge)
├── Layout (Header, Sidebar, Card, Container)
├── Forms (Form components, Input validation)
├── Display (DataTable, List, Modal, Toast)
├── Advanced (DatePicker, Dropdown, Tooltip)
├── Status (StatusBadge, ProgressBar)
└── Custom (UserProfile, NotificationProvider)
```

### Page Structure
```
Feature-Based Organization:
src/features/
├── pos/                 → POS module
├── auth/                → Authentication
├── dashboard/           → Admin dashboards
├── offers/              → Marketing offers
├── approvals/           → Approval workflows
├── analytics/           → Reporting
├── products/            → Catalog
├── categories/          → Classification
├── services/            → Service management
├── store/               → Store operations
├── business/            → Business admin
├── coupons/             → Promotion codes
├── vendor/              → Supplier management
└── [8+ more features]
```

### Styling System
- **Tailwind CSS** for utility-first styling
- **Dark mode** support via ThemeProvider
- **Responsive design** (mobile-first)
- **Component variants** (primary, secondary, outline, text)
- **Consistent spacing** & color palette

---

## 🔌 Backend Architecture Highlights

### API Structure
```
RESTful Endpoints:
- Base URL: http://localhost:8080/api
- Format: JSON request/response
- Auth: Bearer JWT token
- Versioning: /api/v1/ (future)
- Response: Standardized with success/error fields
```

### Database Schema
```
Core Tables (100+ total):
├── Users (authentication & roles)
├── Businesses (company info)
├── Stores (locations)
├── Products (catalog)
├── Categories (hierarchy)
├── Services (offerings)
├── Orders (transactions)
├── OrderItems (line items)
├── Invoices (billing)
├── Payments (payment records)
├── Approvals (workflow)
├── Offers (promotions)
├── Coupons (discount codes)
├── Vendors (suppliers)
├── Permissions (RBAC)
└── [20+ more tables]
```

### Microservice-Ready
- Modular feature design
- Independent feature folders
- Loose coupling between modules
- Ready for Spring Boot microservices conversion

---

## 🔐 Security Features

### Authentication
- ✅ JWT token-based authentication
- ✅ Token expiration & refresh
- ✅ Password hashing (Spring Security)
- ✅ Session management

### Authorization
- ✅ Role-based access control (RBAC)
- ✅ Granular permission system (100+ permissions)
- ✅ Method-level security annotations
- ✅ Permission guards in UI

### Data Protection
- ✅ HTTPS/TLS for all communications
- ✅ Input validation & sanitization
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ XSS prevention (React auto-escaping)
- ✅ CSRF token handling (if needed)

### Audit & Compliance
- ✅ Activity logging
- ✅ Approval workflow tracking
- ✅ User action history
- ✅ Data audit trail (future enhancement)

---

## 📈 Current Development Status

### ✅ Production-Ready Features
- Authentication system
- Business management
- Product & inventory management
- POS core functionality
- Payment processing
- Invoice generation
- Order management
- Analytics framework
- Permission system
- Component library
- Responsive design

### 🔄 In Development / Beta
- Advanced POS features (customer profiles, loyalty)
- Real-time analytics dashboard
- Vendor portal enhancements
- Mobile app integration
- AI-powered recommendations

### 🚀 Future Enhancements
- Multi-language support (i18n)
- Advanced CRM features
- Marketing automation
- Supply chain optimization
- AR product visualization
- AI chatbot support
- Marketplace expansion
- Mobile native apps

---

## 💾 Data Flow Examples

### Order Creation Flow
```
1. Customer adds items to cart (POS)
2. Applies discounts & coupon codes
3. System calculates GST & totals
4. Payment method selection
5. Backend processes order:
   - Validates inventory
   - Creates order record
   - Generates invoice
   - Reserves stock
6. Initiates payment
7. Payment gateway processes
8. Callback received
9. Order marked as PAID
10. Invoice sent to customer
11. Inventory updated
```

### Offer Creation Flow
```
1. Business owner creates offer:
   - Name, description
   - Discount type & amount
   - Validity period
   - Target audience
2. Auto-routed to approver
3. Approver reviews & approves/rejects
4. If approved:
   - Offer goes ACTIVE
   - Available in coupons list
   - Can be applied to orders
5. Tracking:
   - Usage count
   - Revenue impact
   - Customer segment impact
```

### Approval Workflow
```
1. Request created (Business registration, product, etc.)
2. Status: PENDING
3. Routed to appropriate reviewer based on:
   - Request type
   - Priority level
   - Department
4. Reviewer:
   - Views request details
   - Adds comments/issues
   - Approves/Rejects
5. If APPROVED:
   - Entity becomes ACTIVE
   - Notify requester
6. If REJECTED:
   - Return for revision
   - Notify requester with reason
7. Archive for audit trail
```

---

## 📊 Activity Statistics

| Area | Count | Status |
|------|-------|--------|
| **Backend API Endpoints** | 100+ | ✅ Active |
| **Frontend Pages/Features** | 18+ | ✅ Active |
| **Reusable Components** | 40+ | ✅ Active |
| **Custom React Hooks** | 20+ | ✅ Active |
| **Database Tables** | 100+ | ✅ Active |
| **User Roles** | 15+ | ✅ Active |
| **Permissions** | 100+ | ✅ Active |
| **Documentation Files** | 15+ | ✅ Latest |
| **Configuration Files** | 10+ | ✅ Current |
| **Postman Collections** | 5+ | ✅ Updated |

---

## 🚀 Quick Start

### For Developers

**Frontend (3 minutes)**
```bash
cd clapp-dashboard
npm install
npm run dev
# Open http://localhost:5173
```

**Backend (5 minutes)**
```bash
cd ClappN
mvn clean spring-boot:run
# Server runs on http://localhost:8080
```

### For API Testing
```bash
1. Open Postman
2. Import: ClappN/postman/ClappN.postman_collection.json
3. Set environment variables
4. Start testing endpoints
```

---

## 📚 Documentation Map

| Document | Purpose |
|----------|---------|
| [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | 📍 Master navigation hub |
| [ARCHITECTURE_GUIDE.md](ARCHITECTURE_GUIDE.md) | 🏗️ Technical design & structure |
| [API_REFERENCE.md](API_REFERENCE.md) | 🔌 Endpoint reference |
| [DEVELOPER_SETUP_GUIDE.md](DEVELOPER_SETUP_GUIDE.md) | 🛠️ Environment setup |
| [AI_CONTEXT.md](AI_CONTEXT.md) | 🤖 Comprehensive AI context |

---

## 🤝 Contributing

### Code Standards
- Follow existing code patterns
- Write meaningful commit messages
- Add comments for complex logic
- Update relevant documentation
- Test before submitting PR

### Feature Development
1. Create feature branch from `develop`
2. Follow feature-based folder structure
3. Write documentation alongside code
4. Submit PR with clear description
5. Await code review & merge

---

## 📞 Support & Resources

### Debug Resources
- Postman collections in `ClappN/postman/`
- Mock data in `src/common/services/mockApi.ts`
- Component examples in `src/components/*.tsx`
- Feature examples in `src/features/*/pages/`

### Logging
- **Frontend**: Check browser console
- **Backend**: Check `ClappN/logs/` directory
- **Database**: Use MySQL Workbench
- **Network**: Use Firefox/Chrome DevTools

---

## ✨ Key Achievements

✅ **Complete RBAC System** - 100+ permissions, 15+ roles  
✅ **Production-Ready POS** - Barcode, GST, multi-payment  
✅ **Advanced Filtering** - Product search, offer management  
✅ **Real-time Analytics** - Dashboard with KPIs  
✅ **Modern UI** - Dark mode, responsive design  
✅ **API-First Design** - Decoupled frontend/backend  
✅ **TypeScript Strict Mode** - Full type safety  
✅ **Documentation** - 100+ documentation files  

---

**Status**: 🟢 Actively Maintained  
**Last Updated**: March 14, 2026  
**Next Major Release**: Q2 2026

For detailed information, see [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md).
