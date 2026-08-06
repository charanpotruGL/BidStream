# BidStream Frontend

A production-ready, visually stunning **React 18 + TypeScript** frontend for the **BidStream** real-time online auction platform. Built with Vite, Tailwind CSS, Framer Motion, React Query, and Zustand.

## 🎯 Features

✨ **Real-time Bidding Terminal** - Live auction updates with countdown timers and bid history
🎨 **Cyber/Trading Desk Aesthetic** - Midnight dark theme with vibrant neon accents
⚡ **Live Notifications** - Real-time notification bell with unread badge
🔐 **Secure Authentication** - JWT token-based auth with auto-refresh
📱 **Fully Responsive** - Mobile-first design from 320px to 4K
🌊 **Smooth Animations** - Framer Motion micro-interactions & transitions
🔍 **Advanced Filtering** - Filter by status, sort by price/time/newest
💾 **Smart Caching** - React Query with stale-time & garbage collection
📊 **Seller Dashboard** - Create, manage, and monitor auctions

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Framework** | React 18 + TypeScript |
| **Build Tool** | Vite 4 |
| **Styling** | Tailwind CSS 3 + Lucide Icons |
| **Animations** | Framer Motion |
| **State Management** | Zustand |
| **Data Fetching** | React Query (TanStack Query) + Axios |
| **Routing** | React Router v6 |
| **Notifications** | Sonner Toast |
| **Form Validation** | Custom hooks + field errors from API |

## 🚀 Quick Start

### Prerequisites
- **Node.js** 16+ (18+ recommended)
- **npm** or **pnpm**
- **Backend API** running at `http://localhost:8080`

### 1. Clone & Install

```bash
git clone <repository>
cd bidstream-frontend
npm install
```

### 2. Environment Setup

Create `.env.local`:

```env
REACT_APP_API_URL=http://localhost:8080
REACT_APP_ENV=development
```

### 3. Run Development Server

```bash
npm run dev
```

Open http://localhost:5173 in your browser.

### 4. Build for Production

```bash
npm run build
npm run preview  # Test production build locally
```

## 📁 Project Structure

```
src/
├── components/          # React components (layout, auction, bid, auth, etc.)
├── pages/              # Page components for routing
├── hooks/              # Custom React hooks
├── store/              # Zustand stores (auth, notifications)
├── api/                # Axios client with interceptors
├── types/              # TypeScript interfaces
├── utils/              # Helper functions (formatting, validation, etc.)
├── styles/             # Global CSS & Tailwind config
└── App.tsx             # Main app with routing
```

**See `PROJECT_STRUCTURE.md` for detailed directory breakdown.**

## 🔌 API Integration

### Axios Client with Interceptors

```typescript
import { apiClient } from './api/client';

// Automatically injects Bearer token from localStorage
const auctions = await apiClient.getAuctions({ page: 0, size: 12 });

// Handles 401 errors by clearing token & redirecting to /login
```

### All Available Endpoints

**Authentication:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login with credentials
- `GET /api/users/{id}` - Get user profile
- `GET /api/users/username/{username}` - Get by username

**Auctions:**
- `GET /api/auctions` - List with pagination & sorting
- `GET /api/auctions/{id}` - Get single auction
- `POST /api/auctions` - Create auction
- `PUT /api/auctions/{id}` - Update auction
- `POST /api/auctions/{id}/start` - Start auction
- `POST /api/auctions/{id}/close` - Close auction
- `DELETE /api/auctions/{id}` - Delete auction
- `GET /api/auctions/status/{status}` - Filter by status
- `GET /api/auctions/seller/{sellerId}` - Get seller's auctions
- `GET /api/auctions/stats/active-count` - Count active auctions

**Bids:**
- `POST /api/bids` - Place bid
- `GET /api/bids/{id}` - Get bid details
- `GET /api/bids/auction/{auctionId}` - Get bid history
- `GET /api/bids/auction/{auctionId}/highest` - Get top bid
- `GET /api/bids/bidder/{bidderId}` - Get bidder's history

**Notifications:**
- `GET /api/notifications/user/me` - Get all notifications
- `GET /api/notifications/user/me/unread` - Get unread only
- `GET /api/notifications/user/me/count` - Count unread
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/user/me/read-all` - Mark all as read
- `DELETE /api/notifications/{id}` - Delete notification

## 🎨 Component Examples

### AuctionCard
Reusable card component with live countdown, current price, and quick-bid buttons.

```typescript
<AuctionCard
  auction={auction}
  onQuickBid={(amount) => handleQuickBid(amount)}
  isLoading={isLoading}
/>
```

### AuctionDetailView
Real-time bidding terminal with bid history timeline, current highest bid, and place-bid form.

```typescript
<AuctionDetailView auctionId={auctionId} />
```

### CreateAuctionModal
Modal form for sellers to create new auctions with validation and error handling.

```typescript
<CreateAuctionModal
  isOpen={isOpen}
  onClose={() => setIsOpen(false)}
/>
```

### Navbar
Header component with notification bell, user menu, and search bar.

```typescript
<Navbar />
```

## 🧠 State Management

### Auth Store (Zustand)

```typescript
import { useAuthStore } from './store/authStore';

const { user, token, logout } = useAuthStore();
```

Stores: `user`, `token`, `isAuthenticated`, `isLoading`, `error`

### Notification Store (Zustand)

```typescript
import { useNotificationStore } from './store/notificationStore';

const { notifications, unreadCount, markAsRead } = useNotificationStore();
```

Stores: `notifications[]`, `unreadCount`, `isLoading`

### Data Fetching (React Query)

```typescript
const { data, isLoading, error } = useQuery({
  queryKey: ['auctions'],
  queryFn: () => apiClient.getAuctions(),
  staleTime: 30000,      // 30 seconds
  gcTime: 5 * 60 * 1000, // 5 minutes
});
```

## 🎭 Animations

Uses **Framer Motion** for smooth transitions:

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

- Page transitions
- Card hover effects
- Notification toasts
- Modal open/close
- Pulsing animations for live status

## 📱 Responsive Design

Tailwind breakpoints:
- `sm`: 640px (tablets)
- `md`: 768px (small desktops)
- `lg`: 1024px (desktops)
- `xl`: 1280px (large desktops)

Mobile-first approach with hidden desktop elements and expandable mobile menu.

## 🔐 Authentication Flow

1. User submits credentials → `POST /api/auth/login`
2. Backend returns JWT token
3. `authStore.setToken()` saves token to localStorage
4. Axios interceptor injects `Authorization: Bearer <token>` on all requests
5. Protected routes check `useAuthStore().isAuthenticated`
6. On 401 error → clear token & redirect to `/login`

## 📊 Real-Time Features

### Live Countdown Timer

```typescript
useEffect(() => {
  const interval = setInterval(() => {
    setTimeRemaining(getTimeRemaining(new Date(auction.endTime)));
  }, 1000);
  return () => clearInterval(interval);
}, [auction.endTime]);
```

### Bid History Polling

```typescript
useQuery({
  queryKey: ['bids', auctionId],
  queryFn: () => apiClient.getBidsByAuction(auctionId),
  refetchInterval: 3000, // Poll every 3 seconds
});
```

### Notification Polling

```typescript
useQuery({
  queryKey: ['notifications', 'count'],
  queryFn: () => apiClient.getUnreadNotificationCount(),
  refetchInterval: 30000, // Poll every 30 seconds
});
```

## 🧪 Testing

### Run Tests

```bash
npm run test           # Run with Vitest
npm run test:ui        # Watch mode with UI
npm run e2e           # Run E2E tests with Playwright
```

### Test Examples

```typescript
describe('AuctionCard', () => {
  it('renders auction title and current price', () => {
    const { getByText } = render(<AuctionCard auction={mockAuction} />);
    expect(getByText('Test Auction')).toBeInTheDocument();
  });

  it('handles quick bid button click', async () => {
    const mockHandler = vi.fn();
    const { getByText } = render(
      <AuctionCard auction={mockAuction} onQuickBid={mockHandler} />
    );
    await userEvent.click(getByText('+5%'));
    expect(mockHandler).toHaveBeenCalled();
  });
});
```

## 📦 Dependencies

```json
{
  "react": "^18.2.0",
  "react-dom": "^18.2.0",
  "react-router-dom": "^6.20.0",
  "zustand": "^4.4.0",
  "@tanstack/react-query": "^5.25.0",
  "axios": "^1.6.0",
  "framer-motion": "^10.16.0",
  "lucide-react": "^0.292.0",
  "sonner": "^1.2.0",
  "tailwindcss": "^3.3.0"
}
```

## 🚀 Deployment

### Vercel (Recommended)

```bash
npm install -g vercel
vercel login
vercel --prod
```

### Netlify

```bash
npm install -g netlify-cli
netlify deploy --prod --dir=dist
```

### Docker

```bash
docker build -t bidstream-frontend .
docker run -p 80:80 bidstream-frontend
```

See `Dockerfile` in root directory.

## 🎨 Customization

### Update API URL

Modify `.env.local`:
```env
REACT_APP_API_URL=https://api.example.com
```

### Theming

Edit `tailwind.config.ts`:
```typescript
theme: {
  colors: {
    primary: '#6366F1',  // Indigo
    secondary: '#06B6D4', // Cyan
  },
}
```

### Colors

```
Primary Dark:    #0B0F17 (slate-900)
Secondary Dark:  #1E293B (slate-800)
Accent:          #6366F1 (indigo-600)
Success:         #10B981 (emerald-500)
Warning:         #F59E0B (amber-500)
Danger:          #EF4444 (red-500)
```

## 📚 Documentation

- **[PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)** - Detailed folder structure & setup
- **[API_REFERENCE.md](./docs/API_REFERENCE.md)** - All API endpoints
- **[COMPONENTS.md](./docs/COMPONENTS.md)** - Component usage guide
- **[CONTRIBUTING.md](./CONTRIBUTING.md)** - Contribution guidelines

## 🐛 Troubleshooting

### CORS Errors
```
❌ Access to XMLHttpRequest at 'http://localhost:8080/...'
✅ Ensure backend allows CORS from http://localhost:5173
```

### 401 Unauthorized
```
❌ Getting 401 on authenticated routes
✅ Check if token is stored in localStorage under 'bidstream_token'
```

### Tailwind Styles Not Applied
```bash
❌ Styles not showing up
✅ npm run build && npm run preview
✅ Check tailwind.config.ts content paths
```

## 📖 Examples

### Login Flow
```typescript
const { mutate: login } = useMutation({
  mutationFn: (creds) => apiClient.login(creds),
  onSuccess: (data) => {
    apiClient.setToken(data.token);
    useAuthStore.setState({ user: data, token: data.token });
    navigate('/auctions');
  },
});
```

### Place Bid
```typescript
const { mutate: placeBid } = useMutation({
  mutationFn: (amount) => apiClient.placeBid({ amount, bidderId: user.id, auctionId }),
  onSuccess: () => {
    toast.success('Bid placed!');
    queryClient.invalidateQueries(['bids', auctionId]);
  },
  onError: (error) => {
    toast.error(error.response.data.message);
  },
});
```

### Watch for Outbid Notification
```typescript
useEffect(() => {
  const outbidNotifs = notifications.filter(
    n => n.notificationType === NotificationType.BID_OUTBID
  );
  
  if (outbidNotifs.length > 0) {
    toast.error('You\'ve been outbid!');
  }
}, [notifications]);
```

## 📝 License

MIT License - See LICENSE file

## 👥 Support

- **Issues:** Create an issue on GitHub
- **Discussion:** Use GitHub Discussions
- **Email:** support@bidstream.dev

---

**Built with ❤️ for BidStream Auctions**

*Last Updated: August 2026*
*Version: 1.0.0*
