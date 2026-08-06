import { useEffect, type ReactNode } from 'react'
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
import { HomePage } from './pages/HomePage'
import { AuctionsPage } from './pages/AuctionsPage'
import { AuctionDetailPage } from './pages/AuctionDetailPage'
import { LoginPage } from './pages/LoginPage'
import { SellerHubPage } from './pages/SellerHubPage'
import { NotificationsPage } from './pages/NotificationsPage'
import { apiClient } from './api/client'
import { useAuthStore } from './store'

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
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </motion.main>
    </AnimatePresence>
  )
}

function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' as ScrollBehavior })
  }, [pathname])
  return null
}

function App() {
  return (
    <MotionConfig reducedMotion="user">
      <QueryClientProvider client={queryClient}>
        <SessionBootstrap />
        <Router>
          <ScrollToTop />
          <div className="relative flex min-h-screen flex-col bg-slate-950 text-slate-100">
            <AppBackground />
            <Navbar />
            <AnimatedRoutes />
          </div>
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
