import { CardLabel, DatePicker, TextInput,Dropdown } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { convertEpochToDate } from "../../utils/index";

const ReportSubmission = ({ config, onSelect, formData = {}, userType, register, errors }) => {
  const { t } = useTranslation();
  // States for dropdown selections
  const [selectedOrderType, setSelectedOrderType] = useState(formData?.[config.key]?.ordertype || null);
  const [selectedCaseType, setSelectedCaseType] = useState(formData?.[config.key]?.casetype || null);
  
  // Reason options for dropdowns
  const CaseType = [
    { value: "ACB" },
    { value: "Administration" },
    { value: "Criminal" },
  ];

  const EnquiryType = [
    { value: "FFDE" },
    { value: "SDE" },
    { value: "Other Orders" },
  ];


  // Function to update form data
  function setValue(value, input) {
    onSelect(config.key, { ...formData[config.key], [input]: value });
  }

  // Handle dropdown selection changes
  useEffect(() => {
    setValue(selectedOrderType, "ordertype");
  }, [selectedOrderType]);

  useEffect(() => {
    setValue(selectedCaseType, "casetype");
  }, [selectedCaseType]);

  return (
    <React.Fragment>
      <div>
        <div>
          <CardLabel className="bmc-label">{t("EBE_SUBMISSION_COMMENT")}&nbsp;{errors.submissionComment && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.submissionComment.message}</sup>}</CardLabel>
          <div>
            <TextInput name={"submissionComment"}
              value={formData && formData[config.key] ? formData[config.key]["submissionComment"] : undefined}
              onChange={(e) => setValue(e.target.value, "submissionComment")}
            />
          </div>
        </div>
      </div>
      <div>
        <div>
          <CardLabel className="bmc-label">{t("EBE_SUBMISSION_DATE")}&nbsp;{errors.submissiondate && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.submissiondate.message}</sup>}</CardLabel>
          <DatePicker
            key={"submissiondate"}
            min={new Date(2000, 0, 1)}
            max={convertEpochToDate(new Date())}
            date={formData && formData[config.key] ? formData[config.key]["submissiondate"] : undefined}
            onChange={(e) => setValue(e, "submissiondate")}
            disable={false}
            defaultValue={undefined}
          />
        </div>
      </div>
      <div>
        <div>
          <CardLabel className="bmc-label">{t("EBE_ORDER_TYPE")}</CardLabel>
          <div>
            <Dropdown
              key={"ordertype"}
              selected={selectedOrderType}
              option={EnquiryType}
              select={setSelectedOrderType}
              optionKey="value"
              defaultValue={undefined}
              t={t}
            />
          </div>
        </div>
      </div>
      <div>
        <div>
          <CardLabel className="bmc-label">{t("EBE_CASE_TYPE")}</CardLabel>
          <div>
            <Dropdown
              key={"casetype"}
              selected={selectedCaseType}
              option={CaseType}
              select={setSelectedCaseType}
              optionKey="value"
              defaultValue={undefined}
              t={t}
            />
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default ReportSubmission;
