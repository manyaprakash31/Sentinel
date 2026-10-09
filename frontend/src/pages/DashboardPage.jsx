import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useWebSocket } from '../hooks/useWebSocket';
import { SeverityBadge, StatusBadge } from '../components/Badges';
import {
  ShieldAlert,
  AlertTriangle,
  Flame,
  Radio,
  Server,
  ArrowUpRight,
  TrendingUp,
  CheckCircle2,
  RefreshCw,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

export const DashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchSummary = async () => {
    try {
      const res = await api.get('/dashboard/summary');
      if (res.success) {
        setData(res.data);
      }
    } catch (err) {
      console.error('Error fetching dashboard summary:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSummary();
  }, []);

  // Listen to WebSocket broadcasts to update dashboard in real-time
  useWebSocket({
    '/topic/events': () => fetchSummary(),
    '/topic/alerts': () => fetchSummary(),
    '/topic/incidents': () => fetchSummary(),
    '/topic/services': () => fetchSummary(),
  });

  if (loading || !data) {
    return (
      <div className="flex h-64 items-center justify-center">
        <RefreshCw className="w-8 h-8 text-cyan-400 animate-spin" />
      </div>
    );
  }

  // Formatting chart data
  const eventTypeChartData = Object.entries(data.eventsByType || {}).map(([key, value]) => ({
    name: key.replace(/_/g, ' '),
    count: value,
  }));

  const severityColors = {
    CRITICAL: '#ff3366',
    HIGH: '#ff9900',
    MEDIUM: '#eab308',
    LOW: '#3b82f6',
  };

  const alertSeverityData = Object.entries(data.alertsBySeverity || {}).map(([key, value]) => ({
    name: key,
    value: value,
    color: severityColors[key] || '#94a3b8',
  }));

  const incidentStatusData = Object.entries(data.incidentsByStatus || {}).map(([key, value]) => ({
    name: key,
    count: value,
  }));

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Security Operations Center
          </h1>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            Enterprise Threat Telemetry & Incident Overview
          </p>
        </div>
        <button
          onClick={fetchSummary}
          className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-xs text-slate-300 font-mono border border-slate-700 transition"
        >
          <RefreshCw className="w-3.5 h-3.5 text-cyan-400" />
          <span>SYNC METRICS</span>
        </button>
      </div>

      {/* KPI Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* Active Alerts */}
        <div className="glass-panel rounded-xl p-5 border border-slate-800/80 glass-card-hover">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-mono uppercase tracking-wider">Active Alerts</span>
            <ShieldAlert className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="text-2xl font-black text-white">{data.activeAlerts}</div>
          <div className="text-[11px] text-cyan-400 font-mono mt-1">Pending investigation</div>
        </div>

        {/* Critical Alerts */}
        <div className="glass-panel rounded-xl p-5 border border-slate-800/80 glass-card-hover">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-mono uppercase tracking-wider">Critical Alerts</span>
            <Flame className="w-4 h-4 text-rose-500 animate-pulse" />
          </div>
          <div className="text-2xl font-black text-rose-400">{data.criticalAlerts}</div>
          <div className="text-[11px] text-rose-400/80 font-mono mt-1">Immediate action req.</div>
        </div>

        {/* Open Incidents */}
        <div className="glass-panel rounded-xl p-5 border border-slate-800/80 glass-card-hover">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-mono uppercase tracking-wider">Open Incidents</span>
            <AlertTriangle className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-black text-amber-300">{data.openIncidents}</div>
          <div className="text-[11px] text-amber-400/80 font-mono mt-1">{data.resolvedIncidents} resolved</div>
        </div>

        {/* Events Today */}
        <div className="glass-panel rounded-xl p-5 border border-slate-800/80 glass-card-hover">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-mono uppercase tracking-wider">Events Today</span>
            <Radio className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-white">{data.eventsToday}</div>
          <div className="text-[11px] text-slate-400 font-mono mt-1">Total: {data.totalEvents}</div>
        </div>

        {/* Services Status */}
        <div className="glass-panel rounded-xl p-5 border border-slate-800/80 glass-card-hover">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-mono uppercase tracking-wider">Services</span>
            <Server className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-2xl font-black text-white">
            {data.servicesTotal - data.servicesDown}/{data.servicesTotal}
          </div>
          <div className={`text-[11px] font-mono mt-1 ${data.servicesDown > 0 ? 'text-rose-400' : 'text-emerald-400'}`}>
            {data.servicesDown > 0 ? `${data.servicesDown} DOWN` : '100% HEALTHY'}
          </div>
        </div>
      </div>

      {/* Main Charts Area */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Events Breakdown Chart */}
        <div className="lg:col-span-2 glass-panel rounded-xl p-6 border border-slate-800/80">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-bold text-white tracking-wide">
              Security Telemetry Distribution
            </h2>
            <span className="text-xs font-mono text-slate-500">Live Ingested Telemetry</span>
          </div>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={eventTypeChartData}>
                <XAxis dataKey="name" stroke="#64748b" fontSize={10} tickLine={false} />
                <YAxis stroke="#64748b" fontSize={10} tickLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0d1117', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                />
                <Bar dataKey="count" fill="#00f0ff" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Alerts by Severity Pie Chart */}
        <div className="glass-panel rounded-xl p-6 border border-slate-800/80">
          <h2 className="text-sm font-bold text-white tracking-wide mb-4">
            Alert Severity Distribution
          </h2>
          <div className="h-48 flex items-center justify-center">
            {alertSeverityData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={alertSeverityData}
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={75}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {alertSeverityData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0d1117', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-xs text-slate-500 font-mono">No active alerts recorded</div>
            )}
          </div>
          <div className="grid grid-cols-2 gap-2 mt-2 pt-2 border-t border-slate-800">
            {alertSeverityData.map((s) => (
              <div key={s.name} className="flex items-center gap-1.5 text-xs font-mono">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: s.color }} />
                <span className="text-slate-400">{s.name}:</span>
                <span className="text-slate-200 font-bold">{s.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Bottom Feed: Recent Alerts & Monitored Services */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Alerts */}
        <div className="glass-panel rounded-xl p-6 border border-slate-800/80">
          <h2 className="text-sm font-bold text-white tracking-wide mb-4">
            Recent Security Alerts
          </h2>
          <div className="space-y-3">
            {data.recentAlerts && data.recentAlerts.length > 0 ? (
              data.recentAlerts.map((alert) => (
                <div
                  key={alert.id}
                  className="p-3.5 rounded-lg bg-slate-900/60 border border-slate-800/80 flex items-center justify-between hover:border-slate-700 transition"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <SeverityBadge severity={alert.severity} />
                      <span className="text-xs font-semibold text-slate-200">{alert.title}</span>
                    </div>
                    <div className="text-[11px] text-slate-400 font-mono">
                      IP: {alert.sourceIp || 'N/A'} &bull; Source: {alert.source}
                    </div>
                  </div>
                  <StatusBadge status={alert.status} />
                </div>
              ))
            ) : (
              <div className="text-xs text-slate-500 font-mono py-4 text-center">No alerts recorded</div>
            )}
          </div>
        </div>

        {/* Monitored Infrastructure Health */}
        <div className="glass-panel rounded-xl p-6 border border-slate-800/80">
          <h2 className="text-sm font-bold text-white tracking-wide mb-4">
            Monitored Infrastructure Services
          </h2>
          <div className="space-y-3">
            {data.services && data.services.length > 0 ? (
              data.services.map((svc) => (
                <div
                  key={svc.id}
                  className="p-3.5 rounded-lg bg-slate-900/60 border border-slate-800/80 flex items-center justify-between"
                >
                  <div>
                    <div className="text-xs font-semibold text-slate-200">{svc.name}</div>
                    <div className="text-[11px] text-slate-400 font-mono">
                      Latency: {svc.responseTime}ms &bull; {svc.endpoint}
                    </div>
                  </div>
                  <StatusBadge status={svc.status} />
                </div>
              ))
            ) : (
              <div className="text-xs text-slate-500 font-mono py-4 text-center">No services registered</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
