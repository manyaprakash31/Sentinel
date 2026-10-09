import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { SeverityBadge } from '../components/Badges';
import { Radio, Search, Filter, RefreshCw, Eye, X } from 'lucide-react';

export const EventsPage = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [search, setSearch] = useState('');
  const [selectedEvent, setSelectedEvent] = useState(null);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const res = await api.get('/events', {
        params: {
          page,
          size: 15,
          sourceIp: search || undefined,
        },
      });
      if (res.success) {
        setEvents(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error('Error fetching events:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, [page]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchEvents();
  };

  return (
    <div className="space-y-6">
      {/* Title & Filter bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Radio className="w-6 h-6 text-cyan-400" />
            Security Telemetry Events
          </h1>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            Defensive Application Event Logs & Ingested Telemetry
          </p>
        </div>

        <form onSubmit={handleSearchSubmit} className="flex items-center gap-2">
          <div className="relative">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by IP..."
              className="bg-slate-900/80 border border-slate-700/80 rounded-lg pl-8 pr-3 py-1.5 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500 font-mono"
            />
          </div>
          <button
            type="submit"
            className="px-3 py-1.5 rounded-lg bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 font-mono text-xs hover:bg-cyan-500/20 transition"
          >
            FILTER
          </button>
          <button
            type="button"
            onClick={fetchEvents}
            className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:text-white"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </form>
      </div>

      {/* Events Table */}
      <div className="glass-panel rounded-xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#090d14] border-b border-slate-800 text-slate-400 font-mono uppercase tracking-wider text-[11px]">
              <tr>
                <th className="py-3 px-4">Timestamp</th>
                <th className="py-3 px-4">Event Type</th>
                <th className="py-3 px-4">Source</th>
                <th className="py-3 px-4">Source IP</th>
                <th className="py-3 px-4">User</th>
                <th className="py-3 px-4">Severity</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {loading ? (
                <tr>
                  <td colSpan="7" className="text-center py-8 text-slate-500">
                    <RefreshCw className="w-5 h-5 mx-auto animate-spin text-cyan-400" />
                  </td>
                </tr>
              ) : events.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-8 text-slate-500">
                    No security events found.
                  </td>
                </tr>
              ) : (
                events.map((e) => (
                  <tr key={e.id} className="hover:bg-slate-800/40 transition">
                    <td className="py-3 px-4 text-slate-400">
                      {new Date(e.timestamp).toLocaleTimeString()}
                    </td>
                    <td className="py-3 px-4 font-bold text-slate-200">
                      {e.eventType}
                    </td>
                    <td className="py-3 px-4 text-slate-300">{e.source}</td>
                    <td className="py-3 px-4 text-cyan-400 font-semibold">{e.sourceIp}</td>
                    <td className="py-3 px-4 text-slate-400">{e.username || '—'}</td>
                    <td className="py-3 px-4">
                      <SeverityBadge severity={e.severity} />
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => setSelectedEvent(e)}
                        className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-sans inline-flex items-center gap-1 border border-slate-700 transition"
                      >
                        <Eye className="w-3 h-3" />
                        Details
                      </button>
                    </td>
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

      {/* Event Details Drawer/Modal */}
      {selectedEvent && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-xl rounded-2xl border border-slate-700 p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div className="flex items-center gap-2">
                <SeverityBadge severity={selectedEvent.severity} />
                <h3 className="font-bold text-white text-base font-sans">
                  Event #{selectedEvent.id}: {selectedEvent.eventType}
                </h3>
              </div>
              <button
                onClick={() => setSelectedEvent(null)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs font-mono">
              <div className="p-2.5 rounded bg-slate-900/80 border border-slate-800">
                <div className="text-slate-500">SOURCE IP</div>
                <div className="text-cyan-400 font-bold mt-0.5">{selectedEvent.sourceIp}</div>
              </div>
              <div className="p-2.5 rounded bg-slate-900/80 border border-slate-800">
                <div className="text-slate-500">ORIGINATING COMPONENT</div>
                <div className="text-slate-200 mt-0.5">{selectedEvent.source}</div>
              </div>
              <div className="p-2.5 rounded bg-slate-900/80 border border-slate-800">
                <div className="text-slate-500">AUTHENTICATED USER</div>
                <div className="text-slate-200 mt-0.5">{selectedEvent.username || 'Unspecified'}</div>
              </div>
              <div className="p-2.5 rounded bg-slate-900/80 border border-slate-800">
                <div className="text-slate-500">TIMESTAMP</div>
                <div className="text-slate-200 mt-0.5">{new Date(selectedEvent.timestamp).toLocaleString()}</div>
              </div>
            </div>

            <div>
              <div className="text-xs font-mono text-slate-400 mb-1">MESSAGE PAYLOAD:</div>
              <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 text-xs font-mono text-slate-300">
                {selectedEvent.message || 'No explicit log message provided'}
              </div>
            </div>

            {selectedEvent.metadata && (
              <div>
                <div className="text-xs font-mono text-slate-400 mb-1">RAW METADATA:</div>
                <pre className="p-3 rounded-lg bg-slate-950 border border-slate-800 text-[11px] font-mono text-cyan-300/80 overflow-x-auto">
                  {selectedEvent.metadata}
                </pre>
              </div>
            )}

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setSelectedEvent(null)}
                className="px-4 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs text-white font-mono"
              >
                CLOSE
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
