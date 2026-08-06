import React from 'react'
import { cn } from '../../lib/cn'

export type BadgeTone = 'brand' | 'emerald' | 'sky' | 'amber' | 'rose' | 'slate'

const toneClasses: Record<BadgeTone, { pill: string; dot: string }> = {
  brand: {
    pill: 'bg-brand-500/15 text-brand-300 border-brand-500/30',
    dot: 'bg-brand-400',
  },
  emerald: {
    pill: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
    dot: 'bg-emerald-400',
  },
  sky: {
    pill: 'bg-sky-500/15 text-sky-300 border-sky-500/30',
    dot: 'bg-sky-400',
  },
  amber: {
    pill: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
    dot: 'bg-amber-400',
  },
  rose: {
    pill: 'bg-rose-500/15 text-rose-300 border-rose-500/30',
    dot: 'bg-rose-400',
  },
  slate: {
    pill: 'bg-slate-500/15 text-slate-300 border-slate-500/30',
    dot: 'bg-slate-400',
  },
}

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  tone?: BadgeTone
  dot?: boolean
  pulse?: boolean
}

export const Badge: React.FC<BadgeProps> = ({
  tone = 'slate',
  dot = false,
  pulse = false,
  className,
  children,
  ...rest
}) => {
  const styles = toneClasses[tone]
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-semibold tracking-wide',
        styles.pill,
        className
      )}
      {...rest}
    >
      {dot && (
        <span
          className={cn(
            'h-1.5 w-1.5 rounded-full',
            styles.dot,
            pulse && 'animate-pulse-dot'
          )}
          aria-hidden="true"
        />
      )}
      {children}
    </span>
  )
}
