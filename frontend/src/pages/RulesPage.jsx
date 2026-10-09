import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { SeverityBadge } from '../components/Badges';
import { Sliders, Plus, Trash2, ToggleLeft, ToggleRight, RefreshCw, X } from 'lucide-react';

export const RulesPage = () => {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({
    name: '',
    description: '',
    eventType: 'LOGIN_FAILURE',
    threshold: 5,
    timeWindowMinutes: 5,
    severity: 'HIGH',
    enabled: true,
  });

  const fetchRules = async () => {
    setLoading(true);
    try {
      const res = await api.get('/rules');
      if (res.success) {
        setRules(res.data);
      }
    } catch (err) {
      console.error('Error fetching rules:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleToggle = async (rule) => {
    try {
      await api.put(`/rules/${rule.id}`, { enabled: !rule.enabled });
      fetchRules();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this detection rule?')) return;
    try {
      await api.delete(`/rules/${id}`);
      fetchRules();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/rules', form);
      setShowModal(false);
      setForm({
        name: '',
        description: '',
        eventType: 'LOGIN_FAILURE',
        threshold: 5,
        timeWindowMinutes: 5,
        severity: 'HIGH',
        enabled: true,
      });
      fetchRules();
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Sliders className="w-6 h-6 text-cyan-400" />
            Defensive Detection Rules
          </h1>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            Configurable Thresholds, Time Windows & Alerting Policies
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs tracking-wide transition shadow-[0_0_15px_rgba(0,240,255,0.2)]"
        >
          <Plus className="w-4 h-4" />
          <span>NEW DETECTION RULE</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {loading ? (
          <div className="col-span-2 py-12 text-center">
            <RefreshCw className="w-6 h-6 mx-auto animate-spin text-cyan-400" />
          </div>
        ) : (
          rules.map((rule) => (
            <div
              key={rule.id}
              className="glass-panel rounded-xl p-5 border border-slate-800 glass-card-hover flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <SeverityBadge severity={rule.severity} />
                  <button
                    onClick={() => handleToggle(rule)}
                    className="flex items-center gap-1.5 text-xs font-mono"
                  >
                    {rule.enabled ? (
                      <span className="text-emerald-400 flex items-center gap-1">
                        <ToggleRight className="w-5 h-5" /> ENABLED
                      </span>
                    ) : (
                      <span className="text-slate-500 flex items-center gap-1">
                        <ToggleLeft className="w-5 h-5" /> DISABLED
                      </span>
                    )}
                  </button>
                </div>

                <h3 className="text-base font-bold text-slate-100">{rule.name}</h3>
                <p className="text-xs text-slate-400 mt-1">{rule.description}</p>

                <div className="grid grid-cols-3 gap-2 mt-4 pt-3 border-t border-slate-800/80 text-xs font-mono">
                  <div className="bg-slate-900/60 p-2 rounded">
                    <span className="text-slate-500 block text-[10px]">EVENT</span>
                    <span className="text-slate-200 font-bold">{rule.eventType}</span>
                  </div>
                  <div className="bg-slate-900/60 p-2 rounded">
                    <span className="text-slate-500 block text-[10px]">THRESHOLD</span>
                    <span className="text-cyan-400 font-bold">&gt; {rule.threshold}</span>
                  </div>
                  <div className="bg-slate-900/60 p-2 rounded">
                    <span className="text-slate-500 block text-[10px]">TIME WINDOW</span>
                    <span className="text-slate-200 font-bold">{rule.timeWindowMinutes} min</span>
                  </div>
                </div>
              </div>

              <div className="flex justify-end pt-4 mt-2">
                <button
                  onClick={() => handleDelete(rule.id)}
                  className="p-1.5 text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 rounded transition"
                  title="Delete Rule"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-lg rounded-2xl border border-slate-700 p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="font-bold text-white text-base">Create Defensive Detection Rule</h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Rule Name</label>
                <input
                  type="text"
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="e.g. Excessive Service Failures"
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Description</label>
                <textarea
                  rows="2"
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  placeholder="Defensive rationale and trigger criteria..."
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                ></textarea>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Event Type</label>
                  <select
                    value={form.eventType}
                    onChange={(e) => setForm({ ...form, eventType: e.target.value })}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
                  >
                    <option value="LOGIN_FAILURE">LOGIN_FAILURE</option>
                    <option value="ACCESS_DENIED">ACCESS_DENIED</option>
                    <option value="SERVICE_FAILURE">SERVICE_FAILURE</option>
                    <option value="SUSPICIOUS_ACTIVITY">SUSPICIOUS_ACTIVITY</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Alert Severity</label>
                  <select
                    value={form.severity}
                    onChange={(e) => setForm({ ...form, severity: e.target.value })}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
                  >
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="CRITICAL">CRITICAL</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Threshold (Count)</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={form.threshold}
                    onChange={(e) => setForm({ ...form, threshold: parseInt(e.target.value) })}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-mono text-slate-400 uppercase mb-1">Window (Minutes)</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={form.timeWindowMinutes}
                    onChange={(e) => setForm({ ...form, timeWindowMinutes: parseInt(e.target.value) })}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-lg bg-slate-800 text-xs text-slate-300 font-mono"
                >
                  CANCEL
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs font-mono transition"
                >
                  SAVE RULE
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
