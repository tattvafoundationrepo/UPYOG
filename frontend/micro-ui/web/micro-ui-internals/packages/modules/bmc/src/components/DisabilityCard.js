import { CardLabel, Dropdown, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import isEqual from "lodash.isequal";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import ToggleSwitch from "./Toggle";

const DisabilityCard = ({ tenantId, onUpdate, initialRows = {}, AllowEdit = false, ...props}) => {
  const { t } = useTranslation();
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const [focusIndex, setFocusIndex] = useState({ index: -1, type: "" });
  const [rangeValue, setRangeValue] = useState(0);
  const [divyangs, setDivyangs] = useState([]);
  const headerLocale = useMemo(() => Digit.Utils.locale.getTransformedLocale(tenantId), [tenantId]);

  const initialDefaultValues = {
    divyangcardid: null,
    disabilitytype: null,
    divyangpercent: 0,
  };

  const {
    control,
    watch,
    formState: { errors, isValid },
    trigger,
    setValue,
    clearErrors,
    getValues,
  } = useForm({
    defaultValues: initialDefaultValues,
    mode: "onChange",
  });

  const processCommonData = (data, headerLocale) => {
    return (
      data?.CommonDetails?.map((item) => ({
        id: item.id,
        name: item.name,
        i18nKey: `${headerLocale}_ADMIN_${item.name}`,
      })) || []
    );
  };

  const processSingleData = (item, headerLocale) => {
    if (!item) return null;
    if (typeof item === "object" && item.divyangcardid && item.disabilitytype) {
      return {
        disabilitytype: {
          id: item.disabilitytype.id,
          name: item.disabilitytype.name,
          i18nKey: `${headerLocale}_ADMIN_${item.disabilitytype.name}`,
        },
        divyangcardid: item.divyangcardid,
        divyangpercent: item.divyangpercent,
      };
    }
    return null; // Handle cases where item is neither a string nor an object with id and name
  };

  const handleChange = useCallback((e) => {
    setRangeValue(parseInt(e.target.value));
  }, []);

  const divyangFunction = useCallback(
    (data) => {
      const divyangData = processCommonData(data, headerLocale);
      setDivyangs(divyangData);
      return { divyangData };
    },
    [headerLocale]
  );

  const getDivyang = { CommonSearchCriteria: { Option: "divyang" } };
  Digit.Hooks.bmc.useCommonGet(getDivyang, { select: divyangFunction });

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
    trigger(); // Validate the form on mount to show errors if fields are empty
  }, [trigger]);

  useEffect(() => {
    control.setValue("divyangpercent", rangeValue);
  }, [rangeValue, control]);

  useEffect(() => {
    if (initialRows) {
      const processeddata = processSingleData(initialRows, headerLocale);
      if (processeddata) {
        setValue("divyangcardid", processeddata.divyangcardid || "");
        setValue("disabilitytype", processeddata.disabilitytype || "");
        setRangeValue(processeddata.divyangpercent || 0);

        // Clear errors for fields that received initial values
        if (processeddata.divyangcardid) clearErrors("divyangcardid");
        if (processeddata.disabilitytype) clearErrors("disabilitytype");
        if (processeddata.divyangpercent) clearErrors("divyangpercent");
      }
    }
  }, [initialRows, setValue, headerLocale, clearErrors]);

  const handleToggle = () => {
    setIsEditable(!isEditable);
  };

  return (
    <React.Fragment>
      <div className="bmc-row-card-header">
        <div className="bmc-card-row">
          <div className="bmc-card-row">
            <span className="bmc-col-large-header">
              <div className="bmc-title">{t("BMC_DISABILITY_DETAILS")}</div>
            </span>
            <span className="bmc-col-small-header" style={{ textAlign: "end" }}>
              <ToggleSwitch
                id={"DisabilityToggle"}
                isOn={isEditable}
                handleToggle={handleToggle}
                onLabel={t("Yes")}
                offLabel={t("No")}
                disabled={!AllowEdit}
                visible={AllowEdit}
              />
            </span>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
            <CardLabel className="bmc-label">{t("BMC_UDID_ID")}&nbsp;{errors.divyangcardid && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.divyangcardid.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name="divyangcardid"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput 
                      disabled={!isEditable} 
                      readOnly 
                      value={props.value || ""} 
                      autoFocus={focusIndex.index === props.name}
                      onChange={(e) => {
                        props.onChange(e.target.value);
                        setFocusIndex({ index: props.name });
                      }}
                      onBlur={(e) => {
                        setFocusIndex({ index: -1 });
                        props.onBlur(e);
                      }}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
         
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">{t("BMC_DISABILITY_TYPE")}&nbsp;{errors.disabilitytype && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.disabilitytype.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name={"disabilitytype"}
                rules={{
                  required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("BMC_SELECT_THE_DISABILITY_TYPE")}
                        selected={props.value}
                        select={(divyang) => props.onChange(divyang)}
                        onBlur={props.onBlur}
                        option={divyangs}
                        optionKey="i18nKey"
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput readOnly value={t(props.value?.i18nKey || "")} />
                    )}
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col2-card">
            <div className="bmc-range-container">
              <CardLabel className="bmc-label">{t("BMC_DISABILITY_PERCENTAGE")}&nbsp;{errors.divyangpercent && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.divyangpercent.message}</sup>}</CardLabel>
              <Controller
                control={control}
                name={"divyangpercent"}
                rules={{
                  required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                }}
                render={(props) => (
                  <div>
                    <input
                      disabled={!isEditable}
                      type="range"
                      min="1"
                      max="100"
                      className="bmc-range-slider"
                      value={rangeValue}
                      onChange={(e) => {
                        handleChange(e);
                        props.onChange(e.target.value);
                      }}
                      list="tickmarks"
                    />
                    <datalist id="tickmarks">
                      {Array.from({ length: 100 }, (_, i) => (
                        <option key={i} value={i + 1}></option>
                      ))}
                    </datalist>
                    <span className="range-value">
                      {t("Disability")}: {rangeValue}%
                    </span>
                  </div>
                )}
              />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default DisabilityCard;
