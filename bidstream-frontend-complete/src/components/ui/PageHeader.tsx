import React from 'react'
import { cn } from '../../lib/cn'

interface PageHeaderProps {
  title: string
  eyebrow?: string
  description?: string
  actions?: React.ReactNode
  className?: string
}

export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  eyebrow,
  description,
  actions,
  className,
}) => {
  return (
    <div
      className={cn(
        'flex flex-col gap-6 md:flex-row md:items-end md:justify-between',
        className
      )}
    >
      <div className="max-w-2xl">
        {eyebrow && (
          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.2em] text-brand-400">
            {eyebrow}
          </p>
        )}
        <h1 className="font-display text-3xl font-semibold text-white md:text-4xl">
          {title}
        </h1>
        {description && <p className="mt-2 text-base text-slate-400">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-3">{actions}</div>}
    </div>
  )
}
