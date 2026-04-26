import { useState, useEffect } from "react";
import { Spinner } from "react-bootstrap";

interface ChartProps {
    type: "elo" | "results" | "users" | "progress";
    params?: Record<string, any>;
    body?: any;
    alt?: string;
    className?: string;
}

export default function Chart({ type, params, body, alt = "Chart", className }: ChartProps) {
    const [imageData, setImageData] = useState<string | null>(null);
    const [error, setError] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchChart = async () => {
            setLoading(true);
            setError(false);
            try {
                let url = `/api/v1/charts/${type}`;
                if (params) {
                    const searchParams = new URLSearchParams();
                    Object.entries(params).forEach(([key, val]) => {
                        searchParams.set(key, String(val));
                    });
                    url += `?${searchParams.toString()}`;
                }

                const options: RequestInit = {
                    method: "GET",
                    credentials: "include",
                };

                if (body) {
                    options.body = JSON.stringify(body);
                    options.headers = { "Content-Type": "application/json" };
                }


                const res = await fetch(url, options);
                if (!res.ok) throw new Error();
                const text = await res.text();
                setImageData(`data:image/png;base64,${text}`);
            } catch (err) {
                setError(true);
            } finally {
                setLoading(false);
            }
        };

        fetchChart();
    }, [type, JSON.stringify(params), JSON.stringify(body)]);

    if (loading) return <div className="text-center py-4"><Spinner animation="border" size="sm" /></div>;
    if (error) return <div className="text-center py-4 text-secondary small">Failed to load chart</div>;

    return <img src={imageData!} alt={alt} className={`img-fluid ${className || ""}`} />;
}