import React, { useEffect, useState, useContext } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { AuthContext } from '../../contexts/AuthContext';

interface ClipboardMessage {
    content: string;
    timestamp: string;
    deviceInfo: string;
    username: string;
}

const ClipboardRealTime: React.FC = () => {
    const [messages, setMessages] = useState<ClipboardMessage[]>([]);
    const { user } = useContext(AuthContext);

    useEffect(() => {
        // Only proceed if user exists
        if (!user) return;

        const client = new Client({
            brokerURL: 'wss://localhost:8081/ws', // Replace with your backend WebSocket URL
            connectHeaders: {
                Authorization: `Bearer ${localStorage.getItem('jwtToken')}`,
            },
            webSocketFactory: () => new SockJS('https://localhost:8081/ws'),
            onConnect: () => {
                client.subscribe(`/topic/clipboard/${user.username}`, (message) => {
                    const body: ClipboardMessage = JSON.parse(message.body);
                    setMessages((prev) => [body, ...prev]);
                });
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame.headers['message']);
            },
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [user]); // Depend only on `user`, since `user?.username` will be checked inside

    return (
        <div>
            <h2>Real-Time Clipboard Updates</h2>
            <ul>
                {messages.map((msg, index) => (
                    <li key={index}>
                        <p>{msg.content}</p>
                        <p>{new Date(msg.timestamp).toLocaleString()}</p>
                        <p>{msg.deviceInfo}</p>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ClipboardRealTime;
