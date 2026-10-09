import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { StatusBadge } from '../components/Badges';
import { Server, Activity, RefreshCw } from 'lucide-react';

export const ServicesPage = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchServices = async () => {
    setLoading(true);
    try {
      const res = await api.get('/services');
      if (res.success) {
        setServices(res.data);
      }
    } catch (err) {
      console.error('Error fetching services:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchServices();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Server className="w-6 h-6 text-cyan-400" />
            Monitored Services & Infrastructure
          </h1>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            Synthetic Heartbeat Telemetry & Perimeter Availability Checks
          </p>
        </div>

        <button
          onClick={fetchServices}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 font-mono border border-slate-700 transition"
        >
          <RefreshCw className="w-3.5 h-3.5 text-cyan-400" />
          <span>PING ALL</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {loading ? (
          <div className="col-span-2 py-12 text-center">
            <RefreshCw className="w-6 h-6 mx-auto animate-spin text-cyan-400" />
          </div>
        ) : (
          services.map((svc) => (
            <div
              key={svc.id}
              className="glass-panel rounded-xl p-5 border border-slate-800 glass-card-hover"
            >
              <div className="flex items-center justify-between mb-3">
                <span className="text-base font-bold text-slate-100">{svc.name}</span>
                <StatusBadge status={svc.status} />
              </div>

              <p className="text-xs text-slate-400 mb-4">{svc.description}</p>

              <div className="bg-slate-900/80 rounded-lg p-3 border border-slate-800 space-y-2 text-xs font-mono">
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">ENDPOINT</span>
                  <span className="text-slate-300 truncate max-w-[240px]">{svc.endpoint}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">RESPONSE LATENCY</span>
                  <span className="text-cyan-400 font-bold">{svc.responseTime} ms</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">LAST CHECKED</span>
                  <span className="text-slate-400">{new Date(svc.lastChecked).toLocaleTimeString()}</span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
