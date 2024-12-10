import { Loader, Modal, LabelFieldPair, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

const Heading = ({ label }) => <h1 className="heading-m">{label}</h1>;

const Close = () => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
    <path d="M0 0h24v24H0V0z" fill="none" />
    <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
  </svg>
);

const CloseBtn = ({ onClick }) => (
  <div className="icon-bg-secondary" onClick={onClick}>
    <Close />
  </div>
);

const WorkflowPopup = ({ action, tenantId, closeModal, submitAction, businessService, moduleCode, applicationDetails }) => {
  const { t } = useTranslation();

  const reasonOptions = [
    { label: t("Document not Availabel"), value: t("Document not Availabel") },
    { label: t("Incorrect Information"), value: t("Incorrect Information") },
    { label: t("Others"), value: t("Others") },
  ];

  const [config, setConfig] = useState(null);
  const [modalSubmit, setModalSubmit] = useState(true);
  const [selectedReason, setSelectedReason] = useState("");

  const { handleSubmit, control, setValue } = useForm();

  useEffect(() => {
    setConfig({
      form: [
        {
          label: { heading: `Action: ${action?.action}`, cancel: "Cancel", submit: "Submit" },
        },
      ],
    });
  }, [action, businessService]);

  const _submit = (data) => {
    submitAction(data, action);
  };

  if (!config) {
    return <Loader />;
  }

  const handleChange = (Other) => {
    setValue("Others", Other);
    setSelectedReason(Other?.value === "Others");
  };

  return (
    <Modal
      headerBarMain={<Heading label={t(config.form[0].label.heading)} />}
      headerBarEnd={<CloseBtn onClick={closeModal} />}
      actionCancelLabel={t(config.form[0].label.cancel)}
      actionCancelOnSubmit={closeModal}
      actionSaveLabel={t(config.form[0].label.submit)}
      actionSaveOnSubmit={handleSubmit(_submit)}
      formId="modal-action"
      isDisabled={!modalSubmit}
    >
      <form id="modal-action" onSubmit={handleSubmit(_submit)}>
        <LabelFieldPair>
          <CardLabel className="bmc-label">{t("BMC_VERIFICATION_REMARK")}</CardLabel>
          <Controller
            control={control}
            name="VerificationRemarks"
            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
            render={(props) => (
              <Dropdown
                placeholder={t("SELECT VERIFICATION MARKS")}
                selected={props.value}
                select={(value) => {
                  props.onChange(value);
                  handleChange(value);
                }}
                option={reasonOptions}
                optionKey="value"
                t={t}
                isMandatory={true}
                className="employee-select-wrap bmc-form-field"
              />
            )}
          />
        </LabelFieldPair>
        {selectedReason && (
          <LabelFieldPair>
            <CardLabel className="bmc-label">{t("BMC_COMMENT")}</CardLabel>
            <Controller
              control={control}
              name="comments"
              rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
              render={({ value, onChange, onBlur }) => (
                <TextInput value={value} onChange={(e) => onChange(e.target.value)} onBlur={onBlur} optionKey="i18nKey" t={t} />
              )}
            />
          </LabelFieldPair>
        )}
      </form>
    </Modal>
  );
};

export default WorkflowPopup;
