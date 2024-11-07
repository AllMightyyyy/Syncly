import React, { useEffect, useState } from 'react';
import api from '../../api';
import { Formik, Form, Field } from 'formik';

interface UserProfile {
    username: string;
    email: string;
    displayName: string;
    avatarUrl: string;
}

const UserProfile: React.FC = () => {
    const [profile, setProfile] = useState<UserProfile | null>(null);

    const fetchProfile = async () => {
        try {
            const response = await api.get('/users/me'); // Assuming 'me' returns the current user
            setProfile(response.data);
        } catch (error: any) {
            console.error(error);
        }
    };

    useEffect(() => {
        fetchProfile();
    }, []);

    if (!profile) return <div>Loading...</div>;

    return (
        <div>
            <h2>User Profile</h2>
            <Formik
                initialValues={{
                    displayName: profile.displayName || '',
                    avatarUrl: profile.avatarUrl || '',
                }}
                onSubmit={async (values, { setSubmitting }) => {
                    try {
                        await api.put(`/users/${profile.username}`, values);
                        fetchProfile();
                    } catch (error: any) {
                        console.error(error);
                    } finally {
                        setSubmitting(false);
                    }
                }}
            >
                {({ isSubmitting }) => (
                    <Form>
                        <div>
                            <label>Display Name</label>
                            <Field type="text" name="displayName" />
                        </div>
                        <div>
                            <label>Avatar URL</label>
                            <Field type="text" name="avatarUrl" />
                        </div>
                        <button type="submit" disabled={isSubmitting}>
                            Update Profile
                        </button>
                    </Form>
                )}
            </Formik>
        </div>
    );
};

export default UserProfile;
