import { FormComposer, Loader, Modal } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from 'react';
import { useForm } from "react-hook-form";

//import configEstimateModal from './config/configEstimateModal';
//import configEstimateModal from './config/configEstimateModal';
//import { newConfig } from "../components/config/config";
const Heading = (props) => {
    return <h1 className="heading-m">{props.label}</h1>;
};

const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
        <path d="M0 0h24v24H0V0z" fill="none" />
        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
);

const CloseBtn = (props) => {
    return (
        <div className="icon-bg-secondary" onClick={props.onClick}>
            <Close />
        </div>
    );
};

// Import your configuration function

const WorkflowPopup = ({ action, tenantId, t, closeModal, submitAction, businessService, moduleCode, applicationDetails, modalFormConfig }) => {
    const [config, setConfig] = useState(null);
    const [modalSubmit, setModalSubmit] = useState(true);
    const { register, handleSubmit, formState: { errors }, control, setValue } = useForm();
    const [canSubmit, setSubmitValve] = useState(false);
    let myData = {}

    useEffect(() => {
        setConfig({
            form: [
                {
                    label: { heading: `Action: ${action?.action}`, cancel: "Cancel", submit: "Submit" },

                }
            ]
        });
    }, [action, businessService]);

    const _submit = () => {
        submitAction(myData, action);
    };

    if (!config) {
        return <Loader />;
    }

    const onFormValueChange = (setValue = true, formData) => {
        myData = formData;
    };

    const defaultValues = {};
    return (
        <React.Fragment>
            <Modal
                headerBarMain={<h1 className="heading-m">{t(config.form[0].label.heading)}</h1>}
                headerBarEnd={
                    <div className="icon-bg-secondary" onClick={closeModal}>
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
                            <path d="M0 0h24v24H0V0z" fill="none" />
                            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
                        </svg>
                    </div>
                }
                actionCancelLabel={t(config.form[0].label.cancel)}
                actionCancelOnSubmit={closeModal}
                actionSaveLabel={t(config.form[0].label.submit)}
                actionSaveOnSubmit={handleSubmit(_submit)}
                formId="modal-action"
                isDisabled={!modalSubmit}
            >
                <FormComposer
                    formId={"modal-action"}
                    defaultValues={defaultValues}
                    heading={t("")}
                    config={modalFormConfig(config.form[0].label.heading)}
                    onSubmit={handleSubmit(_submit)}
                    onFormValueChange={onFormValueChange}
                    isDisabled={!canSubmit}
                    label={t("HR_COMMON_BUTTON_SUBMIT")}
                />
            </Modal>
        </React.Fragment>
    );
};

export default WorkflowPopup;
