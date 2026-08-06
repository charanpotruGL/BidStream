import React from 'react'
import { cn } from '../../lib/cn'

interface PanelProps extends React.HTMLAttributes<HTMLDivElement> {
  hover?: boolean
}

export const Panel: React.FC<PanelProps> = ({ hover = false, className, children, ...rest }) => {
  return (
    <div
      className={cn(
        'rounded-2xl border border-slate-700/70 bg-slate-800/60 backdrop-blur-sm shadow-card',
        hover &&
          'transition-all duration-300 hover:-translate-y-1 hover:border-slate-600 hover:shadow-card-hover',
        className
      )}
      {...rest}
    >
      {children}
    </div>
  )
}
