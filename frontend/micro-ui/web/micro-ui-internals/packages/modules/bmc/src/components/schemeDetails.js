import React, { useEffect, useCallback, useState, useRef } from "react";
import { Controller, useForm } from "react-hook-form";
import isEqual from "lodash.isequal";
import { useTranslation } from "react-i18next";
import { CheckBox } from "@upyog/digit-ui-react-components";

const SchemeDetailsPage = ({
  onUpdate,
  initialRows = {},
  AllowEdit = true,
  tenantId,
  scheme,
  schemeType,
  selectedScheme,
  checkboxStates,
  onCheckboxChange,
  ...props
}) => {
  const { config } = props;
  const { t } = useTranslation();
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const headerLocale = Digit.Utils.locale.getTransformedLocale(tenantId);
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
      statement: initialRows?.statement || "",
      agreeToPay: initialRows?.agreeToPay || "",
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
    if (initialRows) {
      setValue("statement", initialRows?.statement || "");
      setValue("agreeToPay", initialRows?.agreeToPay || "");

      if (initialRows.statement) clearErrors("statement");
      if (initialRows.agreeToPay) clearErrors("agreeToPay");
    }
  }, [initialRows, setValue, clearErrors, headerLocale]);

  return (
    <React.Fragment>
      <div className="bmc-row-card-header">
        <div className="bmc-card-row">
          <span className="bmc-col-large-header">
            {schemeType && (
              <div className="bmc-title" index={schemeType.key}>
                {schemeType.type === "machine"
                  ? `${t("Selected Machine")}: ${t(schemeType.value)}`
                  : schemeType.type === "course"
                  ? `${t("Selected Course")}: ${t(schemeType.value)}`
                  : schemeType.type === "pension"
                  ? null
                  : null}
              </div>
            )}
          </span>
        </div>
        <div className="bmc-card-row">
          <div className="bmc-title">{t("BMC_TERMS_AND_CONDITIONS")}</div>
          <div className="bmc-col-large-header" style={{ paddingLeft: "5px" }}>
            <div className="bmc-checkbox">
              <div className="bmc-card-row" style={{ paddingBottom: "8px" }}>
                <Controller
                  control={control}
                  name="agreeToPay"
                  render={(props) => (
                    <CheckBox
                      label={t("BMC_AGREE_TO_PAY_CONTRIBUTION")}
                      styles={{ height: "auto", color: "#f47738", fontWeight: "bold", fontSize: "18px", float: "left" }}
                      checked={props.value}
                      onChange={() => {
                        if (isEditable) {
                          props.onChange(!props.value);
                          onCheckboxChange("agreeToPay", !props.value);
                        }
                      }}
                      disabled={!isEditable}
                    />
                  )}
                />
              </div>
              <div className="bmc-card-row">
                <Controller
                  control={control}
                  name="statement"
                  render={(props) => (
                    <CheckBox
                      label={t("BMC_To_the_best_of_my_knowledge_the_information_provided_above_is_correct..")}
                      styles={{ height: "auto", color: "#f47738", fontWeight: "bold", fontSize: "18px", float: "left" }}
                      checked={props.value}
                      onChange={() => {
                        if (isEditable) {
                          props.onChange(!props.value);
                          onCheckboxChange("statement", !props.value);
                        }
                      }}
                      disabled={!isEditable}
                    />
                  )}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default SchemeDetailsPage;
