// ============================================================================
// Formatting Utilities
// ============================================================================

export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
};

/**
 * Format date/time
 */
export const formatDateTime = (dateString: string): string => {
  return new Date(dateString).toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

/**
 * Calculate time remaining until a future date
 */
export const getTimeRemaining = (endDate: Date): string => {
  const now = new Date();
  const difference = endDate.getTime() - now.getTime();

  if (difference <= 0) {
    return "Ended";
  }

  const days = Math.floor(difference / (1000 * 60 * 60 * 24));
  const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((difference / 1000 / 60) % 60);
  const seconds = Math.floor((difference / 1000) % 60);

  if (days > 0) {
    return `${days}d ${hours}h`;
  }
  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }
  if (minutes > 0) {
    return `${minutes}m ${seconds}s`;
  }
  return `${seconds}s`;
};

/**
 * Calculate percentage change
 */
export const calculatePercentageChange = (
  original: number,
  current: number
): number => {
  if (original === 0) return 0;
  return ((current - original) / original) * 100;
};

// ============================================================================
// Validation Utilities
// ============================================================================

/**
 * Validate email format
 */
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Validate password strength
 */
export const validatePasswordStrength = (
  password: string
): {
  isStrong: boolean;
  feedback: string[];
} => {
  const feedback: string[] = [];

  if (password.length < 8) {
    feedback.push("At least 8 characters");
  }
  if (!/[A-Z]/.test(password)) {
    feedback.push("One uppercase letter");
  }
  if (!/[a-z]/.test(password)) {
    feedback.push("One lowercase letter");
  }
  if (!/[0-9]/.test(password)) {
    feedback.push("One number");
  }

  return {
    isStrong: feedback.length === 0,
    feedback,
  };
};

// ============================================================================
// Array & Object Utilities
// ============================================================================

/**
 * Debounce function
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout;

  return function executedFunction(...args: Parameters<T>) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };

    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

/**
 * Throttle function
 */
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean;

  return function (...args: Parameters<T>) {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
};

/**
 * Get initials from name
 */
export const getInitials = (name: string): string => {
  return name
    .split(" ")
    .map((part) => part[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
};

/**
 * Truncate string with ellipsis
 */
export const truncate = (text: string, length: number): string => {
  return text.length > length ? `${text.slice(0, length)}...` : text;
};
