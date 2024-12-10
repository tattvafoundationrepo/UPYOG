import { CardLabel, DatePicker, Dropdown, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import isEqual from "lodash.isequal";
import { useLocation } from "react-router-dom";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import dropdownOptions from "../pagecomponents/dropdownOptions.json";
import ToggleSwitch from "./Toggle";

const PersonalDetailCard = ({ onUpdate, initialRows = {}, AllowEdit = true, tenantId, userType }) => {
  const { t } = useTranslation();
  const location = useLocation();
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const [castes, setCastes] = useState([]);
  const [religions, setReligions] = useState([]);
  const headerLocale = useMemo(() => Digit.Utils.locale.getTransformedLocale(tenantId), [tenantId]);
  const aadharRef = location.state?.aadharRef;


  
  const RequiredFieldMessage = ({ fieldValue }) => {
    if (!fieldValue || (typeof fieldValue === 'object' && !fieldValue.name)) {
      return <p style={{ color: 'red', fontSize: '12px', marginTop: '-10px' }}>This is required field *</p>;
    }
    return null;
  };
  const {
    control,
    formState: { errors, isValid },
    setValue,
    trigger,
    clearErrors,
    watch,
    getValues,
  } = useForm({
    defaultValues: {
      aadharRef: initialRows?.AadharUser?.aadharRef || aadharRef,
      title: initialRows?.title || "",
      aadharName: initialRows?.AadharUser?.aadharName || "",
      father: initialRows?.AadharUser?.father || "",
      dob: initialRows?.AadharUser?.dob || "",
      gender: initialRows?.AadharUser?.gender || "",
      transgenderId: initialRows?.UserOtherDetails?.transgenderId || "",
      religion: initialRows?.UserOtherDetails?.religion || "",
      casteCategory: initialRows?.UserOtherDetails?.caste || "",
    },
    mode: "onChange",
  });

  const formValuesRef = useRef(getValues());
  const formValues = watch();
  const genderValue = watch("gender");

  const processSingleData = useCallback((item, headerLocale) => {
    if (!item) return null;

    const genderMapping = {
      male: { id: 1, name: "MALE" },
      female: { id: 2, name: "FEMALE" },
      transgender: { id: 3, name: "TRANSGENDER" },
    };

    const titleMapping = {
      mr: { id: 1, name: "MR" },
      mrs: { id: 2, name: "MRS" },
      miss: { id: 3, name: "MISS" },
    };

    if (typeof item === "string") {
      // Check if the string matches a title
      const title = titleMapping[item.toLowerCase()];
      if (title) {
        return {
          ...title,
          name: title.name,
          i18nKey: `${headerLocale}_ADMIN_${title.name.toUpperCase()}`,
        };
      }

      // Check if the string matches a gender
      const gender = genderMapping[item.toLowerCase()];
      if (gender) {
        return {
          ...gender,
          name: gender.name,
          i18nKey: `${headerLocale}_ADMIN_${gender.name.toUpperCase()}`,
        };
      }

      // If no title or gender match, return null
      return null;
    }

    if (typeof item === "object" && item.id && item.name) {
      return {
        id: item.id,
        name: item.name,
        i18nKey: `${headerLocale}_ADMIN_${item.name}`,
      };
    }

    return null;
  }, []);

  const processCommonData = useCallback(
    (data, headerLocale) =>
      data?.CommonDetails?.map((item) => ({
        id: item.id,
        name: item.name,
        i18nKey: `${headerLocale}_ADMIN_${item.name}`,
      })) || [],
    []
  );

  const casteFunction = useCallback(
    (data) => {
      const castesData = processCommonData(data, headerLocale);
      setCastes(castesData);
      return { castesData };
    },
    [headerLocale, processCommonData]
  );

  const religionFunction = useCallback(
    (data) => {
      const religionsData = processCommonData(data, headerLocale);
      setReligions(religionsData);
      return { religionsData };
    },
    [headerLocale, processCommonData]
  );

  const getCaste = { CommonSearchCriteria: { Option: "caste" } };
  const getReligion = { CommonSearchCriteria: { Option: "religion" } };

  Digit.Hooks.bmc.useCommonGet(getCaste, { select: casteFunction });
  Digit.Hooks.bmc.useCommonGet(getReligion, { select: religionFunction });

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
      const casteData = processSingleData(initialRows?.UserOtherDetails?.caste, headerLocale);
      const religionData = processSingleData(initialRows?.UserOtherDetails?.religion, headerLocale);
      const genderData = processSingleData(initialRows?.AadharUser?.gender, headerLocale);
      const titleData = processSingleData(initialRows?.title, headerLocale);
      setValue("aadharRef", initialRows?.AadharUser?.aadharRef || aadharRef);
      setValue("title", titleData || "");
      setValue("aadharName", initialRows?.AadharUser?.aadharName || "");
      setValue("father", initialRows?.AadharUser?.father || "");
      setValue("dob", initialRows?.AadharUser?.dob || "");
      setValue("gender", genderData || "");
      setValue("transgenderId", initialRows?.UserOtherDetails?.transgenderId || "");
      setValue("religion", religionData || "");
      setValue("casteCategory", casteData || "");

      // Clear errors for fields that received initial values
      if (initialRows?.AadharUser?.aadharRef) clearErrors("aadharRef");
      if (titleData) clearErrors("title");
      if (initialRows?.AadharUser?.aadharName) clearErrors("aadharName");
      if (initialRows?.AadharUser?.father) clearErrors("father");
      if (initialRows?.AadharUser?.dob) clearErrors("dob");
      if (genderData) clearErrors("gender");
      if (initialRows?.UserOtherDetails?.transgenderId) clearErrors("transgenderId");
      if (religionData) clearErrors("religion");
      if (casteData) clearErrors("casteCategory");
    }
  }, [initialRows, setValue, headerLocale, clearErrors, processSingleData, aadharRef]);

  const handleToggle = () => {
    setIsEditable(!isEditable);
  };

  return (
    <React.Fragment>
      <form className="bmc-row-card-header">
        <div className="bmc-card-row">
          <span className="bmc-col-large-header">
            <div className="bmc-title">{t("BMC_PERSONAL_DETAILS")}</div>
          </span>
          <span className="bmc-col-small-header" style={{ textAlign: "end" }}>
            <ToggleSwitch
              id={"PersonalToggle"}
              isOn={isEditable}
              handleToggle={handleToggle}
              onLabel={t("Editable")}
              offLabel={t("Readonly")}
              disabled={!AllowEdit}
              visible={AllowEdit}
            />
          </span>
        </div>
        <div className="bmc-card-row">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_TITLE")}&nbsp;{errors.title && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.title.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="title"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT TITLE")}
                        selected={props.value}
                        select={props.onChange}
                        onBlur={props.onBlur}
                        option={dropdownOptions.title}
                        optionKey="name"
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={t(props.value?.name) || ""} />

                    )}
                    
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_AADHAR_NAME")}&nbsp;
                {errors.aadharName && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.aadharName.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="aadharName"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={!isEditable}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                    

                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_FATHER")}&nbsp;{errors.father && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.father.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="father"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={!isEditable}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                    

                  </div>
                )}
              />
            </LabelFieldPair>
          </div>

          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_DATE_OF_BIRTH")}&nbsp;{errors.dob && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.dob.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="dob"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <DatePicker
                      disabled={!isEditable}
                      date={props.value}
                      onChange={props.onChange}
                      // onBlur={props.onBlur}
                      type="date"
                    />
                    

                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
        <div className="bmc-row-card">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_RELIGION")}&nbsp;{errors.religion && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.religion.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="religion"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT RELIGION")}
                        selected={props.value}
                        select={props.onChange}
                        onBlur={props.onBlur}
                        option={religions}
                        optionKey="i18nKey"
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={t(props.value?.i18nKey || "")} />
                    )}
                    

                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_CASTECATEGORY")}&nbsp;
                {errors.casteCategory && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.casteCategory.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="casteCategory"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT CASTE CATEGORY")}
                        selected={props.value}
                        select={props.onChange}
                        onBlur={props.onBlur}
                        option={castes}
                        optionKey="i18nKey"
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={t(props.value?.i18nKey) || ""} />
                    )}
                    

                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
        <div className="bmc-row-card">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_GENDER")}&nbsp;{errors.gender && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.gender.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="gender"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT GENDER")}
                        selected={props.value}
                        select={(value) => {
                          props.onChange(value);
                          if (value?.name !== "Transgender") {
                            setValue("transgenderId", "");
                          }
                        }}
                        onBlur={props.onBlur}
                        option={dropdownOptions.gender}
                        optionKey="name"
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={t(props.value?.name) || ""} />
                    )}
                    

                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("BMC_TRANSGENDER_ID")}&nbsp;
                {errors.transgenderId && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.transgenderId.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="transgenderId"
                rules={{ required: genderValue?.name === "TRANSGENDER" ? t("CORE_COMMON_REQUIRED_ERRMSG") : false }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={!isEditable || genderValue?.name !== "TRANSGENDER"}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                    />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
        <div className="bmc-row-card">
          {window.location.href.includes("/employee") ? (
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("BMC_AADHAR_NUMBER")}</CardLabel>
                <Controller
                  control={control}
                  name="aadharRef"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        disabled={true}
                        readOnly={true}
                        value={props.value}
                        onChange={(e) => props.onChange(e.target.value)}
                        onBlur={props.onBlur}
                        optionKey="i18nKey"
                        t={t}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          ) : (
            <div className="bmc-col3-card" style={{ display: "none" }}>
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("BMC_AADHAR_NUMBER")}</CardLabel>
                <Controller
                  control={control}
                  name="aadharRef"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <TextInput
                        disabled={true}
                        readOnly={true}
                        value={props.value}
                        onChange={(e) => props.onChange(e.target.value)}
                        onBlur={props.onBlur}
                        optionKey="i18nKey"
                        t={t}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          )}
        </div>
      </form>
    </React.Fragment>
  );
};

export default PersonalDetailCard;
