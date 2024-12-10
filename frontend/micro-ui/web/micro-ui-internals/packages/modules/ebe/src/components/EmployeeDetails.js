import { AddIcon, RemoveIcon, TextInput } from "@upyog/digit-ui-react-components";
import { CardLabel, Dropdown, LabelFieldPair, CheckBox } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState, useMemo } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import isEqual from "lodash.isequal";
import { useLocation } from "react-router-dom";

const EmployeeDetails = ({ tenantId, onUpdate, initialRows = {}, AddOption = true, AllowRemove = true, AllowEdit = true }) => {
  const { t } = useTranslation();
  const [isEditable, setIsEditable] = useState(AllowEdit);

  const initialDefaultValues = useMemo(() => {
    return {
      employeeCode: "",
      department: "",
      designation: "",
      employeename: "",
      mobileNumber: "",
      sunpended: 'No',
      susOrderNumber: ""
    };
  }, []);

  const {
    control,
    handleSubmit,
    reset,
    getValues,
    setValue,
    watch,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: initialDefaultValues,
    mode: "onChange"
  });

  let [department, setDepartment] = useState([]);
  let [designation, setDesignation] = useState([]);

  const [rows, setRows] = useState([]);
  const employeeCode = watch("employeeCode");
  const location = useLocation();

  const checkCode = (getEmployee, length) => {
    return getEmployee?.empCode[0]?.length >= length;
  };

  const getEmployee = { "empCode": [employeeCode] };

  const ExecuteFlag = checkCode(getEmployee, 7) || false;
  const { data: data1, isLoading: isLoading1 } = Digit.Hooks.hrms.useHrmsMDMS(tenantId, "egov-hrms", "HRMSRolesandDesignation");
  const { data: data2, isLoading: isLoading2 } = Digit.Hooks.ebe.useEmployeeInSAP(getEmployee, ExecuteFlag);


  //console.log("tesing",data1);
  useEffect(() => {
    if (!isLoading1 && data1) {
      const departmentData = data1?.MdmsRes?.['common-masters']?.Department || [];
      const designationData = data1?.MdmsRes?.['common-masters']?.Designation || [];
      setDepartment(departmentData);
      setDesignation(designationData);
    }
  }, [data1, isLoading1]);

  useEffect(() => {
    if (!isLoading2 && data2?.EmployeeData) {
      const details = data2?.EmployeeData[0] || {};
      if (details) {
        setValue("employeename", details.empFname + ' ' + details.empMname + ' ' + details.empLname);
        setValue("department", { "code":details.empDepartment,"name":'abc',"active":'true',"i18key": t("COMMON_MASTERS_DEPARTMENT_" + details.empDepartment) });
        setValue("designation", { "code":details.empDesignation,"name":'abc',"active":'true',"i18key": t("COMMON_MASTERS_DESIGNATION_" + details.empDesignation) });
        setValue("mobileNumber", details.empMob);
        setValue("email", details.empEmail)
      } else {
        setValue("department", "");
        setValue("designation", "");
        setValue("employeename", "");
        setValue("mobileNumber", "");
        setValue("email", "");
        alert("Searched Employee Code Does Not Exist!!");
      }
    } else {
      setValue("department", "");
      setValue("designation", "");
      setValue("employeename", "");
      setValue("mobileNumber", "");
      setValue("email", "");
      setValue("sunpended", 'No');
    }
  }, [data2, setValue, isLoading2, employeeCode, t]);

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

  const addRow = () => {
    const formData = getValues();

    const newRow = {
      employeename: formData.employeename,
      department: formData.department,
      employeeCode: formData.employeeCode,
      designation: formData.designation,
      mobileNumber: formData.mobileNumber,
      email: formData.email,
      suspended: formData.suspended,
      susOrderNumber: formData.susOrderNumber
    };
    console.log(newRow);
    const isDuplicate = rows.some((row) => row.employeeCode === newRow.employeeCode);

    if (isDuplicate) {
      alert("Duplicate qualification detected, not adding.");
      return; // Stop the function execution to avoid adding a duplicate qualification
    }
    const updatedRows = [...rows, newRow];
    setRows(updatedRows);

    //reset(initialDefaultValues);
    onUpdate(updatedRows);
  };

  const removeRow = (index) => {
    const updatedRows = rows.filter((_, i) => i !== index);
    setRows(updatedRows);
    onUpdate(updatedRows);
  };

  return (
    <React.Fragment>
      <div className="bmc-row-card-header">
        <div className="bmc-card-row">
          <div className="bmc-title">{t("EBE_EMPLOYEE_LIST")}</div>
          <div className="bmc-table-container" style={{ padding: "1rem" }}>
            <form onSubmit={handleSubmit(addRow)}>
              <table className="bmc-hover-table">
                <thead>
                  <tr>
                    <th scope="col">{t("EBE_EMP_CODE")}</th>
                    <th scope="col">{t("EBE_EMP_NAME")}</th>
                    <th scope="col">{t("EBE_EMP_DEPT")}</th>
                    <th scope="col">{t("EBE_EMP_DESG")}</th>
                    <th scope="col">{t("EBE_CONTACT_NUMBER")}</th>
                    <th scope="col">{t("EBE_EMAIL")}</th>
                    <th scope="col">{t("SUSPENDED")}?</th>
                    <th scope="col">{t("EBE_SUS_ORD_NUMBER")}</th>
                    {AllowRemove && <th scope="col"></th>}
                  </tr>
                </thead>
                <tbody>
                  {AddOption && (
                    <tr>
                      <td data-label={t("EBE_EMP_CODE")} style={{ textAlign: "left" }}>
                        <Controller
                          control={control}
                          name="employeeCode"
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
                              <TextInput
                                placeholder={t("EMPLOYEE CODE")}
                                value={props.value}
                                onChange={props.onChange}
                                onBlur={props.onBlur}
                                t={t}
                              />
                              {errors.employeeCode && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.employeeCode.message}</sup>}
                            </div>
                          )}
                        />
                      </td>
                      <td data-label={t("EBE_EMP_NAME")} style={{ textAlign: "left" }}>
                        <Controller
                          control={control}
                          name="employeename"
                          render={(props) => (
                            <div>
                              <TextInput {...props} placeholder={t("EBE_EMP_NAME")} disabled />
                              {errors.employeename && <sup style={{ color: "red", fontSize: "x-small" }}>{errors.employeename.message}</sup>}
                            </div>
                          )}
                        />
                      </td>
                      <td data-label={t("EBE_EMP_DEPT")} style={{ textAlign: "left" }}>
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
                      </td>
                      <td data-label={t("EBE_EMP_DESG")} style={{ textAlign: "left" }}>
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
                      </td>
                      <td data-label={t("EBE_CONTACT_NUMBER")} style={{ textAlign: "left" }}>
                        <Controller
                          control={control}
                          name="mobileNumber"
                          render={(props) => (
                            <div>
                              <TextInput {...props} placeholder={t("CONTACT NUMBER")} disabled />
                            </div>
                          )}
                        />
                      </td>
                      <td data-label={t("EBE_EMAIL")} style={{ textAlign: "left" }}>
                        <Controller
                          control={control}
                          name="email"
                          render={(props) => (
                            <div>
                              <TextInput {...props} placeholder={t("EBE_EMAIL")} disabled />
                            </div>
                          )}
                        />
                      </td>
                      <td data-label={t("EBE_SUSPENDED")} style={{ textAlign: "left" }}>
                        <Controller
                          control={control}
                          name="suspended"
                          render={(props) => (
                            <div>
                              <Dropdown
                                placeholder={t("SELECT EBE_SUSPENDED")}
                                selected={props.value}
                                select={props.onChange}
                                onBlur={props.onBlur}
                                option={[{label:0,value:'No'},{label:1,value:'Yes'}]}
                                optionKey={"value"}
                                t={t}
                                isMandatory={true}
                                className="employee-select-wrap bmc-form-field"
                              />
                            </div>
                          )}
                        />
                      </td>
                      <td data-label={t("EBE_SUS_ORD_NUMBER")} style={{ textAlign: "left" }}>
                        <Controller
                          control={control}
                          name="susOrderNumber"
                          render={(props) => (
                            <div>
                              <TextInput {...props} placeholder={t("SUS_ORD_NUMBER")} />
                            </div>
                          )}
                        />
                      </td>
                      <td data-label={t("BMC_ADD_ROW")}>
                        <button type="submit">
                          <AddIcon className="bmc-add-icon" />
                        </button>
                      </td>
                    </tr>
                  )}

                  {rows.map((row, index) => (
                    <tr key={index}>
                      <td style={{ display: "none" }}>{row.id}</td>
                      <td data-label={t("EBE_EMP_CODE")}>{row.employeeCode}</td>
                      <td data-label={t("EBE_EMP_NAME")}>{row.employeename}</td>
                      <td data-label={t("EBE_EMP_DEPT")}>{row.department.i18key}</td>
                      <td data-label={t("EBE_EMP_DESG")}>{row.designation.i18key}</td>
                      <td data-label={t("EBE_CONTACT_NUMBER")}>{row.mobileNumber}</td>
                      <td data-label={t("EBE_CONTACT_SUS_STATUS")}>{row.email}</td>
                      <td data-label={t("EBE_CONTACT_ORDERNO")}>{row.suspended.value}</td>
                      <td data-label={t("EBE_CONTACT_Email")}>{row.susOrderNumber}</td>
                      {AllowRemove && (
                        <td data-label={t("Remove Row")}>
                          <button type="button" onClick={() => removeRow(index)}>
                            <RemoveIcon className="bmc-remove-icon" />
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </form>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default EmployeeDetails;
