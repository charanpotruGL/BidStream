import React from 'react'
import { Link } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { cn } from '../../lib/cn'

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'outline' | 'danger'
type ButtonSize = 'sm' | 'md' | 'lg'

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    'bg-brand-500 text-brand-950 hover:bg-brand-400 shadow-[inset_0_1px_0_rgba(255,255,255,0.25)] hover:shadow-glow',
  secondary:
    'bg-slate-800 text-slate-100 border border-slate-700 hover:bg-slate-750 hover:border-slate-600',
  ghost: 'text-slate-300 hover:text-white hover:bg-slate-800',
  outline:
    'border border-slate-600 text-slate-200 hover:bg-slate-800 hover:border-brand-500/60 hover:text-white',
  danger:
    'bg-rose-500/10 text-rose-300 border border-rose-500/30 hover:bg-rose-500/20 hover:text-rose-200',
}

const sizeClasses: Record<ButtonSize, string> = {
  sm: 'h-9 px-3 text-xs gap-1.5',
  md: 'h-11 px-5 text-sm gap-2',
  lg: 'h-12 px-6 text-base gap-2',
}

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
  loading?: boolean
  fullWidth?: boolean
  to?: string
  href?: string
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  loading = false,
  fullWidth = false,
  to,
  href,
  className,
  children,
  disabled,
  ...rest
}) => {
  const classes = cn(
    'inline-flex items-center justify-center font-semibold rounded-xl transition-all duration-200',
    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500/60 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950',
    'disabled:opacity-50 disabled:pointer-events-none select-none whitespace-nowrap',
    variantClasses[variant],
    sizeClasses[size],
    fullWidth && 'w-full',
    className
  )

  const content = (
    <>
      {loading && <Loader2 className="w-4 h-4 animate-spin" aria-hidden="true" />}
      {children}
    </>
  )

  if (to) {
    return (
      <Link to={to} className={classes} aria-busy={loading} {...(rest as object)}>
        {content}
      </Link>
    )
  }

  if (href) {
    return (
      <a href={href} className={classes} aria-busy={loading} {...(rest as object)}>
        {content}
      </a>
    )
  }

  return (
    <button
      className={classes}
      disabled={disabled || loading}
      aria-busy={loading}
      {...rest}
    >
      {content}
    </button>
  )
}
