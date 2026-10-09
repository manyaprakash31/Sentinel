import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { SeverityBadge, StatusBadge } from '../components/Badges';
import { Bell, Search, RefreshCw, CheckCircle, UserCheck, ShieldAlert } from 'lucide-react';

export const AlertsPage = () => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState('');
  const { isAnalyst } = useAuth();

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const res = await api.get('/alerts', {
        params: {
          page,
          size: 15,
          status: statusFilter || undefined,
        },
      });
      if (res.success) {
        setAlerts(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error('Error fetching alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, [page, statusFilter]);

  const handleAcknowledge = async (id) => {
    try {
      await api.post(`/alerts/${id}/acknowledge`);
      fetchAlerts();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleResolve = async (id) => {
    try {
      await api.post(`/alerts/${id}/resolve`);
      fetchAlerts();
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Bell className="w-6 h-6 text-cyan-400" />
            Security Alerts
          </h1>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            Detection Rule Engine Triggers & Analyst Triage Queue
          </p>
        </div>

        <div className="flex items-center gap-2">
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            className="bg-slate-900 border border-slate-700 rounded-lg px-3 py-1.5 text-xs text-slate-200 font-mono focus:outline-none focus:border-cyan-500"
          >
            <option value="">ALL STATUSES</option>
            <option value="NEW">NEW</option>
            <option value="ACKNOWLEDGED">ACKNOWLEDGED</option>
            <option value="INVESTIGATING">INVESTIGATING</option>
            <option value="RESOLVED">RESOLVED</option>
          </select>
          <button
            onClick={fetchAlerts}
            className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:text-white"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Alerts Table */}
      <div className="glass-panel rounded-xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#090d14] border-b border-slate-800 text-slate-400 font-mono uppercase tracking-wider text-[11px]">
              <tr>
                <th className="py-3 px-4">Severity</th>
                <th className="py-3 px-4">Title</th>
                <th className="py-3 px-4">Source / IP</th>
                <th className="py-3 px-4">Triggered Rule</th>
                <th className="py-3 px-4">Detected At</th>
                <th className="py-3 px-4">Status</th>
                {isAnalyst() && <th className="py-3 px-4 text-right">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {loading ? (
                <tr>
                  <td colSpan="7" className="text-center py-8 text-slate-500">
                    <RefreshCw className="w-5 h-5 mx-auto animate-spin text-cyan-400" />
                  </td>
                </tr>
              ) : alerts.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-8 text-slate-500">
                    No alerts found for this filter.
                  </td>
                </tr>
              ) : (
                alerts.map((a) => (
                  <tr key={a.id} className="hover:bg-slate-800/40 transition">
                    <td className="py-3 px-4">
                      <SeverityBadge severity={a.severity} />
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-bold text-slate-200 font-sans">{a.title}</div>
                      <div className="text-[11px] text-slate-500 line-clamp-1">{a.description}</div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="text-cyan-400">{a.sourceIp || '—'}</div>
                      <div className="text-slate-500 text-[10px]">{a.source}</div>
                    </td>
                    <td className="py-3 px-4 text-slate-300">
                      {a.ruleName || 'Standard Rule'}
                    </td>
                    <td className="py-3 px-4 text-slate-400">
                      {new Date(a.detectedAt).toLocaleTimeString()}
                    </td>
                    <td className="py-3 px-4">
                      <StatusBadge status={a.status} />
                    </td>
                    {isAnalyst() && (
                      <td className="py-3 px-4 text-right">
                        <div className="inline-flex gap-1.5 font-sans">
                          {a.status === 'NEW' && (
                            <button
                              onClick={() => handleAcknowledge(a.id)}
                              className="px-2.5 py-1 rounded bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 text-[11px] transition"
                            >
                              ACK
                            </button>
                          )}
                          {a.status !== 'RESOLVED' && (
                            <button
                              onClick={() => handleResolve(a.id)}
                              className="px-2.5 py-1 rounded bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-[11px] transition"
                            >
                              Resolve
                            </button>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="p-3 bg-[#090d14] border-t border-slate-800 flex items-center justify-between text-xs font-mono text-slate-400">
          <span>Page {page + 1} of {Math.max(1, totalPages)}</span>
          <div className="flex gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
              className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 transition"
            >
              PREVIOUS
            </button>
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => p + 1)}
              className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 transition"
            >
              NEXT
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
