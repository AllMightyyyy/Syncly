import React, { useEffect, useState } from 'react';
import api from '../../api';

interface ClipboardEntry {
    id: number;
    content: string;
    timestamp: string;
    deviceInfo: string;
}

const ClipboardHistory: React.FC = () => {
    const [entries, setEntries] = useState<ClipboardEntry[]>([]);

    const fetchHistory = async () => {
        try {
            const response = await api.get('/clipboard/history', {
                params: { limit: 10, offset: 0 },
            });
            setEntries(response.data);
        } catch (error: any) {
            console.error(error);
        }
    };

    const deleteEntry = async (id: number) => {
        try {
            await api.delete(`/clipboard/delete/${id}`);
            setEntries(entries.filter((entry) => entry.id !== id));
        } catch (error: any) {
            console.error(error);
        }
    };

    useEffect(() => {
        fetchHistory();
    }, []);

    return (
        <div>
            <h2>Clipboard History</h2>
            <ul>
                {entries.map((entry) => (
                    <li key={entry.id}>
                        <p>{entry.content}</p>
                        <p>{new Date(entry.timestamp).toLocaleString()}</p>
                        <p>{entry.deviceInfo}</p>
                        <button onClick={() => deleteEntry(entry.id)}>Delete</button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ClipboardHistory;
