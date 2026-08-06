# BidStream Frontend - Project Structure Guide

Production-ready React 18 + TypeScript + Vite application for the BidStream real-time auction platform.

## ============================================================================
## Directory Structure
## ============================================================================

```
bidstream-frontend/
├── src/
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Navbar.tsx              # Header with notification bell + user menu
│   │   │   ├── Sidebar.tsx             # (Optional) Collapsible sidebar for navigation
│   │   │   └── Footer.tsx              # Footer component
│   │   │
│   │   ├── auction/
│   │   │   ├── AuctionCard.tsx         # Reusable auction card with quick bid buttons
│   │   │   ├── AuctionGrid.tsx         # Grid layout for auction listings
│   │   │   ├── AuctionDetailView.tsx   # Single auction page (real-time bidding terminal)
│   │   │   ├── CreateAuctionModal.tsx  # Modal for creating new auctions
│   │   │   ├── AuctionFilter.tsx       # Filter by status, price range, etc.
│   │   │   └── AuctionSearch.tsx       # Search component
│   │   │
│   │   ├── bid/
│   │   │   ├── BidPlaceForm.tsx        # Form to place a bid
│   │   │   ├── BidHistory.tsx          # Table/list of bid history
│   │   │   └── BidNotification.tsx     # Toast for bid events (outbid, placed, etc.)
│   │   │
│   │   ├── notification/
│   │   │   ├── NotificationBell.tsx    # Notification icon + badge
│   │   │   ├── NotificationDrawer.tsx  # Full notification drawer/panel
│   │   │   ├── NotificationItem.tsx    # Single notification item
│   │   │   └── NotificationHub.tsx     # Full notifications page
│   │   │
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx           # Login component
│   │   │   ├── RegisterForm.tsx        # Registration component
│   │   │   ├── ProtectedRoute.tsx      # Route guard component
│   │   │   └── AuthGuard.tsx           # Auth state provider
│   │   │
│   │   ├── seller/
│   │   │   ├── SellerHub.tsx           # Seller dashboard
│   │   │   ├── MyAuctionsTable.tsx     # Table of user's auctions with actions
│   │   │   ├── AuctionStats.tsx        # Stats card (active, closed, total bids)
│   │   │   └── AuctionControls.tsx     # Start/Close/Delete auction buttons
│   │   │
│   │   └── common/
│   │       ├── LoadingSpinner.tsx      # Loading indicator
│   │       ├── ErrorBoundary.tsx       # Error boundary component
│   │       ├── Toast.tsx               # Toast notification provider (Sonner)
│   │       ├── Modal.tsx               # Reusable modal wrapper
│   │       └── Skeleton.tsx            # Skeleton loader for cards/tables
│   │
│   ├── pages/
│   │   ├── HomePage.tsx                # Landing/home page
│   │   ├── LoginPage.tsx               # /login
│   │   ├── RegisterPage.tsx            # /register
│   │   ├── AuctionsPage.tsx            # /auctions (main marketplace)
│   │   ├── AuctionDetailPage.tsx       # /auction/:id (single auction)
│   │   ├── SellerHubPage.tsx           # /seller-hub (seller dashboard)
│   │   ├── ProfilePage.tsx             # /profile/:userId
│   │   ├── NotificationsPage.tsx       # /notifications (full notifications)
│   │   ├── SettingsPage.tsx            # /settings (user settings)
│   │   └── NotFoundPage.tsx            # 404 page
│   │
│   ├── hooks/
│   │   ├── useAuth.ts                  # Custom hook for auth state + methods
│   │   ├── useNotifications.ts         # Custom hook for notifications
│   │   ├── useAuctions.ts              # Custom hook for auction queries
│   │   ├── useBids.ts                  # Custom hook for bid operations
│   │   ├── useCountdown.ts             # Custom hook for countdown timer
│   │   ├── useLocalStorage.ts          # Custom hook for localStorage
│   │   └── useDebounce.ts              # Custom hook for debounced values
│   │
│   ├── store/
│   │   ├── authStore.ts                # Zustand auth store (user, token, login state)
│   │   ├── notificationStore.ts        # Zustand notification store
│   │   ├── auctionStore.ts             # (Optional) Zustand auction store
│   │   └── index.ts                    # Export all stores
│   │
│   ├── api/
│   │   ├── client.ts                   # Axios instance with interceptors
│   │   ├── endpoints.ts                # (Optional) Grouped endpoint functions
│   │   └── index.ts                    # Export API client
│   │
│   ├── types/
│   │   ├── index.ts                    # TypeScript interfaces for all entities
│   │   └── api-errors.ts               # Error response types
│   │
│   ├── utils/
│   │   ├── formatting.ts               # formatCurrency, formatDateTime, etc.
│   │   ├── validation.ts               # Email, password validation
│   │   ├── dates.ts                    # getTimeRemaining, date utilities
│   │   ├── storage.ts                  # localStorage helpers
│   │   └── index.ts                    # Export all utilities
│   │
│   ├── styles/
│   │   ├── globals.css                 # Tailwind imports + custom globals
│   │   ├── animations.css              # Custom animations & keyframes
│   │   └── variables.css               # CSS custom properties (colors, etc.)
│   │
│   ├── context/
│   │   ├── AuthContext.tsx             # (Alternative to Zustand) Auth context
│   │   └── ThemeContext.tsx            # (Optional) Dark/Light theme context
│   │
│   ├── App.tsx                         # Main app component + route definitions
│   ├── main.tsx                        # Entry point
│   └── index.css                       # Root styles
│
├── public/
│   ├── favicon.svg
│   └── logo.svg
│
├── .env.example                        # Environment variables template
├── .env.local                          # Local environment (git-ignored)
├── .gitignore
├── eslintrc.cjs                        # ESLint config
├── prettier.config.cjs                 # Prettier config
├── tailwind.config.ts                  # Tailwind CSS config
├── tsconfig.json                       # TypeScript config
├── vite.config.ts                      # Vite config
├── package.json                        # Dependencies & scripts
└── README.md                           # Documentation
```

---

## ============================================================================
## Setup Instructions
## ============================================================================

### Prerequisites
- Node.js 16+ (18+ recommended)
- npm or pnpm

### 1. Create Project

```bash
npm create vite@latest bidstream-frontend -- --template react-ts
cd bidstream-frontend
npm install
```

### 2. Install Dependencies

```bash
# Core dependencies
npm install react-dom react-router-dom

# State Management
npm install zustand

# API Client
npm install axios

# Data Fetching & Caching
npm install @tanstack/react-query

# Animations
npm install framer-motion

# UI Icons
npm install lucide-react

# UI Components (Headless)
npm install @radix-ui/react-dialog @radix-ui/react-popover @radix-ui/react-slot

# Notifications/Toast
npm install sonner

# Styling
npm install -D tailwindcss postcss autoprefixer
npm install -D @tailwindcss/forms

# Development Tools
npm install -D typescript @types/react @types/react-dom
npm install -D eslint eslint-config-react eslint-plugin-react-hooks
npm install -D prettier

# HTTP Utilities (Optional)
npm install clsx tailwind-merge
```

### 3. Initialize Tailwind CSS

```bash
npx tailwindcss init -p
```

Update `tailwind.config.ts`:

```typescript
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        slate: {
          900: "#0B0F17", // Primary dark bg
        },
      },
      fontFamily: {
        sans: ["Inter", "sans-serif"],
      },
    },
  },
  plugins: [require("@tailwindcss/forms")],
};
```

### 4. Create `.env.local`

```bash
REACT_APP_API_URL=http://localhost:8080
REACT_APP_ENV=development
```

### 5. Copy Source Files

Copy all provided TypeScript/React files into `src/` directory:

```
src/
├── types.ts              → types/index.ts
├── api-client.ts         → api/client.ts
├── store.ts              → store/authStore.ts + store/notificationStore.ts
├── utils.ts              → utils/index.ts
├── Navbar.tsx            → components/layout/Navbar.tsx
├── AuctionCard.tsx       → components/auction/AuctionCard.tsx
├── AuctionDetailView.tsx → components/auction/AuctionDetailView.tsx
└── CreateAuctionModal.tsx → components/auction/CreateAuctionModal.tsx
```

### 6. Create App Router

Create `src/App.tsx`:

```typescript
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { Navbar } from './components/layout/Navbar';
import LoginPage from './pages/LoginPage';
import AuctionsPage from './pages/AuctionsPage';
import AuctionDetailPage from './pages/AuctionDetailPage';

const queryClient = new QueryClient();

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Navbar />
        <main className="min-h-screen bg-slate-900">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/auctions" element={<AuctionsPage />} />
            <Route path="/auction/:id" element={<AuctionDetailPage />} />
          </Routes>
        </main>
      </Router>
    </QueryClientProvider>
  );
}
```

---

## ============================================================================
## Component Integration Checklist
## ============================================================================

### Must-Have Components (MVP)

- [x] **Navbar** - With notification bell, user menu, search
- [x] **AuctionCard** - Grid/list card with quick bid buttons
- [x] **AuctionDetailView** - Real-time bidding terminal
- [x] **CreateAuctionModal** - Seller auction creation
- [ ] **LoginForm** - User authentication
- [ ] **AuctionsPage** - Marketplace with filtering & pagination
- [ ] **SellerHubPage** - Seller dashboard with auction management

### Optional Enhancements

- [ ] **WebSocket Integration** - Real-time bid updates (Socket.io)
- [ ] **Advanced Analytics** - Charts of bid history
- [ ] **Favorites/Watchlist** - Save auctions to track
- [ ] **User Reviews** - Seller/bidder ratings
- [ ] **Live Chat** - Direct messaging between buyers/sellers
- [ ] **Payment Integration** - Stripe/PayPal checkout

---

## ============================================================================
## Styling System
## ============================================================================

### Color Palette

```
Primary Dark:    #0B0F17 (slate-900)
Secondary Dark:  #1E293B (slate-800)
Accent:          #6366F1 (indigo-600)
Success:         #10B981 (emerald-500)
Warning:         #F59E0B (amber-500)
Danger:          #EF4444 (red-500)
Info:            #06B6D4 (cyan-500)
```

### Text Hierarchy

```
h1: 3xl bold (30px)
h2: 2xl bold (24px)
h3: xl bold (20px)
p:  base regular (16px)
caption: xs gray (12px)
```

### Spacing Scale

Uses Tailwind's default spacing: 4px increments
- `p-4` = 16px padding
- `gap-6` = 24px gap
- `space-y-4` = 16px vertical spacing

---

## ============================================================================
## State Management Flow
## ============================================================================

### Auth State (Zustand)
```
User Login
   ↓
apiClient.login()
   ↓
authStore.setToken() + authStore.setUser()
   ↓
useAuthStore hook reads state
   ↓
Protected routes + Navbar updates
```

### Notifications (Zustand)
```
Poll: GET /api/notifications/user/me (every 30s)
   ↓
useNotificationStore.setNotifications()
   ↓
Navbar notification bell + NotificationDrawer update
   ↓
User clicks notification
   ↓
PUT /api/notifications/{id}/read
   ↓
Store updates, UI re-renders
```

### Auction Data (React Query)
```
Component mounts
   ↓
useQuery(['auctions'], apiClient.getAuctions())
   ↓
Skeleton loader shows
   ↓
Data fetches, component renders AuctionCards
   ↓
User places bid
   ↓
useMutation(['bids'], apiClient.placeBid())
   ↓
queryClient.invalidateQueries(['auctions', 'bids'])
   ↓
Data refetches automatically
```

---

## ============================================================================
## Key Development Patterns
## ============================================================================

### 1. **API Calls with Error Handling**

```typescript
const { mutate: placeBid, isPending } = useMutation({
  mutationFn: (amount: number) => apiClient.placeBid({...}),
  onSuccess: () => {
    toast.success('Bid placed!');
    queryClient.invalidateQueries();
  },
  onError: (error: any) => {
    const msg = error.response?.data?.fieldErrors?.amount;
    toast.error(msg || 'Failed to place bid');
  },
});
```

### 2. **Real-Time Countdown**

```typescript
useEffect(() => {
  const interval = setInterval(() => {
    setTimeRemaining(getTimeRemaining(new Date(auction.endTime)));
  }, 1000);
  return () => clearInterval(interval);
}, [auction.endTime]);
```

### 3. **Framer Motion Animations**

```typescript
<motion.div
  initial={{ opacity: 0, y: 20 }}
  animate={{ opacity: 1, y: 0 }}
  exit={{ opacity: 0, y: -20 }}
  transition={{ duration: 0.3 }}
>
  {/* Content */}
</motion.div>
```

### 4. **Zustand Store Usage**

```typescript
const { user, setUser, logout } = useAuthStore();
```

---

## ============================================================================
## Testing Strategy
## ============================================================================

### Unit Tests (Vitest)
```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom
```

Test files alongside components:
```
AuctionCard.tsx
AuctionCard.test.tsx
```

### E2E Tests (Playwright/Cypress)
```bash
npm install -D @playwright/test
```

Key flows to test:
1. User login → Token stored → Redirect to auctions
2. View auction list → Click auction → See bid history
3. Place bid → Success toast → Bid appears in list
4. Receive notification → Mark as read → Badge disappears

---

## ============================================================================
## Performance Optimization
## ============================================================================

### Code Splitting
```typescript
const AuctionDetailPage = lazy(() => import('./pages/AuctionDetailPage'));
```

### Memoization
```typescript
const AuctionCard = memo(({ auction, onBid }: Props) => {...});
```

### React Query Cache
```typescript
staleTime: 30000,      // 30 seconds
gcTime: 5 * 60 * 1000, // 5 minutes (formerly cacheTime)
```

### Image Optimization
- Lazy load auction images
- Use WebP with PNG fallback
- Responsive images with srcset

---

## ============================================================================
## Deployment Guide
## ============================================================================

### Build
```bash
npm run build
```

Outputs to `dist/` directory.

### Hosting Options

1. **Vercel** (Recommended for Vite)
```bash
npm install -g vercel
vercel
```

2. **Netlify**
```bash
npm install -g netlify-cli
netlify deploy --prod --dir=dist
```

3. **Docker**
```dockerfile
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## ============================================================================
## Useful NPM Scripts
## ============================================================================

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint src --ext .ts,.tsx",
    "lint:fix": "eslint src --ext .ts,.tsx --fix",
    "format": "prettier --write \"src/**/*.{ts,tsx}\"",
    "type-check": "tsc --noEmit",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "e2e": "playwright test"
  }
}
```

---

## ============================================================================
## Next Steps
## ============================================================================

1. ✅ Copy provided TypeScript files
2. ✅ Install dependencies
3. ✅ Set up Tailwind CSS
4. ✅ Create `.env.local` with API URL
5. ✅ Build remaining pages (LoginPage, AuctionsPage, etc.)
6. ✅ Implement auth flow (login → store token → protect routes)
7. ✅ Add WebSocket for real-time updates (optional)
8. ✅ Write tests
9. ✅ Deploy to Vercel/Netlify

---

**Last Updated:** August 2026
**Stack Version:** React 18, TypeScript 5, Vite 4, Tailwind 3
