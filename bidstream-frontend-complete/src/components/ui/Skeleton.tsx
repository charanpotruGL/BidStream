import React from 'react'
import { cn } from '../../lib/cn'

interface SkeletonProps {
  className?: string
}

export const Skeleton: React.FC<SkeletonProps> = ({ className }) => {
  return (
    <div
      aria-hidden="true"
      className={cn(
        'relative overflow-hidden rounded-xl bg-slate-800/80',
        className
      )}
    >
      <span className="absolute inset-0 animate-shimmer bg-gradient-to-r from-transparent via-white/5 to-transparent" />
    </div>
  )
}
