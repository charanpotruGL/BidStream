import React, { forwardRef } from 'react'
import { cn } from '../../lib/cn'

interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string
  error?: string
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  function Textarea({ label, error, className, id, required, ...rest }, ref) {
    const textareaId =
      id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined)

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={textareaId}
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
        <textarea
          ref={ref}
          id={textareaId}
          aria-invalid={!!error}
          className={cn(
            'w-full rounded-xl border bg-slate-900/70 px-4 py-3 text-slate-100 placeholder:text-slate-500',
            'transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-brand-500/40',
            error
              ? 'border-rose-500/60 focus:border-rose-500'
              : 'border-slate-700 hover:border-slate-600 focus:border-brand-500/70',
            className
          )}
          {...rest}
        />
        {error && (
          <p className="mt-1.5 text-xs text-rose-400" role="alert">
            {error}
          </p>
        )}
      </div>
    )
  }
)
