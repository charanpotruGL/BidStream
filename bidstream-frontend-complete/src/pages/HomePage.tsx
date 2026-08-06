import React from 'react'
import { motion } from 'framer-motion'
import { Gavel, Bell, Shield, ArrowRight } from 'lucide-react'
import { useAuthStore } from '../store'
import { Button } from '../components/ui/Button'
import { Panel } from '../components/ui/Panel'

const fadeUp = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0 },
}

const features = [
  {
    icon: Gavel,
    title: 'Real-time Bidding',
    desc: 'Bid activity streams in live with instant confirmation and outbid alerts.',
  },
  {
    icon: Bell,
    title: 'Instant Notifications',
    desc: 'Never miss a bid — get notified when auctions start, close, or you’re outbid.',
  },
  {
    icon: Shield,
    title: 'Secure & Scalable',
    desc: 'JWT-authenticated microservices, Kafka event streaming, and PostgreSQL storage.',
  },
]

export const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuthStore()

  return (
    <div className="flex flex-col gap-20 pb-16">
      {/* Hero */}
      <motion.section
        {...fadeUp}
        transition={{ duration: 0.5 }}
        className="mx-auto max-w-3xl pt-16 text-center sm:pt-24"
      >
        <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-brand-500/30 bg-brand-500/10 px-4 py-1.5 text-sm font-medium text-brand-300">
          <span className="h-1.5 w-1.5 rounded-full bg-brand-400 animate-pulse-dot" aria-hidden="true" />
          Live real-time online auctions
        </div>

        <h1 className="font-display text-4xl font-semibold leading-[1.1] tracking-tight text-white sm:text-6xl md:text-7xl">
          Bid. Win.{' '}
          <span className="italic text-gradient">Repeat.</span>
        </h1>

        <p className="mx-auto mt-6 max-w-xl text-lg leading-relaxed text-slate-400">
          BidStream connects sellers and bidders in real-time. Place bids, track
          auctions live, and get notified the moment you&rsquo;re outbid.
        </p>

        <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Button to="/auctions" size="lg">
            Browse Auctions
            <ArrowRight className="h-4 w-4" aria-hidden="true" />
          </Button>
          {isAuthenticated ? (
            <Button to="/seller-hub" variant="outline" size="lg">
              Sell an Item
            </Button>
          ) : (
            <Button to="/login" variant="outline" size="lg">
              Create Account
            </Button>
          )}
        </div>
      </motion.section>

      {/* Feature cards */}
      <motion.section
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.15 }}
        className="grid grid-cols-1 gap-5 md:grid-cols-3"
      >
        {features.map(({ icon: Icon, title, desc }) => (
          <motion.div
            key={title}
            whileHover={{ y: -4 }}
            transition={{ type: 'spring', stiffness: 300, damping: 24 }}
          >
            <Panel hover className="flex h-full flex-col gap-4 p-6">
              <span className="flex h-12 w-12 items-center justify-center rounded-xl border border-brand-500/25 bg-brand-500/10 text-brand-300">
                <Icon className="h-6 w-6" aria-hidden="true" />
              </span>
              <div>
                <h3 className="text-lg font-semibold text-white">{title}</h3>
                <p className="mt-1.5 text-sm leading-relaxed text-slate-400">{desc}</p>
              </div>
            </Panel>
          </motion.div>
        ))}
      </motion.section>

      {/* CTA banner */}
      <motion.section
        initial={{ opacity: 0, y: 16 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        transition={{ duration: 0.5 }}
        className="mx-auto w-full max-w-5xl"
      >
        <Panel className="relative overflow-hidden p-8 text-center sm:p-12">
          <div
            className="pointer-events-none absolute inset-0 bg-brand-gradient-soft"
            aria-hidden="true"
          />
          <h2 className="relative font-display text-2xl font-semibold text-white sm:text-3xl">
            Ready to make your first bid?
          </h2>
          <p className="relative mx-auto mt-2 max-w-lg text-sm text-slate-400 sm:text-base">
            Join BidStream today — create an account, list an item, or place your
            first bid in seconds.
          </p>
          <div className="relative mt-7 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Button to="/auctions" size="lg">
              Start Bidding
              <ArrowRight className="h-4 w-4" aria-hidden="true" />
            </Button>
            {!isAuthenticated && (
              <Button to="/login" variant="secondary" size="lg">
                Create Free Account
              </Button>
            )}
          </div>
        </Panel>
      </motion.section>
    </div>
  )
}
