import { TextInput, Dropdown, SearchIconSvg } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState, useMemo } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";

const EmployeeAdd = ({ config, onSelect, formData }) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [localFormData, setLocalFormData] = useState({});
  const [selectedSuspension, setSelectedSuspension] = useState(formData?.[config.key]?.suspended || null);

  useEffect(() => {
    // Initialize localFormData with formData from props if necessary
    setLocalFormData(formData[config.key] || {});
  }, [formData, config.key]);
  

  
  const initialDefaultValues = useMemo(() => {
    return {
      employeeCode: "",
      department: "",
      designation: "",
      employeename: "",
      mobileNumber: "",
      sunpended: "No",
      susOrderNumber: "",
    };
  }, []);

  useEffect(() => {
    setValueManual(selectedSuspension, "suspended");
  }, [selectedSuspension]);

  const {
    control,
    setValue,
    watch
  } = useForm({
    defaultValues: initialDefaultValues
  });

  let [department, setDepartment] = useState([]);
  let [designation, setDesignation] = useState([]);

  const employeeCode = watch("employeeCode");

  const checkCode = (getEmployee, length) => {
    return getEmployee?.empCode[0]?.length >= length;
  };

  const getEmployee = { empCode: [employeeCode] };

  const ExecuteFlag = checkCode(getEmployee, 7) || false;
  const { data: data1, isLoading: isLoading1 } = Digit.Hooks.hrms.useHrmsMDMS(tenantId, "egov-hrms", "HRMSRolesandDesignation");
  const { data: data2, isLoading: isLoading2 } = Digit.Hooks.ebe.useEmployeeInSAP(getEmployee, ExecuteFlag);

  //console.log("tesing",data1);
  useEffect(() => {
    if (!isLoading1 && data1) {
      const departmentData = data1?.MdmsRes?.["common-masters"]?.Department || [];
      const designationData = data1?.MdmsRes?.["common-masters"]?.Designation || [];
      setDepartment(departmentData);
      setDesignation(designationData);
    }
  }, [data1, isLoading1]);

  // Function to reset form fields
  const resetFields = () => {
    setValue("department", "");
    setValue("designation", "");
    setValue("employeename", "");
    setValue("mobileNumber", "");
    setValue("email", "");
  };
  // Helper function to update multiple fields
  const updateEmployeeDetails = (details) => {
    if (details) {
      const fullName = `${details.empFname} ${details.empMname} ${details.empLname}`;
      const departmentValue = {
        code: details.empDepartment,
        i18key: t("COMMON_MASTERS_DEPARTMENT_" + details.empDepartment),
        active: "true",
      };
      const designationValue = {
        code: details.empDesignation,
        i18key: t("COMMON_MASTERS_DESIGNATION_" + details.empDesignation),
        active: "true",
      };
      

      // Update fields using react-hook-form's setValue
      setValue("employeename", fullName);
      setValue("department", departmentValue);
      setValue("designation", designationValue);
      setValue("mobileNumber", details.empMob);
      setValue("email", details.empEmail);

      // Call setValues to pass values to parent component
      setValues(fullName, "employeename");
      setValues(fullName, "employeename");
      setValues(departmentValue, "department");
      setValues(designationValue, "designation");
      setValues(details.empMob, "mobileNumber");
      setValues(details.empEmail, "email");
    } else {
      resetFields();
    }
  };



  // Update form fields with employee data after fetching
  useEffect(() => {
    if (!isLoading2 && data2?.EmployeeData) {
      const details = data2?.EmployeeData[0] || {};
      updateEmployeeDetails(details); // Call the helper function
    } else {
      resetFields();
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

  let test = {}

  function setValues(value, input) {
    test = { ...test, [input]: value }
    onSelect(config.key, test);
  }

  function setValueManual(value, input) {
    onSelect(config.key, { ...formData[config.key], [input]: value }, test);
  }

  return (
    <React.Fragment>
      <div className="bmc-table-container" style={{ padding: "1rem" }}>
        <div className="ebe-add-employee-wrp">
          <div className="bmc-col4-card ebe-search-bar">
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
                <div style={{ position: "relative" }}>
                  <TextInput placeholder={t("SEARCH EMPLOYEE CODE")} value={props.value} onChange={props.onChange} onBlur={(e) => setValueManual(e.target.value, "employeeCode")} t={t} />
                  <div style={{ position: "absolute", right: "0px", top: "4px" }}>
                    <SearchIconSvg />
                  </div>
                </div>
              )}
            />
          </div>
          <hr style={{ border: "1px solid #ccc", margin: "20px 0" }} />
          <div style={{ display: "flex", flexDirection: "column" }}>
            <div className="bmc-row-card ebe-add-employee">
              <div className="bmc-col4-card" style={{ width: "100%" }}>
                <Controller
                  control={control}
                  name="employeename"
                  render={(props) => (
                    <div>
                      <TextInput {...props} placeholder={t("EBE_EMP_NAME")} disabled />
                    </div>
                  )}
                />
              </div>
              <div className="bmc-col4-card" style={{ width: "100%" }}>
                <Controller
                  control={control}
                  name="department"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
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
                    </div>
                  )}
                />
              </div>
            </div>
            <div className="bmc-row-card ebe-add-employee">
              <div className="bmc-col4-card" style={{ width: "100%" }}>

                <Controller
                  control={control}
                  name="email"
                  render={(props) => (
                    <div>
                      <TextInput {...props} placeholder={t("EBE_EMAIL")} disabled />
                    </div>
                  )}
                />
              </div>
              <div className="bmc-col4-card" style={{ width: "100%" }}>
                <Controller
                  control={control}
                  name="designation"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
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
                    </div>
                  )}
                />
              </div>
            </div>
            <div className="bmc-row-card ebe-add-employee">
              <div className="bmc-col4-card" style={{ width: "100%" }}>
                <Controller
                  control={control}
                  name="mobileNumber"
                  render={(props) => (
                    <div>
                      <TextInput {...props} placeholder={t("CONTACT NUMBER")} disabled />
                    </div>
                  )}
                />
              </div>
              <div className="bmc-col4-card" style={{ width: "100%" }}>
                <Controller
                  control={control}
                  name="suspended"
                  render={(props) => (
                    <div>
                      <Dropdown
                        placeholder={t("SELECT EBE_SUSPENDED")}
                        selected={props.value}
                        select={setSelectedSuspension}
                        onBlur={props.onBlur}
                        option={[
                          { label: 0, value: "No" },
                          { label: 1, value: "Yes" },
                        ]}
                        optionKey={"value"}
                        t={t}
                        isMandatory={true}
                        className="employee-select-wrap bmc-form-field"
                      />
                    </div>
                  )}
                />
              </div>

            </div>

            <div className="bmc-col4-card">
              <Controller
                control={control}
                name="susOrderNumber"
                render={(props) => (
                  <div>
                    <TextInput {...props} placeholder={t("SUS_ORD_NUMBER")} onBlur={(e) => setValueManual(e.target.value, "susOrderNumber")} />
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

export default EmployeeAdd;
