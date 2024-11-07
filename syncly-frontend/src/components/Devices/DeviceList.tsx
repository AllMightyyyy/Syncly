import React, { useEffect, useState } from 'react';
import api from '../../api';

interface Device {
    id: number;
    deviceName: string;
    deviceType: string;
    createdAt: string;
}

const DeviceList: React.FC = () => {
    const [devices, setDevices] = useState<Device[]>([]);

    const fetchDevices = async () => {
        try {
            const response = await api.get('/device/list');
            setDevices(response.data);
        } catch (error: any) {
            console.error(error);
        }
    };

    const deleteDevice = async (id: number) => {
        try {
            await api.delete(`/device/delete/${id}`);
            setDevices(devices.filter((device) => device.id !== id));
        } catch (error: any) {
            console.error(error);
        }
    };

    useEffect(() => {
        fetchDevices();
    }, []);

    return (
        <div>
            <h2>Your Devices</h2>
            <ul>
                {devices.map((device) => (
                    <li key={device.id}>
                        <p>{device.deviceName}</p>
                        <p>{device.deviceType}</p>
                        <p>{new Date(device.createdAt).toLocaleString()}</p>
                        <button onClick={() => deleteDevice(device.id)}>Remove</button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default DeviceList;
