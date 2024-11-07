import React from 'react';
import { Formik, Form, Field } from 'formik';
import api from '../../api';

const CreatePasteBin: React.FC = () => {
    return (
        <div>
            <h2>Create PasteBin</h2>
            <Formik
                initialValues={{ name: '', content: '' }}
                onSubmit={async (values, { setSubmitting, resetForm }) => {
                    try {
                        await api.post('/pastebin/create', values);
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
                            <label>Name</label>
                            <Field type="text" name="name" />
                        </div>
                        <div>
                            <label>Content</label>
                            <Field as="textarea" name="content" />
                        </div>
                        <button type="submit" disabled={isSubmitting}>
                            Create
                        </button>
                    </Form>
                )}
            </Formik>
        </div>
    );
};

export default CreatePasteBin;
