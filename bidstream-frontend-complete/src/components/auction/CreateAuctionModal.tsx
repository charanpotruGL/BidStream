import React, { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { motion, AnimatePresence } from 'framer-motion'
import { X, Calendar, DollarSign, Info } from 'lucide-react'
import { CreateAuctionPayload } from '../../types'
import { apiClient } from '../../api/client'
import { useAuthStore } from '../../store'
import { toast } from 'sonner'
import { Input } from '../ui/Input'
import { Textarea } from '../ui/Textarea'
import { Button } from '../ui/Button'

// ============================================================================
// Create Auction Modal Component
// ============================================================================

interface CreateAuctionModalProps {
  isOpen: boolean
  onClose: () => void
}

export const CreateAuctionModal: React.FC<CreateAuctionModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [formData, setFormData] = useState<CreateAuctionPayload>({
    title: '',
    description: '',
    startingPrice: 0,
    sellerId: 0,
    startTime: '',
    endTime: '',
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const { user } = useAuthStore()
  const queryClient = useQueryClient()

  const { mutate: createAuction, isPending } = useMutation({
    mutationFn: (payload: CreateAuctionPayload) =>
      apiClient.createAuction(payload),
    onSuccess: (auction) => {
      toast.success(
        `Auction "${auction.title}" created successfully! Status: ${auction.status}`
      )
      queryClient.invalidateQueries({ queryKey: ['auctions'] })
      setFormData({
        title: '',
        description: '',
        startingPrice: 0,
        sellerId: 0,
        startTime: '',
        endTime: '',
      })
      setErrors({})
      onClose()
    },
    onError: (error: any) => {
      const fieldErrors = error.response?.data?.fieldErrors
      if (fieldErrors) {
        setErrors(fieldErrors)
      } else {
        toast.error(apiClient.getErrorMessage(error))
      }
    },
  })

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'startingPrice' ? parseFloat(value) : value,
    }))
    if (errors[name]) {
      setErrors((prev) => {
        const updated = { ...prev }
        delete updated[name]
        return updated
      })
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    const newErrors: Record<string, string> = {}

    if (!formData.title.trim()) newErrors.title = 'Title is required'
    if (!formData.description.trim())
      newErrors.description = 'Description is required'
    if (!formData.startingPrice || formData.startingPrice <= 0)
      newErrors.startingPrice = 'Starting price must be greater than 0'
    if (!formData.startTime) newErrors.startTime = 'Start time is required'
    if (!formData.endTime) newErrors.endTime = 'End time is required'

    if (new Date(formData.startTime) >= new Date(formData.endTime)) {
      newErrors.endTime = 'End time must be after start time'
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    createAuction({
      ...formData,
      sellerId: user!.id,
    })
  }

  // Format a Date as "YYYY-MM-DDTHH:MM" in the browser's LOCAL timezone.
  // The min/value attributes of datetime-local inputs must use local time;
  // toISOString() returns UTC, which shifts the picker range.
  const toLocalDateTime = (d: Date) => {
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(
      d.getHours()
    )}:${pad(d.getMinutes())}`
  }

  const getMinDateTime = () => {
    const now = new Date()
    now.setMinutes(now.getMinutes() + 1)
    return toLocalDateTime(now)
  }

  const minEndTime = formData.startTime
    ? toLocalDateTime(new Date(new Date(formData.startTime).getTime() + 3600000))
    : ''

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
          onKeyDown={(e) => {
            if (e.key === 'Escape') onClose()
          }}
          className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-950/80 p-4 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="create-auction-title"
        >
          <motion.div
            initial={{ scale: 0.96, y: 16, opacity: 0 }}
            animate={{ scale: 1, y: 0, opacity: 1 }}
            exit={{ scale: 0.96, y: 16, opacity: 0 }}
            transition={{ duration: 0.2 }}
            onClick={(e) => e.stopPropagation()}
            className="my-8 w-full max-w-2xl overflow-hidden rounded-2xl border border-slate-700/70 bg-slate-900 shadow-card"
          >
            <div className="flex items-center justify-between border-b border-slate-800 px-6 py-4">
              <div>
                <p className="text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-400">
                  New listing
                </p>
                <h2
                  id="create-auction-title"
                  className="font-display text-xl font-semibold text-white"
                >
                  Create New Auction
                </h2>
              </div>
              <button
                onClick={onClose}
                aria-label="Close dialog"
                className="flex h-9 w-9 items-center justify-center rounded-xl text-slate-400 transition-colors hover:bg-slate-800 hover:text-white"
              >
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>

            <form
              onSubmit={handleSubmit}
              className="max-h-[calc(100vh-220px)] space-y-5 overflow-y-auto px-6 py-6"
            >
              <Input
                label="Auction Title"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="e.g., Vintage Rolex Watch"
                error={errors.title}
                required
                autoFocus
              />

              <Textarea
                label="Description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Describe your item in detail..."
                rows={3}
                error={errors.description}
                required
                className="resize-none"
              />

              <Input
                label="Starting Price"
                name="startingPrice"
                type="number"
                value={formData.startingPrice || ''}
                onChange={handleChange}
                placeholder="0.00"
                step="0.01"
                min="0"
                icon={<DollarSign className="h-4 w-4" aria-hidden="true" />}
                error={errors.startingPrice}
                required
              />

              <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
                <Input
                  label="Start Time"
                  name="startTime"
                  type="datetime-local"
                  defaultValue={formData.startTime}
                  onChange={handleChange}
                  min={getMinDateTime()}
                  icon={<Calendar className="h-4 w-4" aria-hidden="true" />}
                  error={errors.startTime}
                  required
                />

                <Input
                  label="End Time"
                  name="endTime"
                  type="datetime-local"
                  defaultValue={formData.endTime}
                  onChange={handleChange}
                  min={minEndTime}
                  icon={<Calendar className="h-4 w-4" aria-hidden="true" />}
                  error={errors.endTime}
                  required
                />
              </div>

              <div className="flex items-start gap-3 rounded-xl border border-brand-500/25 bg-brand-500/5 p-3.5">
                <Info className="mt-0.5 h-4 w-4 flex-shrink-0 text-brand-400" aria-hidden="true" />
                <p className="text-xs leading-relaxed text-brand-200">
                  Your auction will be created in PENDING status and will
                  automatically start when the start time is reached.
                </p>
              </div>
            </form>

            <div className="flex gap-3 border-t border-slate-800 px-6 py-4">
              <Button variant="secondary" fullWidth onClick={onClose}>
                Cancel
              </Button>
              <Button
                fullWidth
                onClick={handleSubmit}
                loading={isPending}
                disabled={isPending}
              >
                {isPending ? 'Creating...' : 'Create Auction'}
              </Button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
