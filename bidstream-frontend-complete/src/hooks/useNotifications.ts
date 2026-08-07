import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '../api/client'
import { Notification } from '../types'
import { toast } from 'sonner'

export interface UseNotificationsOptions {
  enabled?: boolean
}

export interface UseNotificationsResult {
  notifications: Notification[]
  unreadCount: number
  isLoading: boolean
  markAsRead: (id: number) => void
  markAllAsRead: () => void
  removeNotification: (id: number) => void
  refresh: () => void
}

/**
 * Single source of truth for notification data. The Navbar bell and the
 * Notifications page share these query keys, so TanStack Query dedupes the
 * fetch and both views stay in sync after mutations.
 */
export function useNotifications({
  enabled = true,
}: UseNotificationsOptions = {}): UseNotificationsResult {
  const queryClient = useQueryClient()

  const listQuery = useQuery({
    queryKey: ['notifications', 'list'],
    queryFn: () => apiClient.getMyNotifications(),
    enabled,
    refetchInterval: enabled ? 30000 : false,
  })

  const notifications = listQuery.data ?? []
  const unreadCount = notifications.filter((n) => !n.read).length

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['notifications'] })
  }

  const markAsReadMutation = useMutation({
    mutationFn: (id: number) => apiClient.markNotificationAsRead(id),
    onMutate: async (id: number) => {
      await queryClient.cancelQueries({ queryKey: ['notifications', 'list'] })
      const previous = queryClient.getQueryData<Notification[]>(['notifications', 'list'])
      queryClient.setQueryData<Notification[]>(['notifications', 'list'], (old) =>
        old?.map((n) => (n.id === id ? { ...n, read: true } : n))
      )
      return { previous }
    },
    onError: (error: unknown, _id, context) => {
      if (context?.previous) {
        queryClient.setQueryData(['notifications', 'list'], context.previous)
      }
      toast.error(apiClient.getErrorMessage(error))
    },
    onSettled: () => refresh(),
  })

  const markAllAsReadMutation = useMutation({
    mutationFn: () => apiClient.markAllNotificationsAsRead(),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ['notifications', 'list'] })
      const previous = queryClient.getQueryData<Notification[]>(['notifications', 'list'])
      queryClient.setQueryData<Notification[]>(['notifications', 'list'], (old) =>
        old?.map((n) => ({ ...n, read: true }))
      )
      return { previous }
    },
    onError: (error: unknown, _id, context) => {
      if (context?.previous) {
        queryClient.setQueryData(['notifications', 'list'], context.previous)
      }
      toast.error(apiClient.getErrorMessage(error))
    },
    onSettled: () => refresh(),
  })

  const removeMutation = useMutation({
    mutationFn: (id: number) => apiClient.deleteNotification(id),
    onMutate: async (id: number) => {
      await queryClient.cancelQueries({ queryKey: ['notifications', 'list'] })
      const previous = queryClient.getQueryData<Notification[]>(['notifications', 'list'])
      queryClient.setQueryData<Notification[]>(['notifications', 'list'], (old) =>
        old?.filter((n) => n.id !== id)
      )
      return { previous }
    },
    onError: (error: unknown, _id, context) => {
      if (context?.previous) {
        queryClient.setQueryData(['notifications', 'list'], context.previous)
      }
      toast.error(apiClient.getErrorMessage(error))
    },
    onSettled: () => refresh(),
  })

  return {
    notifications,
    unreadCount,
    isLoading: listQuery.isLoading,
    markAsRead: (id) => markAsReadMutation.mutate(id),
    markAllAsRead: () => markAllAsReadMutation.mutate(),
    removeNotification: (id) => removeMutation.mutate(id),
    refresh,
  }
}
