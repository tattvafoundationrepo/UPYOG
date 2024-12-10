import React, { useEffect, useCallback, useState, useRef } from "react";
import { Controller, useForm } from "react-hook-form";
import isEqual from "lodash.isequal";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import RadioButton from "../components/radiobutton";
import dropdownOptions from "../pagecomponents/dropdownOptions.json";

const UserOthersDetails = ({ onUpdate, initialRows = {}, AllowEdit = true, tenantId }) => {
  // const { config } = props;
  const { t } = useTranslation();
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const headerLocale = Digit.Utils.locale.getTransformedLocale(tenantId);

  const processData = useCallback((data) => {
    let value = null;
    if (AllowEdit) {
      value = { value: data, label: data };
    } else {
      value = data;
    }
    return value;
  }, []);

  const {
    control,
    setValue,
    clearErrors,
    trigger,
    watch,
    getValues,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      income: processData(initialRows?.income) || "",
      occupation: processData(initialRows?.occupation || "None") || "",
    },
    mode: "onChange",
  });

  const formValuesRef = useRef(getValues());
  const formValues = watch();

  const stableOnUpdate = useCallback(
    (values, valid) => {
      onUpdate(values, valid);
    },
    [onUpdate]
  );

  useEffect(() => {
    if (!isEqual(formValuesRef.current, formValues)) {
      formValuesRef.current = formValues;
      stableOnUpdate(formValues, isValid);
    }
  }, [formValues, isValid, stableOnUpdate]);

  useEffect(() => {
    trigger();
  }, [trigger]);

  useEffect(() => {
    setValue("income", processData(initialRows?.income) || "");
    setValue("occupation", processData(initialRows?.occupation || "None"));

    if (processData(initialRows.income)) clearErrors("income");
    if (processData(initialRows.occupation)) clearErrors("occupation");
  }, [initialRows, setValue, clearErrors, headerLocale, processData]);

  return (
    <React.Fragment>
      <div className="bmc-row-card-header">
        <div className="bmc-card-row">
          <div className="bmc-title">{t("BMC_OTHER_DETAILS")}</div>
          <div className="bmc-card-row">
            <div className="bmc-col1-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("BMC_INCOME")}</CardLabel>
                <Controller
                  control={control}
                  name="income"
                  rules={{
                    required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                  }}
                  render={(props) => (
                    <div>
                      {isEditable ? (
                        <Dropdown
                          placeholder={t("Select the Income")}
                          selected={props.value}
                          select={(value) => props.onChange(value)}
                          onBlur={props.onBlur}
                          option={dropdownOptions.Income}
                          optionKey="label"
                          t={t}
                          isMandatory={true}
                        />
                      ) : (
                        <TextInput disabled={!isEditable} readOnly={!isEditable} value={props?.value || ""} />
                      )}
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card"></div>
            <div className="bmc-col1-card">
              <LabelFieldPair t={t} isMultipleAllow={true}>
                <CardLabel className="bmc-label">{t("BMC_EMPLOYEEMENT_DETAILS")}</CardLabel>
                <Controller
                  control={control}
                  name="occupation"
                  render={(props) => (
                    <div>
                      {isEditable ? (
                        <RadioButton
                          t={t}
                          optionsKey="value"
                          disabled={!isEditable}
                          options={[
                            { label: t("Service"), value: t("Service") },
                            { label: t("Business"), value: t("Business") },
                            { label: t("None"), value: t("None") },
                          ]}
                          selectedOption={props.value}
                          onSelect={(value) => props.onChange(value)}
                        />
                      ) : (
                        <TextInput disabled={!isEditable} readOnly={!isEditable} value={t(props?.value)} t={t} />
                      )}
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default UserOthersDetails;
