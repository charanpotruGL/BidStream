import React from 'react'
import { Mail, CalendarDays } from 'lucide-react'
import { useAuthStore } from '../store'
import { UserRole } from '../types'
import { getInitials, formatDateTime } from '../utils'
import { PageHeader } from '../components/ui/PageHeader'
import { Panel } from '../components/ui/Panel'
import { Badge } from '../components/ui/Badge'
import { WonAuctions } from '../components/profile/WonAuctions'

// ============================================================================
// Profile Page - account details + auctions won by the current bidder
// ============================================================================

const roleLabels: Record<UserRole, string> = {
  [UserRole.USER]: 'Bidder',
  [UserRole.SELLER]: 'Seller',
  [UserRole.ADMIN]: 'Admin',
}

const roleTone: Record<UserRole, 'brand' | 'sky' | 'rose'> = {
  [UserRole.USER]: 'brand',
  [UserRole.SELLER]: 'sky',
  [UserRole.ADMIN]: 'rose',
}

export const ProfilePage: React.FC = () => {
  const { user } = useAuthStore()

  if (!user) return null

  return (
    <div className="flex flex-col gap-8">
      <PageHeader
        eyebrow="Your Account"
        title="Profile"
        description="Your account details and auctions you&rsquo;ve won."
      />

      <Panel className="p-6 sm:p-8">
        <div className="flex flex-col gap-6 sm:flex-row sm:items-center">
          <span className="flex h-20 w-20 flex-shrink-0 items-center justify-center rounded-full border border-brand-500/50 bg-brand-gradient text-2xl font-bold text-brand-950">
            {getInitials(user.fullName || user.username)}
          </span>
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-3">
              <h2 className="font-display text-2xl font-semibold text-white">
                {user.fullName}
              </h2>
              <Badge tone={roleTone[user.role]}>{roleLabels[user.role]}</Badge>
            </div>
            <p className="mt-0.5 text-sm text-slate-400">@{user.username}</p>
          </div>
        </div>

        <div className="mt-6 grid grid-cols-1 gap-4 border-t border-slate-700/70 pt-6 sm:grid-cols-2">
          <div className="flex items-center gap-3 rounded-xl border border-slate-700/70 bg-slate-900/40 px-4 py-3">
            <Mail className="h-4 w-4 flex-shrink-0 text-brand-300" aria-hidden="true" />
            <div className="min-w-0">
              <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                Email
              </p>
              <p className="truncate text-sm font-semibold text-white">{user.email}</p>
            </div>
          </div>
          <div className="flex items-center gap-3 rounded-xl border border-slate-700/70 bg-slate-900/40 px-4 py-3">
            <CalendarDays className="h-4 w-4 flex-shrink-0 text-brand-300" aria-hidden="true" />
            <div className="min-w-0">
              <p className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
                Joined
              </p>
              <p className="truncate text-sm font-semibold text-white">
                {formatDateTime(user.createdAt)}
              </p>
            </div>
          </div>
        </div>
      </Panel>

      <WonAuctions bidderId={user.id} />
    </div>
  )
}
