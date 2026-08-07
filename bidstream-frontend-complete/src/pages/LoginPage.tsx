import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Mail, Lock, User, Shield, Eye, EyeOff, Gavel, Bell, Zap } from 'lucide-react'
import { apiClient } from '../api/client'
import { useAuthStore } from '../store'
import { AuthCredentials, AuthResponse, RegisterPayload, UserRole } from '../types'
import { toast } from 'sonner'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { Select } from '../components/ui/Select'
import { cn } from '../lib/cn'

// ============================================================================
// Login / Register Page
// ============================================================================

const highlights = [
  { icon: Gavel, text: 'Bid in real-time with instant confirmation' },
  { icon: Bell, text: 'Get notified the moment you’re outbid' },
  { icon: Zap, text: 'List and sell items in minutes' },
]

export const LoginPage: React.FC = () => {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [showPassword, setShowPassword] = useState(false)
  const [credentials, setCredentials] = useState<AuthCredentials>({
    usernameOrEmail: '',
    password: '',
  })
  const [register, setRegister] = useState<RegisterPayload>({
    username: '',
    fullName: '',
    email: '',
    role: UserRole.USER,
    password: '',
  })
  const navigate = useNavigate()
  const { setUser, setToken } = useAuthStore()

  const handleAuthSuccess = async (data: { token: string; userId: number }) => {
    apiClient.setToken(data.token)
    apiClient.setUserId(data.userId)
    setToken(data.token)
    toast.success('Welcome to BidStream!')
    navigate('/auctions')
  }

  const completeAuth = async (data: AuthResponse) => {
    handleAuthSuccess(data)
    try {
      const user = await apiClient.getUserById(data.userId)
      setUser(user)
    } catch {
      // Fall back to the auth payload if the profile fetch fails.
      setUser({
        id: data.userId,
        username: data.username,
        email: data.email,
        fullName: '',
        role: data.role,
        active: true,
        createdAt: new Date().toISOString(),
      })
    }
  }

  const loginMutation = useMutation({
    mutationFn: (creds: AuthCredentials) => apiClient.login(creds),
    onSuccess: (data) => {
      void completeAuth(data)
    },
    onError: (error: any) => toast.error(apiClient.getErrorMessage(error)),
  })

  const registerMutation = useMutation({
    mutationFn: (payload: RegisterPayload) => apiClient.register(payload),
    onSuccess: (data) => {
      void completeAuth(data)
    },
    onError: (error: any) => toast.error(apiClient.getErrorMessage(error)),
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (mode === 'login') {
      loginMutation.mutate(credentials)
    } else {
      registerMutation.mutate(register)
    }
  }

  const isPending = loginMutation.isPending || registerMutation.isPending
  const passwordRightSlot = (
    <button
      type="button"
      onClick={() => setShowPassword((v) => !v)}
      aria-label={showPassword ? 'Hide password' : 'Show password'}
      className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-500 transition-colors hover:text-slate-300"
    >
      {showPassword ? <EyeOff className="h-4 w-4" aria-hidden="true" /> : <Eye className="h-4 w-4" aria-hidden="true" />}
    </button>
  )

  return (
    <div className="grid min-h-[calc(100vh-4rem)] lg:grid-cols-2">
      {/* Branding panel */}
      <div className="relative hidden overflow-hidden border-r border-slate-800 p-12 lg:flex lg:flex-col lg:justify-between">
        <div
          className="pointer-events-none absolute inset-0 bg-ink-radial"
          aria-hidden="true"
        />
        <div
          className="pointer-events-none absolute -left-24 top-1/3 h-80 w-80 rounded-full bg-brand-500/10 blur-[120px]"
          aria-hidden="true"
        />

        <div className="relative">
          <Link to="/" className="flex items-center gap-2.5" aria-label="BidStream home">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-gradient shadow-glow">
              <span className="font-display text-xl font-bold leading-none text-brand-950">B</span>
            </div>
            <span className="font-display text-2xl font-semibold tracking-tight text-white">
              Bid<span className="text-gradient">Stream</span>
            </span>
          </Link>
        </div>

        <div className="relative max-w-md">
          <h2 className="font-display text-4xl font-semibold leading-tight text-white">
            Where every bid
            <br />
            tells a <span className="italic text-gradient">story.</span>
          </h2>
          <p className="mt-4 text-slate-400">
            Join thousands of bidders and sellers on the real-time auction
            marketplace built for modern commerce.
          </p>

          <ul className="mt-10 space-y-4">
            {highlights.map(({ icon: Icon, text }) => (
              <li key={text} className="flex items-center gap-3 text-sm text-slate-300">
                <span className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg border border-brand-500/25 bg-brand-500/10 text-brand-300">
                  <Icon className="h-4 w-4" aria-hidden="true" />
                </span>
                {text}
              </li>
            ))}
          </ul>
        </div>

        <p className="relative text-xs text-slate-400">
          © {new Date().getFullYear()} BidStream. All rights reserved.
        </p>
      </div>

      {/* Form panel */}
      <div className="flex items-center justify-center px-4 py-14 sm:px-8">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
          className="w-full max-w-md"
        >
          <div className="mb-8 text-center lg:hidden">
            <Link to="/" className="inline-flex items-center gap-2.5" aria-label="BidStream home">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-gradient shadow-glow">
                <span className="font-display text-xl font-bold leading-none text-brand-950">B</span>
              </div>
            </Link>
          </div>

          <div className="text-center lg:text-left">
            <p className="mb-1 text-xs font-semibold uppercase tracking-[0.2em] text-brand-400">
              {mode === 'login' ? 'Welcome back' : 'Join us'}
            </p>
            <h1 className="font-display text-3xl font-semibold text-white">
              {mode === 'login' ? 'Sign in to start bidding' : 'Create your account'}
            </h1>
            <p className="mt-2 text-sm text-slate-400">
              {mode === 'login'
                ? 'Enter your credentials to continue'
                : 'It takes less than a minute'}
            </p>
          </div>

          <div className="mt-8">
            {/* Mode Toggle */}
            <div className="mb-6 grid grid-cols-2 gap-1 rounded-xl border border-slate-700 bg-slate-900/70 p-1">
              {(['login', 'register'] as const).map((m) => (
                <button
                  key={m}
                  onClick={() => setMode(m)}
                  aria-pressed={mode === m}
                  className={cn(
                    'rounded-lg py-2 text-sm font-semibold transition-all',
                    mode === m
                      ? 'bg-brand-500 text-brand-950 shadow'
                      : 'text-slate-400 hover:text-white'
                  )}
                >
                  {m === 'login' ? 'Login' : 'Register'}
                </button>
              ))}
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              {mode === 'register' && (
                <>
                  <Input
                    type="text"
                    label="Full name"
                    placeholder="Jane Doe"
                    value={register.fullName}
                    onChange={(e) => setRegister({ ...register, fullName: e.target.value })}
                    icon={<User className="h-4 w-4" aria-hidden="true" />}
                    required
                  />
                  <Input
                    type="text"
                    label="Username"
                    placeholder="janedoe"
                    value={register.username}
                    onChange={(e) => setRegister({ ...register, username: e.target.value })}
                    icon={<User className="h-4 w-4" aria-hidden="true" />}
                    required
                  />
                  <Select
                    label="Role"
                    value={register.role}
                    onChange={(e) =>
                      setRegister({ ...register, role: e.target.value as UserRole })
                    }
                    icon={<Shield className="h-4 w-4" aria-hidden="true" />}
                  >
                    <option value={UserRole.USER}>Bidder (USER)</option>
                    <option value={UserRole.SELLER}>Seller (SELLER)</option>
                    <option value={UserRole.ADMIN}>Admin (ADMIN)</option>
                  </Select>
                </>
              )}

              <Input
                type={mode === 'login' ? 'text' : 'email'}
                label={mode === 'login' ? 'Username or email' : 'Email address'}
                placeholder={mode === 'login' ? 'you@example.com' : 'you@example.com'}
                value={mode === 'login' ? credentials.usernameOrEmail : register.email}
                onChange={(e) =>
                  mode === 'login'
                    ? setCredentials({ ...credentials, usernameOrEmail: e.target.value })
                    : setRegister({ ...register, email: e.target.value })
                }
                icon={<Mail className="h-4 w-4" aria-hidden="true" />}
                autoComplete={mode === 'login' ? 'username' : 'email'}
                required
              />

              <Input
                type={showPassword ? 'text' : 'password'}
                label="Password"
                placeholder="••••••••"
                value={mode === 'login' ? credentials.password : register.password}
                onChange={(e) =>
                  mode === 'login'
                    ? setCredentials({ ...credentials, password: e.target.value })
                    : setRegister({ ...register, password: e.target.value })
                }
                icon={<Lock className="h-4 w-4" aria-hidden="true" />}
                rightSlot={passwordRightSlot}
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                required
              />

              <Button type="submit" fullWidth size="lg" loading={isPending} disabled={isPending}>
                {isPending
                  ? 'Please wait...'
                  : mode === 'login'
                  ? 'Login'
                  : 'Create Account'}
              </Button>
            </form>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
