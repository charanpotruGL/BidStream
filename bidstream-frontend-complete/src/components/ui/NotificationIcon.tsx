import React from 'react'
import {
  Target,
  Rocket,
  CheckCircle2,
  Banknote,
  BellRing,
  Info,
  Megaphone,
  type LucideIcon,
} from 'lucide-react'
import { NotificationType } from '../../types'
import { cn } from '../../lib/cn'
import type { BadgeTone } from './Badge'

interface NotificationMeta {
  Icon: LucideIcon
  tone: BadgeTone
  tile: string
}

const meta: Record<NotificationType, NotificationMeta> = {
  [NotificationType.AUCTION_CREATED]: {
    Icon: Target,
    tone: 'brand',
    tile: 'bg-brand-500/15 text-brand-300 border-brand-500/25',
  },
  [NotificationType.AUCTION_STARTED]: {
    Icon: Rocket,
    tone: 'sky',
    tile: 'bg-sky-500/15 text-sky-300 border-sky-500/25',
  },
  [NotificationType.AUCTION_CLOSED]: {
    Icon: CheckCircle2,
    tone: 'emerald',
    tile: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/25',
  },
  [NotificationType.BID_PLACED]: {
    Icon: Banknote,
    tone: 'emerald',
    tile: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/25',
  },
  [NotificationType.BID_OUTBID]: {
    Icon: BellRing,
    tone: 'amber',
    tile: 'bg-amber-500/15 text-amber-300 border-amber-500/25',
  },
  [NotificationType.INFO]: {
    Icon: Info,
    tone: 'slate',
    tile: 'bg-slate-500/15 text-slate-300 border-slate-500/25',
  },
}

const fallbackMeta: NotificationMeta = {
  Icon: Megaphone,
  tone: 'slate',
  tile: 'bg-slate-500/15 text-slate-300 border-slate-500/25',
}

function getNotificationMeta(type: NotificationType): NotificationMeta {
  return meta[type] ?? fallbackMeta
}

interface NotificationIconProps {
  type: NotificationType
  size?: 'sm' | 'md'
  className?: string
}

export const NotificationIcon: React.FC<NotificationIconProps> = ({
  type,
  size = 'md',
  className,
}) => {
  const { Icon, tile } = getNotificationMeta(type)
  return (
    <span
      className={cn(
        'flex flex-shrink-0 items-center justify-center rounded-xl border',
        tile,
        size === 'md' ? 'h-10 w-10' : 'h-8 w-8',
        className
      )}
    >
      <Icon className={size === 'md' ? 'h-5 w-5' : 'h-4 w-4'} aria-hidden="true" />
    </span>
  )
}
