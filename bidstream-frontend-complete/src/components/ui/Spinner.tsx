import React from 'react'
import { Loader2 } from 'lucide-react'
import { cn } from '../../lib/cn'

const sizeClasses = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-9 w-9',
}

interface SpinnerProps {
  size?: keyof typeof sizeClasses
  className?: string
  label?: string
}

export const Spinner: React.FC<SpinnerProps> = ({
  size = 'md',
  className,
  label = 'Loading',
}) => {
  return (
    <Loader2
      className={cn('animate-spin text-brand-500', sizeClasses[size], className)}
      role="status"
      aria-label={label}
    />
  )
}
