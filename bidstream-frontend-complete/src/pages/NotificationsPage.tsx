import React from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Bell, CheckCheck, Trash2 } from 'lucide-react'
import { apiClient } from '../api/client'
import { Notification } from '../types'
import { toast } from 'sonner'
import { PageHeader } from '../components/ui/PageHeader'
import { Button } from '../components/ui/Button'
import { Panel } from '../components/ui/Panel'
import { Skeleton } from '../components/ui/Skeleton'
import { EmptyState } from '../components/ui/EmptyState'
import { NotificationIcon } from '../components/ui/NotificationIcon'
import { cn } from '../lib/cn'

// ============================================================================
// Notifications Page
// ============================================================================

export const NotificationsPage: React.FC = () => {
  const queryClient = useQueryClient()

  const { data: notifications = [], isLoading } = useQuery({
    queryKey: ['notifications', 'list'],
    queryFn: () => apiClient.getMyNotifications(),
    refetchInterval: 30000,
  })

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['notifications', 'list'] })
    queryClient.invalidateQueries({ queryKey: ['notifications', 'count'] })
  }

  const readMutation = useMutation({
    mutationFn: (id: number) => apiClient.markNotificationAsRead(id),
    onSuccess: refresh,
    onError: (e: any) => toast.error(apiClient.getErrorMessage(e)),
  })

  const readAllMutation = useMutation({
    mutationFn: () => apiClient.markAllNotificationsAsRead(),
    onSuccess: refresh,
    onError: (e: any) => toast.error(apiClient.getErrorMessage(e)),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteNotification(id),
    onSuccess: refresh,
    onError: (e: any) => toast.error(apiClient.getErrorMessage(e)),
  })

  const unreadCount = notifications.filter((n) => !n.read).length

  return (
    <div className="mx-auto flex w-full max-w-3xl flex-col gap-8">
      <PageHeader
        eyebrow="Inbox"
        title="Notifications"
        description="Auction and bid activity, all in one place"
        actions={
          unreadCount > 0 ? (
            <Button variant="secondary" size="sm" onClick={() => readAllMutation.mutate()}>
              <CheckCheck className="h-4 w-4" aria-hidden="true" />
              Mark all read
            </Button>
          ) : undefined
        }
      />

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-24" />
          ))}
        </div>
      ) : notifications.length === 0 ? (
        <EmptyState
          icon={<Bell className="h-8 w-8" aria-hidden="true" />}
          title="No notifications"
          description="You'll see auction and bid updates here."
        />
      ) : (
        <div className="space-y-3">
          {notifications.map((notification: Notification, index) => (
            <motion.div
              key={notification.id}
              initial={{ opacity: 0, x: -12 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: Math.min(index * 0.03, 0.3) }}
            >
              <Panel
                hover
                className={cn(
                  'flex items-start gap-4 p-4',
                  !notification.read && 'border-brand-500/40'
                )}
              >
                <NotificationIcon type={notification.notificationType} />

                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-semibold text-white">
                      {notification.title}
                    </p>
                    {!notification.read && (
                      <span className="rounded-full bg-brand-500/15 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-brand-300">
                        New
                      </span>
                    )}
                  </div>
                  <p className="mt-1 text-sm leading-relaxed text-slate-400">
                    {notification.message}
                  </p>
                  <p className="mt-1.5 text-xs text-slate-500">
                    {new Date(notification.createdAt).toLocaleString()}
                  </p>
                </div>

                <div className="flex flex-shrink-0 flex-col gap-1.5">
                  {!notification.read && (
                    <button
                      onClick={() => readMutation.mutate(notification.id)}
                      className="flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-slate-300 transition-colors hover:bg-slate-800 hover:text-white"
                    >
                      <CheckCheck className="h-3.5 w-3.5" aria-hidden="true" />
                      Read
                    </button>
                  )}
                  <button
                    onClick={() => deleteMutation.mutate(notification.id)}
                    className="flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-slate-400 transition-colors hover:bg-rose-500/10 hover:text-rose-300"
                  >
                    <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                    Delete
                  </button>
                </div>
              </Panel>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  )
}
