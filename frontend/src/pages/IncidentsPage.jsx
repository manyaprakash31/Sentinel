import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { SeverityBadge, StatusBadge } from '../components/Badges';
import { AlertTriangle, Plus, Send, RefreshCw, X, MessageSquare, Clock } from 'lucide-react';

export const IncidentsPage = () => {
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [newNote, setNewNote] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    title: '',
    description: '',
    severity: 'HIGH',
  });
  const { isAnalyst } = useAuth();

  const fetchIncidents = async () => {
    setLoading(true);
    try {
      const res = await api.get('/incidents');
      if (res.success) {
        setIncidents(res.data.content);
        if (selectedIncident) {
          const updated = res.data.content.find(i => i.id === selectedIncident.id);
          if (updated) setSelectedIncident(updated);
        }
      }
    } catch (err) {
      console.error('Error fetching incidents:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIncidents();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/incidents', createForm);
      setShowCreateModal(false);
      setCreateForm({ title: '', description: '', severity: 'HIGH' });
      fetchIncidents();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleAddNote = async (e) => {
    e.preventDefault();
    if (!newNote.trim() || !selectedIncident) return;
    try {
      await api.post(`/incidents/${selectedIncident.id}/notes`, { content: newNote });
      setNewNote('');
      fetchIncidents();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleResolve = async (id) => {
    try {
      await api.post(`/incidents/${id}/resolve`);
      fetchIncidents();
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
            <AlertTriangle className="w-6 h-6 text-amber-400" />
            Incident Response Management
          </h1>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            Investigation Case Tracking, Note Timeline & Remediation
          </p>
        </div>

        {isAnalyst() && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs tracking-wide transition shadow-[0_0_15px_rgba(0,240,255,0.2)]"
          >
            <Plus className="w-4 h-4" />
            <span>CREATE INCIDENT</span>
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Incident List */}
        <div className="lg:col-span-1 glass-panel rounded-xl border border-slate-800 p-4 space-y-3">
          <div className="text-xs font-mono text-slate-400 uppercase tracking-wider pb-2 border-b border-slate-800">
            Active Cases ({incidents.length})
          </div>

          <div className="space-y-2 max-h-[600px] overflow-y-auto pr-1">
            {loading ? (
              <div className="py-8 text-center">
                <RefreshCw className="w-5 h-5 mx-auto animate-spin text-cyan-400" />
              </div>
            ) : incidents.length === 0 ? (
              <div className="text-xs font-mono text-slate-500 text-center py-6">
                No incidents on record.
              </div>
            ) : (
              incidents.map((inc) => (
                <div
                  key={inc.id}
                  onClick={() => setSelectedIncident(inc)}
                  className={`p-3.5 rounded-lg border cursor-pointer transition ${
                    selectedIncident?.id === inc.id
                      ? 'bg-slate-800 border-cyan-500/50 shadow-[0_0_12px_rgba(0,240,255,0.1)]'
                      : 'bg-slate-900/60 border-slate-800 hover:border-slate-700'
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <SeverityBadge severity={inc.severity} />
                    <StatusBadge status={inc.status} />
                  </div>
                  <h4 className="text-xs font-bold text-slate-200 line-clamp-1">{inc.title}</h4>
                  <div className="text-[10px] font-mono text-slate-500 mt-1 flex items-center justify-between">
                    <span>By: {inc.createdByUsername}</span>
                    <span>{new Date(inc.detectedAt).toLocaleTimeString()}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Incident Detail & Investigation Timeline */}
        <div className="lg:col-span-2 glass-panel rounded-xl border border-slate-800 p-6 flex flex-col justify-between">
          {selectedIncident ? (
            <div className="space-y-6">
              {/* Header details */}
              <div className="border-b border-slate-800 pb-4">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <SeverityBadge severity={selectedIncident.severity} />
                    <StatusBadge status={selectedIncident.status} />
                  </div>
                  {isAnalyst() && selectedIncident.status !== 'RESOLVED' && (
                    <button
                      onClick={() => handleResolve(selectedIncident.id)}
                      className="px-3 py-1.5 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-mono transition"
                    >
                      MARK RESOLVED
                    </button>
                  )}
                </div>
                <h2 className="text-lg font-bold text-white font-sans">{selectedIncident.title}</h2>
                <p className="text-xs text-slate-400 mt-1 font-mono">{selectedIncident.description}</p>
              </div>

              {/* Investigation Timeline */}
              <div>
                <h3 className="text-xs font-mono text-slate-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-cyan-400" />
                  Investigation Timeline & Notes
                </h3>

                <div className="space-y-3 max-h-[300px] overflow-y-auto pr-2">
                  {selectedIncident.notes && selectedIncident.notes.length > 0 ? (
                    selectedIncident.notes.map((note) => (
                      <div key={note.id} className="p-3 rounded-lg bg-slate-900/80 border border-slate-800/80 space-y-1">
                        <div className="flex items-center justify-between text-[11px] font-mono">
                          <span className="text-cyan-400 font-semibold">{note.author}</span>
                          <span className="text-slate-500">{new Date(note.createdAt).toLocaleTimeString()}</span>
                        </div>
                        <div className="text-xs text-slate-300 font-sans">{note.content}</div>
                      </div>
                    ))
                  ) : (
                    <div className="text-xs font-mono text-slate-500 py-3">No analyst notes entered yet.</div>
                  )}
                </div>
              </div>

              {/* Add Note Input */}
              {isAnalyst() && selectedIncident.status !== 'RESOLVED' && (
                <form onSubmit={handleAddNote} className="flex gap-2 pt-2 border-t border-slate-800">
                  <input
                    type="text"
                    required
                    value={newNote}
                    onChange={(e) => setNewNote(e.target.value)}
                    placeholder="Add an analyst investigation note..."
                    className="flex-1 bg-slate-900 border border-slate-700 rounded-lg px-3.5 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
                  />
                  <button
                    type="submit"
                    className="px-4 py-2 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs flex items-center gap-1 transition"
                  >
                    <Send className="w-3.5 h-3.5" />
                    <span>POST</span>
                  </button>
                </form>
              )}
            </div>
          ) : (
            <div className="h-64 flex flex-col items-center justify-center text-slate-500 text-xs font-mono">
              <AlertTriangle className="w-8 h-8 text-slate-600 mb-2" />
              Select an incident from the queue to view timeline and details.
            </div>
          )}
        </div>
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-lg rounded-2xl border border-slate-700 p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="font-bold text-white text-base">Declare Security Incident</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Title</label>
                <input
                  type="text"
                  required
                  value={createForm.title}
                  onChange={(e) => setCreateForm({ ...createForm, title: e.target.value })}
                  placeholder="e.g. Distributed Credential Brute Force"
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Description</label>
                <textarea
                  rows="3"
                  value={createForm.description}
                  onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
                  placeholder="Case notes, impacted assets, and perimeter telemetry..."
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                ></textarea>
              </div>

              <div>
                <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Severity</label>
                <select
                  value={createForm.severity}
                  onChange={(e) => setCreateForm({ ...createForm, severity: e.target.value })}
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                >
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                  <option value="CRITICAL">CRITICAL</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 rounded-lg bg-slate-800 text-xs text-slate-300 font-mono"
                >
                  CANCEL
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs font-mono transition"
                >
                  DECLARE INCIDENT
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
