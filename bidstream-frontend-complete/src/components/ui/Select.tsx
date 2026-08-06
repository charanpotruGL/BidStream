import React, { forwardRef } from 'react'
import { ChevronDown } from 'lucide-react'
import { cn } from '../../lib/cn'

interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  error?: string
  icon?: React.ReactNode
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { label, error, icon, className, id, required, children, ...rest },
  ref
) {
  const selectId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined)

  return (
    <div className="w-full">
      {label && (
        <label
          htmlFor={selectId}
          className="mb-1.5 flex items-center gap-1 text-sm font-medium text-slate-300"
        >
          {label}
          {required && (
            <span className="text-brand-400" aria-hidden="true">
              *
            </span>
          )}
        </label>
      )}
      <div className="relative">
        {icon && (
          <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-500">
            {icon}
          </span>
        )}
        <select
          ref={ref}
          id={selectId}
          aria-invalid={!!error}
          className={cn(
            'h-11 w-full appearance-none rounded-xl border bg-slate-900/70 text-slate-100',
            'transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-brand-500/40',
            icon ? 'pl-11' : 'pl-4',
            'pr-10',
            error
              ? 'border-rose-500/60 focus:border-rose-500'
              : 'border-slate-700 hover:border-slate-600 focus:border-brand-500/70',
            className
          )}
          {...rest}
        >
          {children}
        </select>
        <ChevronDown
          className="pointer-events-none absolute inset-y-0 right-3 my-auto h-4 w-4 text-slate-500"
          aria-hidden="true"
        />
      </div>
      {error && (
        <p className="mt-1.5 text-xs text-rose-400" role="alert">
          {error}
        </p>
      )}
    </div>
  )
})
