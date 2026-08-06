# BidStream Frontend - Quick Start Guide

## 🚀 Get Up and Running in 5 Minutes

### Prerequisites
- Node.js 16+ (v18+ recommended)
- npm or pnpm
- Backend API running at `http://localhost:8080`

### Step 1: Install Dependencies
```bash
npm install
```

### Step 2: Set Environment Variables
```bash
cp .env.example .env.local
# Edit .env.local if needed (default values work for local dev)
```

### Step 3: Start Development Server
```bash
npm run dev
```

Your app will open at **http://localhost:5173**

### Step 4: Check the Navbar Works
- You should see the BidStream logo and navigation
- The notification bell (top right) should display
- Try clicking around to test basic navigation

---

## 📁 Project Structure (Quick Reference)

```
src/
├── components/
│   ├── layout/Navbar.tsx          ← Main header
│   └── auction/                   ← Auction components
├── pages/                         ← Page components
├── store/                         ← Zustand stores
├── api/client.ts                  ← HTTP client
└── types/                         ← TypeScript types
```

---

## 🔧 Available Commands

```bash
npm run dev          # Start dev server
npm run build        # Build for production
npm run preview      # Preview production build
npm run lint         # Run ESLint
npm run lint:fix     # Fix linting issues
npm run format       # Format code with Prettier
npm run type-check   # Check TypeScript types
npm run test         # Run tests
```

---

## 🧩 Component Checklist

### Already Provided ✅
- [x] `Navbar.tsx` - Header with notifications & user menu
- [x] `AuctionCard.tsx` - Auction card component
- [x] `AuctionDetailView.tsx` - Real-time bidding terminal
- [x] `CreateAuctionModal.tsx` - Create auction form

### Still Needed 🚧
- [ ] LoginPage - User authentication
- [ ] AuctionsPage - Main marketplace
- [ ] SellerHubPage - Seller dashboard
- [ ] ProfilePage - User profile

**Next Step:** Copy component examples to create missing pages (see `AuctionsPage.example.tsx` as template)

---

## 🔌 API Integration

The API client is ready to use in components:

```typescript
import { apiClient } from './api/client';

// Get all auctions
const auctions = await apiClient.getAuctions({ page: 0, size: 12 });

// Place a bid
const bid = await apiClient.placeBid({ amount: 150, bidderId: 1, auctionId: 5 });

// Get notifications
const notifications = await apiClient.getMyNotifications();
```

**Token is automatically injected into all requests!**

---

## 🧪 Testing the Components

### Test Navbar
1. Open http://localhost:5173
2. See the BidStream header with logo
3. Click the notification bell (no data yet without backend)
4. Click user profile dropdown

### Test API Client
```typescript
// In browser console:
import { apiClient } from './api/client';
apiClient.login({ usernameOrEmail: 'test@example.com', password: 'pass' })
```

---

## 📱 Mobile Testing

```bash
# Build and test locally
npm run build
npm run preview

# Or use your phone on same network:
npm run dev -- --host
# Then visit http://<your-ip>:5173
```

---

## 🚀 Deployment

### Vercel (Recommended)
```bash
npm install -g vercel
vercel login
vercel
```

### Docker
```bash
docker build -t bidstream-frontend .
docker run -p 3000:80 bidstream-frontend
```

### Manual Deploy
```bash
npm run build
# Upload `dist/` folder to your hosting
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Cannot find module" | Run `npm install` |
| CORS errors | Check backend is running at `http://localhost:8080` |
| Blank page | Check browser console for errors (F12) |
| Tailwind not working | Rebuild CSS: `npm run dev` |

---

## 📚 Learn More

- **[README.md](./README.md)** - Full feature list and tech stack
- **[PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)** - Detailed setup guide
- **[FILES_SUMMARY.md](./FILES_SUMMARY.md)** - File reference guide

---

## ✨ Next Steps

1. ✅ Project is set up and running
2. 📝 Create LoginPage component
3. 🛍️ Create AuctionsPage component
4. 👤 Create ProfilePage component
5. 🎯 Test API integration with real backend
6. 🎨 Customize styling as needed
7. 🚀 Deploy to production

---

**Happy Coding! 🎉**

Questions? Check the documentation or the API logs for endpoint examples.
