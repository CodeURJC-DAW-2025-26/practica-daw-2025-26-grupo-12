import { useState, useEffect } from "react";
import { Spinner } from "react-bootstrap";
import {
    LineChart, Line,
    PieChart, Pie, Cell,
    BarChart, Bar,
    XAxis, YAxis, Tooltip, ResponsiveContainer
} from "recharts";

interface ChartProps {
    type: "elo" | "results" | "users" | "progress";
    params?: Record<string, any>;
    body?: any;
}

export default function Chart({ type, params, body }: ChartProps) {
    const [data, setData] = useState<any>(null);
    const [error, setError] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(false);

            try {
                let url = `/api/v1/charts/${type}`;

                if (params) {
                    const searchParams = new URLSearchParams();
                    Object.entries(params).forEach(([k, v]) => {
                        searchParams.set(k, String(v));
                    });
                    url += `?${searchParams.toString()}`;
                }

                const res = await fetch(url, {
                    method: body ? "POST" : "GET",
                    credentials: "include",
                    headers: body ? { "Content-Type": "application/json" } : undefined,
                    body: body ? JSON.stringify(body) : undefined
                });

                if (!res.ok) throw new Error();

                const json = await res.json();
                setData(json);
            } catch {
                setError(true);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [type, JSON.stringify(params), JSON.stringify(body)]);

    if (loading) return <div className="text-center py-4"><Spinner size="sm" /></div>;
    if (error) return <div className="text-center py-4 text-secondary">Error loading chart</div>;

    if (type === "results") {
        const pieData = [
            { name: "Wins", value: data.wins },
            { name: "Losses", value: data.losses },
            { name: "Draws", value: data.draws },
        ];

        return (
            <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                    <Pie data={pieData} dataKey="value" nameKey="name">
                        <Cell fill="#22c55e" />
                        <Cell fill="#ef4444" />
                        <Cell fill="#9ca3af" />
                    </Pie>
                    <Tooltip />
                </PieChart>
            </ResponsiveContainer>
        );
    }

    if (type === "elo") {
        const lineData = data.map((value: number, index: number) => ({
            game: index + 1,
            elo: value
        }));

        return (
            <ResponsiveContainer width="100%" height={250}>
                <LineChart data={lineData}>
                    <XAxis dataKey="game" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="elo" stroke="#3b82f6" />
                </LineChart>
            </ResponsiveContainer>
        );
    }

    if (type === "users") {
        const barData = data.map((d: any) => ({
            label: `${d.year}-${d.month}`,
            users: d.count
        }));

        return (
            <ResponsiveContainer width="100%" height={250}>
                <BarChart data={barData}>
                    <XAxis dataKey="label" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="users" fill="#6366f1" />
                </BarChart>
            </ResponsiveContainer>
        );
    }

    if (type === "progress") {
        const percent = (data.current / data.max) * 100;

        return (
            <div className="w-100 bg-dark rounded" style={{ height: 20 }}>
                <div
                    className="bg-success h-100 rounded"
                    style={{ width: `${percent}%` }}
                />
            </div>
        );
    }

    return null;
}