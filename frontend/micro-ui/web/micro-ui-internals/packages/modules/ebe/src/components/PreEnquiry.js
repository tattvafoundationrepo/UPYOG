import { CardLabel, DatePicker, Dropdown, LabelFieldPair, TextInput, TextArea, Toast } from "@upyog/digit-ui-react-components";
import isEqual from "lodash.isequal";
//import { useLocation } from "react-router-dom";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import ToggleSwitch from "./Toggle";

const PreEnquiryCard = ({ onUpdate, initialRows = {}, AllowEdit = true, tenantId }) => {
  const { t } = useTranslation();
  //const location = useLocation();
  const [isEditable, setIsEditable] = useState(AllowEdit);
  let [department, setDepartment] = useState([]);
  let [designation, setDesignation] = useState([]);
  //const headerLocale = useMemo(() => Digit.Utils.locale.getTransformedLocale(tenantId), [tenantId]);
  const [val, setVal] = useState(sessionStorage.getItem("GeneratedENQID"));
  const {
    control,
    formState: { errors, isValid },
    setValue,
    trigger,
    clearErrors,
    watch,
    getValues,
  } = useForm({
    mode: "onChange",
  });

  const checkCode = (getEmployee, length) => {
    return getEmployee?.empCode[0]?.length >= length;
  };

  const employeeCode = watch("employee");

  const getEmployee = { empCode: [employeeCode] };

  const ExecuteFlag = checkCode(getEmployee, 7) || false;

  const GeneratedENQID = sessionStorage.getItem("GeneratedENQID");
  const { data, isLoading } = Digit.Hooks.hrms.useHrmsMDMS(tenantId, "egov-hrms", "HRMSRolesandDesignation");

  useEffect(() => {
    setVal(GeneratedENQID);
    setValue("enquiryCode", GeneratedENQID);
  }, [GeneratedENQID,setValue]);

  useEffect(() => {
    if (!isLoading && data) {
      const departmentData = data?.MdmsRes?.["common-masters"]?.Department || [];
      const designationData = data?.MdmsRes?.["common-masters"]?.Designation || [];
      setDepartment(departmentData);
      setDesignation(designationData);
    }
  }, [data, isLoading]);

  const { data: test, isLoading: isEmployeeLoading } = Digit.Hooks.ebe.useEmployeeInSAP(getEmployee, ExecuteFlag);

  useEffect(() => {
    if (!isEmployeeLoading && test) {
      const details = test?.EmployeeData[0] || {};
      if (details) {
        setValue("enquiryCode", GeneratedENQID);
        setValue("employeename", details.empFname + " " + details.empMname + " " + details.empLname);
        setValue("department", {
          code: details.empDepartment,
          name: "abc",
          active: "true",
          i18key: t("COMMON_MASTERS_DEPARTMENT_" + details.empDepartment),
        });
        setValue("designation", {
          code: details.empDesignation,
          name: "abc",
          active: "true",
          i18key: t("COMMON_MASTERS_DESIGNATION_" + details.empDesignation),
        });
      } else {
        setValue("enquiryCode", GeneratedENQID);
        setValue("department", "");
        setValue("designation", "");
        setValue("employeename", "");
        setValue("enquiryCode", "");
        alert("Searched Employee Code Does Not Exist!!");
      }
    } else {
      setValue("enquiryCode", GeneratedENQID);
      setValue("department", "");
      setValue("designation", "");
      setValue("employeename", "");
      setValue("enquiryCode", "");
    }
  }, [test, setValue, isEmployeeLoading, getValues, t,GeneratedENQID]);

  const formValuesRef = useRef(getValues());
  const formValues = watch();

  function getdepartmentdata() {
    return department.map((ele) => {
      ele["i18key"] = t("COMMON_MASTERS_DEPARTMENT_" + ele.code);
      return ele;
    });
  }

  function getdesignationdata() {
    return designation.map((ele) => {
      ele["i18key"] = t("COMMON_MASTERS_DESIGNATION_" + ele.code);
      return ele;
    });
  }

  // const selectDepartment = (value) => {
  //   setassignments((pre) => pre.map((item) => (item.key === assignment.key ? { ...item, department: value } : item)));
  // };

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

  const handleToggle = () => {
    setIsEditable(!isEditable);
  };

  const handleEmployeeChange = (employee) => {
    //setValue("employee", employee);
  };

  return (
    <React.Fragment>
      <form className="bmc-row-card-header">
        <div className="bmc-card-row">
          <div className="bmc-col-large-header">
            <div className="bmc-title">{t("EBE_PE_TITLE")}</div>
          </div>
          <div className="bmc-col-small-header" style={{ textAlign: "end" }}>
            <ToggleSwitch
              id={"PersonalToggle"}
              isOn={isEditable}
              handleToggle={handleToggle}
              onLabel={t("Editable")}
              offLabel={t("Readonly")}
              disabled={!AllowEdit}
              visible={AllowEdit}
            />
          </div>
        </div>
        <div className="bmc-card-row">
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("EBE_ENQUIRY_CODE")}&nbsp;
                {errors.enquiryCode && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.enquiryCode.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="enquiryCode"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextInput
                      disabled={true}
                      readOnly={true}
                      value={val}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      // optionKey="i18nKey"
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
                {t("EBE_ENQUIRY_CODE_OLD")}&nbsp;
                {errors.oldenquiryCode && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.oldenquiryCode.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="oldenquiryCode"
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
                {t("EBE_ORDER_NO")}&nbsp;{errors.orderNo && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.orderNo.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="orderNo"
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
                {t("EBE_ORDER_DATE")}&nbsp;{errors.orderDate && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.orderDate.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="orderDate"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <DatePicker disabled={!isEditable} date={props.value} onChange={props.onChange} onBlur={props.onBlur} />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
        </div>
        <div className="bmc-row-card">
          <div className="bmc-col4-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("EBE_ENQUIRY_SUBJECT")}&nbsp;
                {errors.enquirySubject && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.enquirySubject.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="enquirySubject"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    <TextArea
                      disabled={!isEditable}
                      readOnly={!isEditable}
                      value={props.value}
                      onChange={(e) => props.onChange(e.target.value)}
                      onBlur={props.onBlur}
                      name="enquirySubject"
                      t={t}
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
                {t("EBE_EMPLOYEE")}&nbsp;{errors.employee && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.employee.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="employee"
                rules={{
                  required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                  maxLength: { value: 7, message: t("Employee code must be 7 characters long") },
                  // pattern: {
                  //   value: /^[A-Z]{4}0[A-Z0-9]{6}$/, // Regex pattern for IFSC code
                  //   message: t("Invalid EMPLOYEEcode") // Custom validation message
                  // }
                }}
                render={(props) => (
                  <div>
                    <TextInput placeholder={t("EMPLOYEE CODE")} value={props.value} onChange={props.onChange} onBlur={props.onBlur} t={t} />
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("EBE_EMP_NAME")}&nbsp;
                {errors.employeename && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.employeename.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="employeename"
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
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("EBE_DEPARTMENT")}&nbsp;{errors.department && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.department.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="department"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT DEPARTMENT")}
                        selected={props.value}
                        select={props.onChange}
                        onBlur={props.onBlur}
                        option={getdepartmentdata() || []}
                        optionKey={"i18key"}
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    ) : (
                      <TextInput disabled={!isEditable} readOnly={!isEditable} value={props.value?.name || ""} />
                    )}
                  </div>
                )}
              />
            </LabelFieldPair>
          </div>
          <div className="bmc-col3-card">
            <LabelFieldPair>
              <CardLabel className="bmc-label">
                {t("EBE_DESIGNATION")}&nbsp;
                {errors.designation && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.designation.message}</sup>}
              </CardLabel>
              <Controller
                control={control}
                name="designation"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                  <div>
                    {isEditable ? (
                      <Dropdown
                        placeholder={t("SELECT DESIGNATION")}
                        selected={props.value}
                        select={props.onChange}
                        onBlur={props.onBlur}
                        option={getdesignationdata() || []}
                        optionKey={"i18key"}
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
      </form>
    </React.Fragment>
  );
};

export default PreEnquiryCard;
