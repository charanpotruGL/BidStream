import React from 'react'
import { useUser } from '../../hooks/useUser'
import { getInitials } from '../../utils'
import { cn } from '../../lib/cn'

// ============================================================================
// User Identity Components
// Renders a username/initials for a user id, falling back to "Bidder #<id>"
// when the user can no longer be resolved (e.g. deleted account).
// ============================================================================

interface UserNameProps {
  userId: number
  className?: string
}

export const UserName: React.FC<UserNameProps> = ({ userId, className }) => {
  const { data, isSuccess } = useUser(userId)
  if (isSuccess && data) {
    return <span className={className}>{data.username}</span>
  }
  return <span className={className}>Bidder #{userId}</span>
}

interface UserAvatarProps {
  userId: number
  className?: string
}

export const UserAvatar: React.FC<UserAvatarProps> = ({ userId, className }) => {
  const { data, isSuccess } = useUser(userId)
  const label =
    isSuccess && data
      ? getInitials(data.fullName || data.username)
      : `#${userId}`
  return (
    <span
      className={cn(
        'relative z-10 flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full border text-xs font-bold',
        'border-slate-700 bg-slate-800 text-slate-400',
        className
      )}
      title={isSuccess && data ? data.username : undefined}
    >
      {label}
    </span>
  )
}
