import React from 'react'
import { cn } from '../../lib/cn'

interface EmptyStateProps {
  icon: React.ReactNode
  title: string
  description?: string
  action?: React.ReactNode
  className?: string
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  action,
  className,
}) => {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-700 bg-slate-900/40 px-6 py-16 text-center',
        className
      )}
    >
      <div className="mb-5 flex h-16 w-16 items-center justify-center rounded-2xl border border-slate-700 bg-slate-800 text-slate-500">
        {icon}
      </div>
      <h3 className="font-display text-xl font-semibold text-slate-100">{title}</h3>
      {description && (
        <p className="mt-2 max-w-sm text-sm text-slate-400">{description}</p>
      )}
      {action && <div className="mt-6">{action}</div>}
    </div>
  )
}
