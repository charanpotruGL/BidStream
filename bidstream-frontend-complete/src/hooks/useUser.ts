import { useQuery } from '@tanstack/react-query'
import { apiClient } from '../api/client'

/**
 * Resolve a user id to a full User record. TanStack Query dedupes by key, so
 * rendering many bidders (e.g. the live bid activity feed) results in a single
 * request per unique user, cached for 5 minutes.
 */
export function useUser(userId: number | null | undefined) {
  return useQuery({
    queryKey: ['user', userId],
    queryFn: () => apiClient.getUserById(userId!),
    enabled: userId != null,
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 30,
    retry: 1,
  })
}
