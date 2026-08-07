import React, { forwardRef } from 'react'
import { cn } from '../../lib/cn'

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  hint?: string
  icon?: React.ReactNode
  rightSlot?: React.ReactNode
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, error, hint, icon, rightSlot, className, id, required, ...rest },
  ref
) {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined)

  return (
    <div className="w-full">
      {label && (
        <label
          htmlFor={inputId}
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
        <input
          ref={ref}
          id={inputId}
          aria-invalid={!!error}
          className={cn(
            'h-11 w-full rounded-xl border bg-slate-900/70 text-slate-100 placeholder:text-slate-500',
            'transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-brand-500/40',
            icon ? 'pl-11' : 'pl-4',
            rightSlot ? 'pr-11' : 'pr-4',
            error
              ? 'border-rose-500/60 focus:border-rose-500'
              : 'border-slate-700 hover:border-slate-600 focus:border-brand-500/70',
            className
          )}
          {...rest}
        />
        {rightSlot && (
          <span className="absolute inset-y-0 right-1.5 flex items-center">{rightSlot}</span>
        )}
      </div>
      {error && (
        <p className="mt-1.5 text-xs text-rose-400" id={`${inputId}-error`} role="alert">
          {error}
        </p>
      )}
      {!error && hint && <p className="mt-1.5 text-xs text-slate-400">{hint}</p>}
    </div>
  )
})
