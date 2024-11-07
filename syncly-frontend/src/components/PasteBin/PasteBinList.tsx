import React, { useEffect, useState } from 'react';
import api from '../../api';

interface PasteBin {
    id: number;
    name: string;
    content: string;
    timestamp: string;
}

const PasteBinList: React.FC = () => {
    const [pasteBins, setPasteBins] = useState<PasteBin[]>([]);

    const fetchPasteBins = async () => {
        try {
            const response = await api.get('/pastebin/list', {
                params: { limit: 10, offset: 0 },
            });
            setPasteBins(response.data);
        } catch (error: any) {
            console.error(error);
        }
    };

    const deletePasteBin = async (id: number) => {
        try {
            await api.delete(`/pastebin/delete/${id}`);
            setPasteBins(pasteBins.filter((pasteBin) => pasteBin.id !== id));
        } catch (error: any) {
            console.error(error);
        }
    };

    useEffect(() => {
        fetchPasteBins();
    }, []);

    return (
        <div>
            <h2>Your PasteBins</h2>
            <ul>
                {pasteBins.map((pasteBin) => (
                    <li key={pasteBin.id}>
                        <h3>{pasteBin.name}</h3>
                        <p>{pasteBin.content}</p>
                        <p>{new Date(pasteBin.timestamp).toLocaleString()}</p>
                        <button onClick={() => deletePasteBin(pasteBin.id)}>Delete</button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default PasteBinList;
