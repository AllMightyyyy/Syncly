import React from 'react';
import { Formik, Form, Field } from 'formik';
import api from '../../api';

const AddDevice: React.FC = () => {
    return (
        <div>
            <h2>Add Device</h2>
            <Formik
                initialValues={{ deviceName: '', deviceType: 'OTHER' }}
                onSubmit={async (values, { setSubmitting, resetForm }) => {
                    try {
                        await api.post('/device/add', values);
                        resetForm();
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
                            <label>Device Name</label>
                            <Field type="text" name="deviceName" />
                        </div>
                        <div>
                            <label>Device Type</label>
                            <Field as="select" name="deviceType">
                                <option value="DESKTOP">Desktop</option>
                                <option value="MOBILE">Mobile</option>
                                <option value="WEB">Web</option>
                                <option value="OTHER">Other</option>
                            </Field>
                        </div>
                        <button type="submit" disabled={isSubmitting}>
                            Add Device
                        </button>
                    </Form>
                )}
            </Formik>
        </div>
    );
};

export default AddDevice;
