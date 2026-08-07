import { Component, Suspense, lazy, useEffect, type ReactNode } from 'react'
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useLocation,
} from 'react-router-dom'
import { QueryClientProvider, QueryClient } from '@tanstack/react-query'
import { AnimatePresence, MotionConfig, motion } from 'framer-motion'
import { Toaster } from 'sonner'
import { Navbar } from './components/layout/Navbar'
import { Spinner } from './components/ui/Spinner'
import { Button } from './components/ui/Button'
import { apiClient } from './api/client'
import { useAuthStore } from './store'

const HomePage = lazy(() =>
  import('./pages/HomePage').then((m) => ({ default: m.HomePage }))
)
const AuctionsPage = lazy(() =>
  import('./pages/AuctionsPage').then((m) => ({ default: m.AuctionsPage }))
)
const AuctionDetailPage = lazy(() =>
  import('./pages/AuctionDetailPage').then((m) => ({ default: m.AuctionDetailPage }))
)
const LoginPage = lazy(() =>
  import('./pages/LoginPage').then((m) => ({ default: m.LoginPage }))
)
const SellerHubPage = lazy(() =>
  import('./pages/SellerHubPage').then((m) => ({ default: m.SellerHubPage }))
)
const NotificationsPage = lazy(() =>
  import('./pages/NotificationsPage').then((m) => ({ default: m.NotificationsPage }))
)
const ProfilePage = lazy(() =>
  import('./pages/ProfilePage').then((m) => ({ default: m.ProfilePage }))
)

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 30,
      gcTime: 1000 * 60 * 5,
      retry: 1,
    },
  },
})

// Restore the logged-in session from localStorage on first load.
function SessionBootstrap() {
  const { setUser, setToken } = useAuthStore()

  useEffect(() => {
    const restore = async () => {
      const token = apiClient.getToken()
      const userId = apiClient.getUserId()
      if (!token || !userId) return
      setToken(token)
      try {
        const user = await apiClient.getUserById(userId)
        setUser(user)
      } catch (error) {
        // 401 is already handled by the interceptor (token cleared + redirect).
      }
    }
    restore()
  }, [setUser, setToken])

  return null
}

function RequireAuth({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuthStore()
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}

// Ambient decorative background shared by every page.
function AppBackground() {
  return (
    <div className="pointer-events-none fixed inset-0 z-0" aria-hidden="true">
      <div className="absolute inset-0 bg-ink-radial" />
      <div className="absolute -top-40 left-1/2 h-[480px] w-[820px] -translate-x-1/2 rounded-full bg-brand-500/10 blur-[140px]" />
      <div className="absolute right-[-160px] top-1/3 h-[360px] w-[360px] rounded-full bg-slate-500/10 blur-[120px]" />
      <div className="absolute bottom-[-180px] left-[-120px] h-[380px] w-[380px] rounded-full bg-brand-500/5 blur-[120px]" />
      <div className="absolute inset-0 bg-slate-950/60" />
    </div>
  )
}

interface ErrorBoundaryProps {
  children: ReactNode
}

interface ErrorBoundaryState {
  hasError: boolean
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { hasError: false }

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true }
  }

  componentDidCatch(error: unknown) {
    console.error('Unhandled render error:', error)
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="relative z-10 mx-auto flex w-full max-w-2xl flex-col items-center gap-6 px-4 py-24 text-center sm:px-6 lg:px-8">
          <h1 className="font-display text-3xl font-semibold text-white">
            Something went wrong
          </h1>
          <p className="text-slate-400">
            An unexpected error occurred. Please reload the page to try again.
          </p>
          <Button onClick={() => window.location.reload()}>Reload page</Button>
        </main>
      )
    }
    return this.props.children
  }
}

const routeTitles: Record<string, string> = {
  '/': 'BidStream – Real-time Online Auctions',
  '/auctions': 'Auctions | BidStream',
  '/login': 'Sign in | BidStream',
  '/seller-hub': 'Seller Hub | BidStream',
  '/notifications': 'Notifications | BidStream',
  '/profile': 'Profile | BidStream',
}

// Scrolls to top and updates the document title on navigation.
function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' as ScrollBehavior })
    document.title = routeTitles[pathname] ?? 'BidStream'
  }, [pathname])
  return null
}

// Animated route transitions keyed by pathname.
function AnimatedRoutes() {
  const location = useLocation()

  return (
    <AnimatePresence mode="wait" initial={false}>
      <motion.main
        key={location.pathname}
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -8 }}
        transition={{ duration: 0.28, ease: 'easeOut' }}
        className="relative z-10 mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 lg:px-8"
      >
        <Suspense
          fallback={
            <div className="flex min-h-[60vh] items-center justify-center">
              <Spinner size="lg" label="Loading" />
            </div>
          }
        >
          <Routes location={location}>
            <Route path="/" element={<HomePage />} />
            <Route path="/auctions" element={<AuctionsPage />} />
            <Route path="/auction/:id" element={<AuctionDetailPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/seller-hub"
              element={
                <RequireAuth>
                  <SellerHubPage />
                </RequireAuth>
              }
            />
            <Route
              path="/notifications"
              element={
                <RequireAuth>
                  <NotificationsPage />
                </RequireAuth>
              }
            />
            <Route
              path="/profile"
              element={
                <RequireAuth>
                  <ProfilePage />
                </RequireAuth>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </motion.main>
    </AnimatePresence>
  )
}

function App() {
  return (
    <MotionConfig reducedMotion="user">
      <QueryClientProvider client={queryClient}>
        <SessionBootstrap />
        <Router>
          <ErrorBoundary>
            <ScrollToTop />
            <div className="relative flex min-h-screen flex-col bg-slate-950 text-slate-100">
              <AppBackground />
              <Navbar />
              <AnimatedRoutes />
            </div>
          </ErrorBoundary>
          <Toaster
            position="top-right"
            theme="dark"
            toastOptions={{
              style: {
                background: '#15171F',
                border: '1px solid #232735',
                color: '#E3E5EB',
                borderRadius: '12px',
              },
            }}
          />
        </Router>
      </QueryClientProvider>
    </MotionConfig>
  )
}

export default App
