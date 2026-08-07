import React, { useState, useEffect, useRef } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { motion, AnimatePresence } from 'framer-motion'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { Bell, LogOut, Menu, X, Search, ChevronDown } from 'lucide-react'
import { useAuthStore } from '../../store'
import { apiClient } from '../../api/client'
import { useNotifications } from '../../hooks/useNotifications'
import { Notification } from '../../types'
import { getInitials } from '../../utils'
import { NotificationIcon } from '../ui/NotificationIcon'
import { Button } from '../ui/Button'
import { cn } from '../../lib/cn'

// ============================================================================
// Navbar Component
// ============================================================================

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    'relative text-sm font-medium transition-colors duration-200',
    isActive ? 'text-white' : 'text-slate-400 hover:text-white'
  )

export const Navbar: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false)
  const [isNotificationOpen, setIsNotificationOpen] = useState(false)
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const notificationRef = useRef<HTMLDivElement>(null)
  const userMenuRef = useRef<HTMLDivElement>(null)
  const { user, isAuthenticated, logout } = useAuthStore()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { unreadCount, notifications, markAsRead, markAllAsRead } =
    useNotifications({ enabled: isAuthenticated })

  // Close dropdowns on outside click
  useEffect(() => {
    const onClick = (e: MouseEvent) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(e.target as Node)
      ) {
        setIsNotificationOpen(false)
      }
      if (userMenuRef.current && !userMenuRef.current.contains(e.target as Node)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [])

  const handleLogout = () => {
    logout()
    apiClient.clearToken()
    apiClient.clearUserId()
    queryClient.clear()
    navigate('/login')
  }

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const query = searchQuery.trim()
    navigate(query ? `/auctions?q=${encodeURIComponent(query)}` : '/auctions')
    setSearchQuery('')
  }

  const handleNotificationClick = (notification: Notification) => {
    if (!notification.read) {
      markAsRead(notification.id)
    }
  }

  return (
    <header className="sticky top-0 z-50 border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl">
      <nav className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8" aria-label="Main">
        <div className="flex h-16 items-center justify-between gap-4">
          {/* Logo */}
          <Link to="/" className="flex shrink-0 items-center gap-2.5" aria-label="BidStream home">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-gradient shadow-glow">
              <span className="font-display text-lg font-bold leading-none text-brand-950">B</span>
            </div>
            <span className="font-display text-xl font-semibold tracking-tight text-white">
              Bid<span className="text-gradient">Stream</span>
            </span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden flex-1 items-center justify-center gap-8 md:flex">
            <form onSubmit={handleSearchSubmit} className="relative w-full max-w-xs">
              <Search
                className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500"
                aria-hidden="true"
              />
              <input
                type="search"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search auctions..."
                aria-label="Search auctions"
                className="h-10 w-full rounded-full border border-slate-700 bg-slate-900/70 pl-10 pr-4 text-sm text-slate-100 placeholder:text-slate-500 transition-colors focus:border-brand-500/60 focus:outline-none focus:ring-2 focus:ring-brand-500/30"
              />
            </form>

            <NavLink to="/auctions" className={navLinkClass}>
              Auctions
            </NavLink>
            <NavLink to="/seller-hub" className={navLinkClass}>
              Sell
            </NavLink>
          </div>

          {/* Right Section */}
          <div className="flex items-center gap-1.5 sm:gap-3">
            {/* Notification Bell */}
            {isAuthenticated && (
              <div className="relative" ref={notificationRef}>
                <button
                  onClick={() => setIsNotificationOpen(!isNotificationOpen)}
                  aria-label={`Notifications${unreadCount > 0 ? `, ${unreadCount} unread` : ''}`}
                  aria-expanded={isNotificationOpen}
                  className="relative flex h-10 w-10 items-center justify-center rounded-xl text-slate-300 transition-colors hover:bg-slate-800 hover:text-white"
                >
                  <Bell className="h-5 w-5" />
                  {unreadCount > 0 && (
                    <motion.span
                      initial={{ scale: 0 }}
                      animate={{ scale: 1 }}
                      className="absolute right-1.5 top-1.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-brand-500 px-1 text-[10px] font-bold text-brand-950"
                    >
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </motion.span>
                  )}
                </button>

                {/* Notification Dropdown */}
                <AnimatePresence>
                  {isNotificationOpen && (
                    <motion.div
                      initial={{ opacity: 0, y: -8, scale: 0.98 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: -8, scale: 0.98 }}
                      transition={{ duration: 0.16 }}
                      className="absolute right-0 mt-2 w-80 overflow-hidden rounded-2xl border border-slate-700/70 bg-slate-900/95 shadow-card backdrop-blur-xl"
                    >
                      <div className="flex items-center justify-between border-b border-slate-800 px-4 py-3">
                        <h3 className="text-sm font-semibold text-white">
                          Notifications
                        </h3>
                        {unreadCount > 0 && (
                          <button
                            onClick={markAllAsRead}
                            className="text-xs font-medium text-brand-400 transition-colors hover:text-brand-300"
                          >
                            Mark all as read
                          </button>
                        )}
                      </div>

                      <div className="max-h-96 overflow-y-auto">
                        {notifications.length === 0 ? (
                          <div className="px-6 py-12 text-center">
                            <Bell className="mx-auto mb-3 h-8 w-8 text-slate-600" aria-hidden="true" />
                            <p className="text-sm text-slate-400">No notifications yet</p>
                          </div>
                        ) : (
                          notifications.slice(0, 8).map((notif) => (
                            <div
                              key={notif.id}
                              onClick={() => handleNotificationClick(notif)}
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter' || e.key === ' ') {
                                  e.preventDefault()
                                  handleNotificationClick(notif)
                                }
                              }}
                              className={cn(
                                'flex cursor-pointer gap-3 border-b border-slate-800 px-4 py-3 transition-colors last:border-b-0',
                                notif.read
                                  ? 'bg-transparent hover:bg-slate-800/60'
                                  : 'bg-brand-500/5 hover:bg-slate-800/60'
                              )}
                            >
                              <NotificationIcon type={notif.notificationType} size="sm" className="mt-0.5" />
                              <div className="min-w-0 flex-1">
                                <p className="line-clamp-1 text-sm font-semibold text-white">
                                  {notif.title}
                                </p>
                                <p className="mt-0.5 line-clamp-2 text-xs text-slate-400">
                                  {notif.message}
                                </p>
                                <p className="mt-1 text-[11px] text-slate-400">
                                  {new Date(notif.createdAt).toLocaleString()}
                                </p>
                              </div>
                              {!notif.read && (
                                <span className="mt-1.5 h-1.5 w-1.5 flex-shrink-0 rounded-full bg-brand-400" aria-hidden="true" />
                              )}
                            </div>
                          ))
                        )}
                      </div>

                      <div className="border-t border-slate-800 p-2">
                        <Link
                          to="/notifications"
                          onClick={() => setIsNotificationOpen(false)}
                          className="block rounded-lg px-3 py-2 text-center text-sm font-medium text-brand-400 transition-colors hover:bg-slate-800 hover:text-brand-300"
                        >
                          View all notifications
                        </Link>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            )}

            {/* Login / Sign up */}
            {!isAuthenticated && (
              <Button to="/login" size="sm" className="hidden md:inline-flex">
                Login
              </Button>
            )}

            {/* User Menu */}
            <div className="relative hidden md:block" ref={userMenuRef}>
              {isAuthenticated && (
                <>
                  <button
                    onClick={() => setIsOpen(!isOpen)}
                    aria-expanded={isOpen}
                    className="flex h-10 items-center gap-2 rounded-full border border-slate-700 bg-slate-800/80 py-1 pl-1 pr-3 transition-colors hover:border-slate-600"
                  >
                    <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-gradient text-xs font-bold text-brand-950">
                      {getInitials(user?.fullName || user?.username || '')}
                    </span>
                    <span className="max-w-[110px] truncate text-sm font-medium text-slate-200">
                      {user?.username}
                    </span>
                    <ChevronDown className="h-3.5 w-3.5 text-slate-500" aria-hidden="true" />
                  </button>

                  <AnimatePresence>
                    {isOpen && (
                      <motion.div
                        initial={{ opacity: 0, y: -8, scale: 0.98 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        exit={{ opacity: 0, y: -8, scale: 0.98 }}
                        transition={{ duration: 0.16 }}
                        className="absolute right-0 mt-2 w-52 overflow-hidden rounded-xl border border-slate-700/70 bg-slate-900/95 py-1.5 shadow-card backdrop-blur-xl"
                      >
                        <div className="border-b border-slate-800 px-4 py-2.5">
                          <p className="truncate text-sm font-semibold text-white">
                            {user?.fullName || user?.username}
                          </p>
                          <p className="truncate text-xs text-slate-400">{user?.email}</p>
                        </div>
                        <Link
                          to="/seller-hub"
                          onClick={() => setIsOpen(false)}
                          className="block px-4 py-2 text-sm text-slate-300 transition-colors hover:bg-slate-800 hover:text-white"
                        >
                          Seller Hub
                        </Link>
                        <Link
                          to="/profile"
                          onClick={() => setIsOpen(false)}
                          className="block px-4 py-2 text-sm text-slate-300 transition-colors hover:bg-slate-800 hover:text-white"
                        >
                          My Profile
                        </Link>
                        <button
                          onClick={handleLogout}
                          className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-rose-300 transition-colors hover:bg-slate-800"
                        >
                          <LogOut className="h-4 w-4" aria-hidden="true" />
                          <span>Logout</span>
                        </button>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </>
              )}
            </div>

            {/* Mobile Menu Toggle */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              aria-label="Toggle navigation menu"
              aria-expanded={isMobileMenuOpen}
              className="flex h-10 w-10 items-center justify-center rounded-xl text-slate-300 transition-colors hover:bg-slate-800 hover:text-white md:hidden"
            >
              {isMobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        <AnimatePresence>
          {isMobileMenuOpen && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.2 }}
              className="overflow-hidden border-t border-slate-800 py-3 md:hidden"
            >
              <div className="space-y-1">
                <NavLink
                  to="/auctions"
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    cn(
                      'block rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                      isActive ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-800'
                    )
                  }
                >
                  Auctions
                </NavLink>
                <NavLink
                  to="/seller-hub"
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    cn(
                      'block rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                      isActive ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-800'
                    )
                  }
                >
                  Sell
                </NavLink>
                {!isAuthenticated && (
                  <Link
                    to="/login"
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="block rounded-lg px-3 py-2 text-sm font-medium text-brand-400"
                  >
                    Login
                  </Link>
                )}
                {isAuthenticated && (
                  <button
                    onClick={() => {
                      setIsMobileMenuOpen(false)
                      handleLogout()
                    }}
                    className="block w-full rounded-lg px-3 py-2 text-left text-sm font-medium text-rose-300 transition-colors hover:bg-slate-800"
                  >
                    Logout
                  </button>
                )}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </nav>
    </header>
  )
}
