import React from 'react';

export const SeverityBadge = ({ severity }) => {
  const styles = {
    CRITICAL: 'bg-red-500/10 text-red-400 border-red-500/30 animate-pulse',
    HIGH: 'bg-orange-500/10 text-orange-400 border-orange-500/30',
    MEDIUM: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    LOW: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
  };

  const currentStyle = styles[severity] || 'bg-slate-700/20 text-slate-300 border-slate-700/40';

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold font-mono tracking-wider border ${currentStyle}`}>
      {severity}
    </span>
  );
};

export const StatusBadge = ({ status }) => {
  const styles = {
    NEW: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30',
    ACKNOWLEDGED: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30',
    INVESTIGATING: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    OPEN: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
    CONTAINED: 'bg-purple-500/10 text-purple-400 border-purple-500/30',
    RESOLVED: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    CLOSED: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
    DISMISSED: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
    UP: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    DEGRADED: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    DOWN: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
  };

  const currentStyle = styles[status] || 'bg-slate-800 text-slate-400 border-slate-700';

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium font-mono border ${currentStyle}`}>
      {status}
    </span>
  );
};
